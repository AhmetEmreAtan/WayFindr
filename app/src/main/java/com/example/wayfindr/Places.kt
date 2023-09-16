package com.example.wayfindr

import PlacesAdapter
import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wayfindr.places.ItemClickListener
import com.example.wayfindr.places.PlaceModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class Places : Fragment() {

    val databaseReference = FirebaseDatabase.getInstance().getReference("places")

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
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val placesList= mutableListOf<PlaceModel>()

                for (childSnapshot in snapshot.children) {
                    val placeName = childSnapshot.child("placeName").getValue(String::class.java)
                    val placeDescription = childSnapshot.child("placeDescription").getValue(String::class.java)
                    val placeImage = childSnapshot.child("placeImage").getValue(String::class.java)

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

            override fun onCancelled(error: DatabaseError) {
                Log.e(ContentValues.TAG, "Veri çekme işlemi başarısız. Hata: ${error.message}")
            }
        })



    }


}