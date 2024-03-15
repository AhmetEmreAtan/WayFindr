package com.example.wayfindr

import PopularAdapter
import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wayfindr.databinding.FragmentHomeBinding
import com.example.wayfindr.databinding.FragmentPlacesBinding
import com.example.wayfindr.home.CategoryDetailFragment
import com.example.wayfindr.places.PlaceModel
import com.example.wayfindr.places.PlacesDetailFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
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


        val recyclerViewPopular: RecyclerView? = view.findViewById(R.id.recyclerViewPopular)

        recyclerViewPopular?.layoutManager = LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false)
        recyclerViewPopular?.adapter = adapter

        if (recyclerViewPopular != null) {
            recyclerViewPopular.layoutManager = LinearLayoutManager(context)
            recyclerViewPopular.adapter = adapter
        } else {
            Log.e("Home", "recyclerView not found in layout")
        }

        fetchPlacesData()
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
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getRandomItems(totalItems: Int, itemCount: Int): List<Int> {
        val shuffledIndices = (0 until totalItems).shuffled()
        return shuffledIndices.take(itemCount)
    }

    private fun openCategoryDetailFragment(category: String) {
        val fragment = CategoryDetailFragment.newInstance(category)
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.home_fragment_FL, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }
}
