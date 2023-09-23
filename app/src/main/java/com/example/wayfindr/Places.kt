package com.example.wayfindr

import PlacesAdapter
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wayfindr.places.ItemClickListener
import com.example.wayfindr.places.PlaceModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.Collator
import java.util.Locale


class Places : Fragment() {

    val db = FirebaseFirestore.getInstance()
    val placesCollection = db.collection("places")
    val turkishCollator = Collator.getInstance(Locale("tr", "TR"))

    private val itemClickListener = object : ItemClickListener {
        override fun onItemClick(position: Int) {

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_places, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val filterImage: ImageView? = view.findViewById(R.id.filterImage)
        filterImage?.setOnClickListener {
            val filterBottomSheetFragment = FilterBottomSheetFragment()
            activity?.supportFragmentManager?.beginTransaction()
                ?.replace(R.id.fragmentPlaces, filterBottomSheetFragment)
                ?.commit()
        }

        /*val resetButton: Button? = view.findViewById(R.id.resetButton)
        resetButton?.setOnClickListener {
            activity?.supportFragmentManager?.beginTransaction()?.remove(this)?.commit()
        }*/

        //Firebase Verlerini Çekme İşlemleri
        fetchPlacesData()

        // SearchEditText
        val searchEditText = view?.findViewById<EditText>(R.id.searchText)
        searchEditText?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable?) {
                val query = editable.toString().trim()
                performSearch(query)
            }

            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {

            }
        })

    }

    private fun performSearch(searchTerm: String) {

        placesCollection
            .orderBy("placeName")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val placesList = mutableListOf<PlaceModel>()

                for (document in querySnapshot.documents) {
                    val placeName = document.getString("placeName")
                    val placeDescription = document.getString("placeDescription")
                    val placeImage = document.getString("placeImage")

                    if (placeName != null && placeDescription != null && placeImage != null) {
                        // Belge içinde arama terimini içeriyorsa ekle
                        if (placeName.contains(searchTerm, ignoreCase = true)) {
                            val place = PlaceModel(placeName, placeDescription, placeImage)
                            placesList.add(place)
                        }
                    }
                }

                placesList.sortWith(Comparator { place1, place2 ->
                    turkishCollator.compare(place1.placeName, place2.placeName)
                })

                val recyclerView = requireView().findViewById<RecyclerView>(R.id.recyclerViewPlaces)
                val adapter = PlacesAdapter(placesList, itemClickListener)
                recyclerView.adapter = adapter
                recyclerView.layoutManager = LinearLayoutManager(requireContext())
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
                    val placeName = document.getString("placeName")
                    val placeDescription = document.getString("placeDescription")
                    val placeImage = document.getString("placeImage")

                    if (placeName != null && placeDescription != null && placeImage != null) {
                        val place = PlaceModel(placeName, placeDescription, placeImage)
                        placesList.add(place)
                    }
                }

                placesList.sortWith(Comparator { place1, place2 ->
                    turkishCollator.compare(place1.placeName, place2.placeName)
                })

                val recyclerView = requireView().findViewById<RecyclerView>(R.id.recyclerViewPlaces)
                val adapter = PlacesAdapter(placesList, itemClickListener)
                recyclerView.adapter = adapter
                recyclerView.layoutManager = LinearLayoutManager(requireContext())
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Veri çekme işlemi başarısız. Hata: $exception")
            }
    }
}