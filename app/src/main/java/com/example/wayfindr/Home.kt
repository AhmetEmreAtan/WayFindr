package com.example.wayfindr

import com.example.wayfindr.home.adapters.PopularAdapter
import android.content.ContentValues
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.wayfindr.home.CategoryDetailFragment
import com.example.wayfindr.databinding.FragmentHomeBinding
import com.example.wayfindr.home.EventsDetailFragment
import com.example.wayfindr.home.FilteredResultsBottomSheetFragment
import com.example.wayfindr.home.MessagesFragment
import com.example.wayfindr.home.NotificationFragment
import com.example.wayfindr.home.adapters.EventsAdapter
import com.example.wayfindr.home.adapters.FilteredAdapter
import com.example.wayfindr.home.models.EventModel
import com.example.wayfindr.places.PlaceModel
import com.example.wayfindr.places.PlacesDetailFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.Collator
import java.util.Locale

class Home : Fragment(), EventsAdapter.OnItemClickListener, PopularAdapter.OnItemClickListener, FilteredAdapter.OnItemClickListener {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()
    private val eventsCollection = db.collection("events")
    private val placesCollection = db.collection("places")
    private val turkishCollator = Collator.getInstance(Locale("tr", "TR"))

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var eventsAdapter: EventsAdapter
    private lateinit var popularAdapter: PopularAdapter
    private lateinit var filteredAdapter: FilteredAdapter
    private var filteredResultsBottomSheet: FilteredResultsBottomSheetFragment? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()
        eventsAdapter = EventsAdapter(emptyList(), this)
        popularAdapter = PopularAdapter(emptyList(), this)
        filteredAdapter = FilteredAdapter(emptyList(), this)

        loadUserProfilePicture()
        loadUserName()
        setupCitySpinner(view)
        setupRecyclerView(view)
        setupEventsRecyclerView(view)
        setupSearchListener()
        fetchPlacesData()
        fetchEventsData()
        setupCategoryButtons()
        setupNotificationButton()
        setupMessageButtons()
        setupKeyboardVisibilityListener()

    }

    private fun fetchEventsData() {
        eventsCollection
            .get()
            .addOnSuccessListener { querySnapshot ->
                val eventsList = mutableListOf<EventModel>()

                for (document in querySnapshot.documents) {
                    val eventId = document.id
                    val eventModel = document.toObject(EventModel::class.java)?.apply {
                        this.eventId = eventId
                    }

                    eventModel?.let {
                        eventsList.add(eventModel)
                    }
                }

                eventsAdapter.setEventsList(eventsList)
            }
            .addOnFailureListener { exception ->
                Log.e(ContentValues.TAG, "Veri çekme işlemi başarısız. Hata: $exception")
            }
    }

    private fun fetchPlacesData() {
        placesCollection
            .orderBy("placeName", Query.Direction.DESCENDING)
            .limit(5)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val placesList = mutableListOf<PlaceModel>()

                for (document in querySnapshot.documents) {
                    val placeId = document.id
                    val placeModel = document.toObject(PlaceModel::class.java)?.apply {
                        this.placeId = placeId
                    }

                    placeModel?.let {
                        placesList.add(placeModel)
                    }
                }

                placesList.sortWith(Comparator { place1, place2 ->
                    turkishCollator.compare(place1.placeName, place2.placeName)
                })

                popularAdapter.setPlacesList(placesList)
            }
            .addOnFailureListener { exception ->
                Log.e(ContentValues.TAG, "Veri çekme işlemi başarısız. Hata: $exception")
            }
    }

    private fun setupRecyclerView(view: View) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewPopular)
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = popularAdapter
    }

    private fun setupEventsRecyclerView(view: View) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewEvents)
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = eventsAdapter
    }

    private fun setupSearchListener() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (filteredResultsBottomSheet?.isVisible != true) {
                    showFilteredResultsBottomSheet()
                }
                filterPlaces(query ?: "")
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })

        val searchButtonId = binding.searchView.context.resources.getIdentifier("android:id/search_go_btn", null, null)
        val searchButton = binding.searchView.findViewById<View>(searchButtonId)
        searchButton?.setOnClickListener {
            val query = binding.searchView.query.toString()
            if (query.isNotEmpty()) {
                if (filteredResultsBottomSheet?.isVisible != true) {
                    showFilteredResultsBottomSheet()
                }
                filterPlaces(query)
            }
        }
    }

    private fun showFilteredResultsBottomSheet() {
        filteredResultsBottomSheet = FilteredResultsBottomSheetFragment.newInstance(this)
        filteredResultsBottomSheet?.show(parentFragmentManager, FilteredResultsBottomSheetFragment::class.java.simpleName)
    }

    private fun filterPlaces(query: String) {
        val lowerCaseQuery = query.toLowerCase(Locale.getDefault())
        placesCollection
            .orderBy("placeName")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val filteredList = mutableListOf<PlaceModel>()

                for (document in querySnapshot.documents) {
                    val placeId = document.id
                    val placeModel = document.toObject(PlaceModel::class.java)?.apply {
                        this.placeId = placeId
                    }

                    placeModel?.let {
                        if (it.placeName.toLowerCase(Locale.getDefault()).contains(lowerCaseQuery)) {
                            filteredList.add(placeModel)
                        }
                    }
                }

                filteredResultsBottomSheet?.updateResults(filteredList)
            }
            .addOnFailureListener { exception ->
                Log.e(ContentValues.TAG, "Veri çekme işlemi başarısız. Hata: $exception")
            }
    }

    override fun onItemClick(event: EventModel) {
        showEventDetailFragment(event)
    }

    override fun onItemClick(placeId: String) {
        val selectedPlace = popularAdapter.getPlaceByPlaceId(placeId)
        if (selectedPlace != null) {
            showPlaceDetailFragment(selectedPlace)
        }
    }

    fun onItemClickFiltered(place: PlaceModel) {
        showPlaceDetailFragment(place)
    }

    private fun showEventDetailFragment(selectedEvent: EventModel) {
        val fragment = EventsDetailFragment()
        val bundle = Bundle()
        bundle.putSerializable("selectedEvent", selectedEvent)
        fragment.arguments = bundle

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun showPlaceDetailFragment(selectedPlace: PlaceModel) {
        val fragment = PlacesDetailFragment()
        val bundle = Bundle()
        bundle.putSerializable("selectedPlace", selectedPlace)
        fragment.arguments = bundle

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun loadUserProfilePicture() {
        val user = firebaseAuth.currentUser
        user?.let { user ->
            val databaseReference = FirebaseDatabase.getInstance().getReference("users/${user.uid}")
            databaseReference.child("profileImageUrl").get().addOnSuccessListener { dataSnapshot ->
                if (dataSnapshot.exists()) {
                    val imageUrl = dataSnapshot.value as String?
                    if (!imageUrl.isNullOrEmpty()) {
                        context?.let { ctx ->
                            Glide.with(ctx)
                                .load(imageUrl)
                                .placeholder(R.drawable.profilephotoicon)
                                .error(R.drawable.error_image)
                                .transform(CircleCrop())
                                .into(binding.profilepicture)
                        }
                    } else {
                        binding.profilepicture.setImageResource(R.drawable.profilephotoicon)
                    }
                } else {
                    Toast.makeText(context, "Error: Profile picture URL not found", Toast.LENGTH_SHORT).show()
                    binding.profilepicture.setImageResource(R.drawable.profilephotoicon)
                }
            }.addOnFailureListener {
                Toast.makeText(context, "Error fetching profile picture: ${it.message}", Toast.LENGTH_SHORT).show()
                binding.profilepicture.setImageResource(R.drawable.profilephotoicon)
            }
        }
    }

    private fun loadUserName() {
        val user = firebaseAuth.currentUser
        user?.let {
            val userId = it.uid
            val userRef = FirebaseFirestore.getInstance().collection("users").document(userId)
            userRef.get().addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists() && isAdded) {
                    val userName = documentSnapshot.getString("username") ?: ""
                    binding.homepageHitext.text = "Hi, $userName"
                }
            }.addOnFailureListener { exception ->
                Log.d("ProfileFragment", "Error getting username: ", exception)
                Toast.makeText(context, "Kullanıcı adı yüklenirken hata oluştu.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupCitySpinner(view: View) {
        val cities = arrayOf("İstanbul", "Ankara", "İzmir", "Antalya", "Çanakkale")
        val adapter = ArrayAdapter(requireContext(), R.layout.selected_city_spinner, cities)
        adapter.setDropDownViewResource(R.layout.dropdown_city_spinner)
        val spinner = view.findViewById<Spinner>(R.id.City_Select_Button)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (parent.getItemAtPosition(position) != "İstanbul") {
                    Toast.makeText(requireContext(), "Coming soon", Toast.LENGTH_SHORT).show()
                    spinner.setSelection(adapter.getPosition("İstanbul"))
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setupCategoryButtons() {
        val categoryClickListener = View.OnClickListener { view ->
            val category = when (view.id) {
                R.id.category1, R.id.Category1_title -> "Avm"
                R.id.category2, R.id.Category2_title -> "Dini"
                R.id.category3, R.id.Category3_title -> "Doğa"
                R.id.Category4, R.id.Category4_title -> "Eğlence"
                R.id.category5, R.id.Category5_title -> "Kafe"
                R.id.category6, R.id.Category6_title -> "Müze"
                R.id.category7, R.id.Category7_title -> "Restoran"
                R.id.category8, R.id.Category8_title -> "Daha Fazla"
                else -> ""
            }
            openCategoryDetailFragment(category)
        }

        binding.category1.setOnClickListener(categoryClickListener)
        binding.Category1Title.setOnClickListener(categoryClickListener)
        binding.category2.setOnClickListener(categoryClickListener)
        binding.Category2Title.setOnClickListener(categoryClickListener)
        binding.category3.setOnClickListener(categoryClickListener)
        binding.Category3Title.setOnClickListener(categoryClickListener)
        binding.Category4.setOnClickListener(categoryClickListener)
        binding.Category4Title.setOnClickListener(categoryClickListener)
        binding.category5.setOnClickListener(categoryClickListener)
        binding.Category5Title.setOnClickListener(categoryClickListener)
        binding.category6.setOnClickListener(categoryClickListener)
        binding.Category6Title.setOnClickListener(categoryClickListener)
        binding.category7.setOnClickListener(categoryClickListener)
        binding.Category7Title.setOnClickListener(categoryClickListener)
        binding.category8.setOnClickListener(categoryClickListener)
        binding.Category8Title.setOnClickListener(categoryClickListener)
    }

    private fun openCategoryDetailFragment(category: String) {
        val fragment = CategoryDetailFragment.newInstance(category)
        parentFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun setupMessageButtons() {
        binding.messagingButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, MessagesFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun setupNotificationButton() {
        binding.notificationSettingsButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, NotificationFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun setupKeyboardVisibilityListener() {
        val rootView = binding.root
        rootView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            private var wasKeyboardVisible = false

            override fun onGlobalLayout() {
                val rect = Rect()
                rootView.getWindowVisibleDisplayFrame(rect)
                val screenHeight = rootView.height
                val keypadHeight = screenHeight - rect.bottom

                val isKeyboardVisible = keypadHeight > screenHeight * 0.15

                if (isKeyboardVisible != wasKeyboardVisible) {
                    if (isKeyboardVisible) {
                        (activity as MainActivity).binding.bottomNavBar.visibility = View.GONE
                    } else {
                        (activity as MainActivity).binding.bottomNavBar.visibility = View.VISIBLE
                    }
                }
                wasKeyboardVisible = isKeyboardVisible
            }
        })
    }


}