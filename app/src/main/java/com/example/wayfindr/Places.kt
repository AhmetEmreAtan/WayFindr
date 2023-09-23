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
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wayfindr.places.ItemClickListener
import com.example.wayfindr.places.PlaceModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore


class Places : Fragment() {

    val databaseReference = FirebaseDatabase.getInstance().getReference("places")

    val db = FirebaseFirestore.getInstance()
    val placesCollection = db.collection("places")

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
        placesCollection
            .orderBy("placeName") // placeName alanına göre sıralama
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

                val recyclerView = requireView().findViewById<RecyclerView>(R.id.recyclerViewPlaces)
                val adapter = PlacesAdapter(placesList, itemClickListener)
                recyclerView.adapter = adapter
                recyclerView.layoutManager = LinearLayoutManager(requireContext())
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Veri çekme işlemi başarısız. Hata: $exception")
            }

        // SearchEditText
        val searchEditText = view?.findViewById<EditText>(R.id.searchText)
        searchEditText?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable?) {

                val query = editable.toString().trim()
                searchInFirestore(query)
            }

            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {

            }
        })


    }


    private fun searchInFirestore(query: String) {
    val placesList = mutableListOf<PlaceModel>()
    val db = FirebaseFirestore.getInstance()
    val placesCollection = db.collection("places")

    placesCollection
        .orderBy("placeName")
        .startAt(query)
        .endAt(query + "\uf8ff")
        .get()
        .addOnSuccessListener { querySnapshot ->
            for (document in querySnapshot.documents) {
                val placeName = document.getString("placeName")
                val placeDescription = document.getString("placeDescription")
                val placeImage = document.getString("placeImage")

                if (placeName != null && placeDescription != null && placeImage != null) {
                    val place = PlaceModel(placeName, placeDescription, placeImage)
                    placesList.add(place)
                }
            }

            val recyclerView = requireView().findViewById<RecyclerView>(R.id.recyclerViewPlaces)
            val adapter = PlacesAdapter(placesList, itemClickListener)
            recyclerView.adapter = adapter
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
        }
        .addOnFailureListener { exception ->
            Log.e(ContentValues.TAG, "Veri çekme işlemi başarısız. Hata: $exception")
        }
    }




}