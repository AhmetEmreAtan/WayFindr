package com.example.wayfindr.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wayfindr.Home
import com.example.wayfindr.R
import com.example.wayfindr.home.adapters.CategoryAdapter
import com.example.wayfindr.home.models.CategoryDataModel
import com.example.wayfindr.places.PlaceModel
import com.example.wayfindr.places.PlacesDetailFragment
import com.google.firebase.firestore.FirebaseFirestore

class CategoryDetailFragment : Fragment() {

    private lateinit var category: String
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CategoryAdapter

    companion object {
        fun newInstance(category: String): CategoryDetailFragment {
            val fragment = CategoryDetailFragment()
            val args = Bundle()
            args.putString("CATEGORY", category)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_category_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val categoryName = arguments?.getString("CATEGORY") ?: "Category"
        val categoryTitleTextView = view.findViewById<TextView>(R.id.categorysTitle)
        categoryTitleTextView.text = categoryName

        recyclerView = view.findViewById(R.id.recyclerView_category)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val closeButton: ImageButton = view.findViewById(R.id.category_fragment_close)
        closeButton.setOnClickListener {
            val homeFragment = Home()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, homeFragment)
                .commit()
        }

        fetchCategoryData()
    }

    private fun fetchCategoryData() {
        category = arguments?.getString("CATEGORY") ?: ""
        Log.d("CategoryDetailFragment", "Fetching data for category: $category")

        val db = FirebaseFirestore.getInstance()
        db.collection("places")
            .whereEqualTo("placeCategories", category)
            .get()
            .addOnSuccessListener { documents ->
                Log.d("FirestoreQuery", "Data received: $documents")
                val placeList = mutableListOf<CategoryDataModel>()

                for (document in documents) {
                    val placeId = document.getString("placeId") ?: ""
                    val placeName = document.getString("placeName") ?: ""
                    val placeDescription = document.getString("placeDescription") ?: ""
                    val placeImage = document.getString("placeImage") ?: ""
                    val placeAddress = document.getString("placeAddress") ?: ""
                    val placeLocation = document.getString("placeLocation") ?: ""
                    val placeOpeningHours = document.getString("placeOpeningHours") ?: ""
                    val placeDetails = document.getString("placeDetails") ?: ""
                    val placePrice = document.getString("placePrice") ?: ""
                    val isFavorite = document.getBoolean("isFavorite") ?: false

                    val place = CategoryDataModel(
                        placeId, placeName, placeDescription, placeImage,
                        category, placeAddress, placeLocation, placeOpeningHours,
                        placeDetails, placePrice, isFavorite
                    )
                    placeList.add(place)
                }

                setupRecyclerView(placeList)
            }
            .addOnFailureListener { exception ->
                Log.e("FirestoreQuery", "Error fetching data: ", exception)
                val fragmentManager = requireActivity().supportFragmentManager
                fragmentManager.beginTransaction().replace(R.id.home_fragment_FL, Home()).commit()
            }
    }

    private fun setupRecyclerView(dataList: List<CategoryDataModel>) {
        adapter = CategoryAdapter(dataList, object : CategoryAdapter.OnItemClickListener {
            override fun onItemClick(place: CategoryDataModel) {
                showPlaceDetailFragment(place)
            }
        })
        recyclerView.adapter = adapter
    }

    private fun showPlaceDetailFragment(selectedPlace: CategoryDataModel) {
        val fragment = PlacesDetailFragment()
        val bundle = Bundle()
        bundle.putSerializable("placeId", selectedPlace.placeId)
        fragment.arguments = bundle

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, fragment)
            .addToBackStack(null)
            .commit()
    }
}
