package com.example.wayfindr

import com.example.wayfindr.places.PlaceModel
import PlacesAdapter
import android.content.ContentValues.TAG
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.wayfindr.databinding.FragmentPlacesBinding
import com.example.wayfindr.places.AddPlacesFragment
import com.example.wayfindr.places.FilterFragment
import com.example.wayfindr.places.FilterResultListener
import com.example.wayfindr.places.ItemClickListener
import com.example.wayfindr.places.PlacesDetailFragment
import com.example.wayfindr.places.PlacesRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.Collator
import java.util.Locale

class Places : Fragment(), FilterResultListener {

    private var _binding: FragmentPlacesBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()
    private val placesCollection = db.collection("places")
    private val turkishCollator = Collator.getInstance(Locale("tr", "TR"))

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var adapter: PlacesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlacesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Add Places Button
        binding.addPlacesBtn.setOnClickListener {
            val fragment = AddPlacesFragment()
            val transaction = parentFragmentManager.beginTransaction()
            transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
            transaction.add(android.R.id.content, fragment).addToBackStack(null).commit()
        }

        binding.filterImage.setOnClickListener {
            val tag = "FilterBottomSheetFragment"

            val existingFragment = parentFragmentManager.findFragmentByTag(tag)

            if (existingFragment == null) {
                val filterFragment = FilterFragment()
                filterFragment.filterResultListener = this
                filterFragment.show(parentFragmentManager, tag)
            }
        }


        firebaseAuth = FirebaseAuth.getInstance()
        val placesRepository=PlacesRepository()
        adapter = PlacesAdapter(emptyList(), itemClickListener, firebaseAuth,placesRepository)

        binding.recyclerViewPlaces.adapter = adapter
        binding.recyclerViewPlaces.layoutManager = LinearLayoutManager(requireContext())

        fetchPlacesData()

        binding.searchText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable?) {
                val query = editable.toString().trim()
                performSearch(query)
            }

            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private val itemClickListener = object : ItemClickListener {
        override fun onItemClick(placeId: String) {
            val selectedPlace = adapter.getPlaceByPlaceId(placeId)

            if (selectedPlace != null) {
                showPlaceDetailFragment(selectedPlace)
            }
        }
    }

    private fun performSearch(searchTerm: String) {
        placesCollection
            .orderBy("placeName")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val placesList = mutableListOf<PlaceModel>()

                for (document in querySnapshot.documents) {
                    val placeId = document.id
                    val placeModel = document.toObject(PlaceModel::class.java)?.apply {
                        this.placeId = placeId
                    }

                    if (placeModel != null && placeModel.placeName.contains(searchTerm, ignoreCase = true)) {
                        placesList.add(placeModel)
                    }
                }

                placesList.sortWith(Comparator { place1, place2 ->
                    turkishCollator.compare(place1.placeName, place2.placeName)
                })

                adapter.setPlacesList(placesList)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Veri çekme işlemi başarısız. Hata: $exception")
            }
    }

    private fun fetchPlacesData() {
        placesCollection
            .orderBy("placeName", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val placesList = mutableListOf<PlaceModel>()

                for (document in querySnapshot.documents) {
                    val placeId = document.id
                    val placeModel = document.toObject(PlaceModel::class.java)?.apply {
                        this.placeId = placeId
                    }

                    if (placeModel != null) {
                        placesList.add(placeModel)
                    }
                }

                placesList.sortWith(Comparator { place1, place2 ->
                    turkishCollator.compare(place1.placeName, place2.placeName)
                })

                adapter.setPlacesList(placesList)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Veri çekme işlemi başarısız. Hata: $exception")
            }
    }

    private fun showPlaceDetailFragment(selectedPlace: PlaceModel) {
        val tag = "PlacesDetailFragment"

        val existingFragment = parentFragmentManager.findFragmentByTag(tag)

        if (existingFragment == null) {
            val fragment = PlacesDetailFragment()
            val bundle = Bundle()
            bundle.putSerializable("selectedPlace", selectedPlace)
            fragment.arguments = bundle
            fragment.show(parentFragmentManager, tag)
        }
    }


    override fun onFilterResult(places: List<PlaceModel>) {
        Log.d("Filter", "onFilterResult is called with ${places.size} places.")
        adapter.setPlacesList(places)
        adapter.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}