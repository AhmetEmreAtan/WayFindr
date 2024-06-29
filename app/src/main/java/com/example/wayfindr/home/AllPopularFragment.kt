package com.example.wayfindr.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wayfindr.R
import com.example.wayfindr.home.adapters.AllPopularAdapter
import com.example.wayfindr.home.models.AllPopular
import com.example.wayfindr.places.PlacesDetailFragment
import com.google.firebase.firestore.FirebaseFirestore

class AllPopularFragment : Fragment(), AllPopularAdapter.OnItemClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AllPopularAdapter
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_all_popular, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.recyclerView_all_popular)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = AllPopularAdapter(emptyList(), this)
        recyclerView.adapter = adapter
        fetchPopularData()

        val closeButton = view.findViewById<ImageButton>(R.id.allPopularBackBtn)
        closeButton.setOnClickListener {
            Log.d("AllPopularFragment", "Close button clicked")
            parentFragmentManager.popBackStack()
        }
    }

    private fun fetchPopularData() {
        db.collection("popular")
            .get()
            .addOnSuccessListener { documents ->
                val popularList = mutableListOf<AllPopular>()
                for (document in documents) {
                    val placeId = document.getString("placeId") ?: ""
                    val placeName = document.getString("placeName") ?: ""
                    val placeLocation = document.getString("placeLocation") ?: ""
                    val placeImage = document.getString("placeImage") ?: ""

                    val popular = AllPopular(
                        placeId, placeName, placeLocation, placeImage
                    )
                    popularList.add(popular)
                }
                adapter.setPopularList(popularList)
            }
            .addOnFailureListener { exception ->
                Log.e("FirestoreQuery", "Error fetching data: ", exception)
                Toast.makeText(requireContext(), "Error fetching data.", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onItemClick(popular: AllPopular) {
        showPlaceDetailFragment(popular)
    }

    private fun showPlaceDetailFragment(selectedPlace: AllPopular) {
        val fragment = PlacesDetailFragment()
        val bundle = Bundle()
        bundle.putString("placeId", selectedPlace.placeId)
        fragment.arguments = bundle

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, fragment)
            .addToBackStack(null)
            .commit()
    }
}