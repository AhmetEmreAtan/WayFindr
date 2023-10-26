package com.example.wayfindr

import PlaceModel
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
import com.example.wayfindr.places.ItemClickListener
import com.example.wayfindr.places.PlacesDetailFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.Collator
import java.util.Locale

class Places : Fragment() {

    private lateinit var binding: FragmentPlacesBinding
    private val db = FirebaseFirestore.getInstance()
    private val placesCollection = db.collection("places")
    private val turkishCollator = Collator.getInstance(Locale("tr", "TR"))

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var adapter: PlacesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPlacesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()
        adapter = PlacesAdapter(emptyList(), itemClickListener, firebaseAuth)

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

        binding.filterImage.setOnClickListener {
            val filterBottomSheetFragment = FilterBottomSheetFragment()
            filterBottomSheetFragment.show(parentFragmentManager, filterBottomSheetFragment.tag)
        }
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
        val fragment = PlacesDetailFragment()
        val bundle = Bundle()
        bundle.putParcelable("selectedPlace", selectedPlace)
        fragment.arguments = bundle
        fragment.show(parentFragmentManager, fragment.tag)
    }
}