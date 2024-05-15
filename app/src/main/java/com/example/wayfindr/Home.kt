package com.example.wayfindr

import PopularAdapter
import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.wayfindr.home.CategoryDetailFragment
import com.example.wayfindr.databinding.FragmentHomeBinding
import com.example.wayfindr.home.NotificationFragment
import com.example.wayfindr.places.PlaceModel
import com.example.wayfindr.places.PlacesDetailFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.ismaeldivita.chipnavigation.ChipNavigationBar
import java.text.Collator
import java.util.Locale

class Home : Fragment(), PopularAdapter.OnItemClickListener {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()
    private val placesCollection = db.collection("places")
    private val turkishCollator = Collator.getInstance(Locale("tr", "TR"))

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var adapter: PopularAdapter

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
        adapter = PopularAdapter(emptyList(), this)

        loadUserProfilePicture()
        loadUserName()
        setupCitySpinner(view)
        setupRecyclerView(view)
        fetchPlacesData()
        setupCategoryButtons()
        setupNotificationButton()
    }

    private fun setupRecyclerView(view: View) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewPopular)
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = adapter
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

                adapter.setPlacesList(placesList)
            }
            .addOnFailureListener { exception ->
                Log.e(ContentValues.TAG, "Veri çekme işlemi başarısız. Hata: $exception")
            }
    }

    override fun onItemClick(placeId: String) {
        val selectedPlace = adapter.getPlaceByPlaceId(placeId)

        if (selectedPlace != null) {
            showPlaceDetailFragment(selectedPlace)
        }
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

    //Profil fotoğrafı yükleme
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

    //Profil ismi yükleme
    private fun loadUserName() {
        val user = firebaseAuth.currentUser
        user?.let {
            val userId = it.uid
            val userRef = FirebaseDatabase.getInstance().getReference("users/$userId")
            userRef.child("username").get().addOnSuccessListener { dataSnapshot ->
                if (dataSnapshot.exists() && isAdded) {
                    val userName = dataSnapshot.value as String
                    binding.homepageHitext.text = "Hi, $userName"
                }
            }
        }
    }

    //Category Fragment Open
    private fun setupCategoryButtons() {
        val categoryClickListener = View.OnClickListener { view ->
            val category = when (view.id) {
                R.id.category1, R.id.Category1_title -> "AVM"
                R.id.category2, R.id.Category2_title -> "Dini"
                R.id.category3, R.id.Category3_title -> "Doğa"
                R.id.Category4, R.id.Category4_title -> "Eğlence"
                R.id.category5, R.id.Category5_title -> "Kafe"
                R.id.category6, R.id.Category6_title -> "Müze"
                R.id.category7, R.id.Category7_title -> "Restoran"
                R.id.Category8, R.id.Category8_title -> "Daha Fazla"
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
        binding.Category8.setOnClickListener(categoryClickListener)
        binding.Category8Title.setOnClickListener(categoryClickListener)
    }

    private fun openCategoryDetailFragment(category: String) {
        val fragment = CategoryDetailFragment.newInstance(category)
        parentFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun setupNotificationButton() {
        binding.notificationSettingsButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, NotificationFragment())
                .addToBackStack(null)
                .commit()
        }
    }
}
