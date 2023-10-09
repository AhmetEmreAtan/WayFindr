package com.example.wayfindr

import PlaceModel
import PlacesAdapter
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
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
import com.example.wayfindr.places.PlacesDetailFragment
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.Collator
import java.util.Locale


class Places : Fragment() {

    private val db = FirebaseFirestore.getInstance()
    private val placesCollection = db.collection("places")
    private val turkishCollator = Collator.getInstance(Locale("tr", "TR"))

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PlacesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_places, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewPlaces)
        adapter = PlacesAdapter(emptyList(), itemClickListener,this)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fetchPlacesData()
        

        adapter = PlacesAdapter(emptyList(), itemClickListener,this)
        recyclerView.adapter = adapter

        val searchEditText = view.findViewById<EditText>(R.id.searchText)
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable?) {
                val query = editable.toString().trim()
                performSearch(query)
            }

            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {}

        })

        val filterImage: ImageView? = view.findViewById(R.id.filterImage)
        filterImage?.setOnClickListener {
            val filterBottomSheetFragment = FilterBottomSheetFragment()
            filterBottomSheetFragment.show(parentFragmentManager, filterBottomSheetFragment.tag)
        }
    }

    private val itemClickListener = object : ItemClickListener {
        override fun onItemClick(placeId: String) {
            val selectedPlace = adapter.getPlaceByPlaceId(placeId)

            if (selectedPlace != null) {
                showPlaceDetailFragment(selectedPlace)
            } else {
                // Handle the case where selectedPlace is null, if needed
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
                    // Belge ID'sini al
                    val placeId = document.id

                    // Belgeyi PlaceModel'e çevir ve placeId'yi set et
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
                    // Belge ID'sini al
                    val placeId = document.id

                    // Belgeyi PlaceModel'e çevir ve placeId'yi set et
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

    fun updateFirestoreFavorites(placeId: String) {
        val db = FirebaseFirestore.getInstance()

        // "favorites" koleksiyonunu referans al
        val favoritesCollection = db.collection("favorites")

        // Yeni bir belge oluştur ve favori mekanı eklemek için kullan
        val favoriteDocument = favoritesCollection.document(placeId)
        favoriteDocument.set(mapOf("placeId" to placeId))
            .addOnSuccessListener {
                Log.d(TAG, "Favori mekan başarıyla eklendi")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Favori mekan eklenirken hata oluştu", e)
            }

    }

    fun removeFromFirestoreFavorites(placeId: String) {
        // Firestore veritabanına erişim sağla
        val db = FirebaseFirestore.getInstance()

        // "favorites" koleksiyonunu referans al
        val favoritesCollection = db.collection("favorites")

        // Belirli placeId'ye sahip olan favori mekanı kaldır
        favoritesCollection.document(placeId)
            .delete()
            .addOnSuccessListener {
                Log.d(TAG, "Favori mekan başarıyla kaldırıldı")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Favori mekan kaldırılırken hata oluştu", e)
            }

    }


}