package com.example.wayfindr.places

import android.R
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SeekBar
import com.example.wayfindr.databinding.FragmentFilterFragmentBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.firestore.FirebaseFirestore

class FilterFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentFilterFragmentBinding? = null
    private val binding get() = _binding!!

    var filterResultListener: FilterResultListener? = null

    private val firestore = FirebaseFirestore.getInstance()
    private val placesCollection = firestore.collection("places")

    private var selectedCategory = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFilterFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.resetButton.setOnClickListener {
            resetFilters()
        }

        binding.radioGroupPricing.setOnCheckedChangeListener { group, checkedId ->
            val selectedPricingType = when (checkedId) {
                binding.radioButtonPaid.id -> "Ücretli"
                binding.radioButtonFree.id -> "Ücretsiz"
                else -> "Bilinmeyen"
            }

            Log.d("RadioButton", "Selected Pricing Type: $selectedPricingType")

            fetchPlacesByPricing(selectedPricingType)
        }

        binding.seekBarLocation.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val seekBarValue = seekBar?.progress
                binding.textViewSelectedDistance.text = "Seçilen mesafe: $seekBarValue km"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        setupDistrictSpinner()

        binding.spinnerDistricts.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val selectedDistrict =
                        binding.spinnerDistricts.getItemAtPosition(position).toString()
                    Log.d("Spinner", "Selected District: $selectedDistrict")
                    fetchPlacesByDistrict(selectedDistrict)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // Hiçbir şey seçilmediğinde yapılacak işlemler
                }
            }

        val categories = arrayOf(
            "Tarihi Müzeler",
            "Bilim ve Endüstri",
            "Antropoloji",
            "Sanat",
            "Özel"
        )

        val cardViews = arrayOf(
            binding.cardview1,
            binding.cardview2,
            binding.cardview3,
            binding.cardview4,
            binding.cardview5
        )

        for (i in cardViews.indices) {
            val cardView = cardViews[i]

            cardView.setOnClickListener {

                selectedCategory = if (selectedCategory != categories[i]) categories[i] else ""

                for (j in cardViews.indices) {
                    val card = cardViews[j]
                    card.setCardBackgroundColor(
                        if (selectedCategory == categories[j]) resources.getColor( R.color.darker_gray)
                        else Color.WHITE
                    )
                }

                Log.d("SelectedCategory", "Selected Category: $selectedCategory")
                fetchPlacesByCategory(selectedCategory)
            }
        }
    }

    private fun setupDistrictSpinner() {
        val districtAdapter = ArrayAdapter(
            requireContext(),
            R.layout.simple_spinner_item,
            resources.getStringArray(com.example.wayfindr.R.array.istanbul_districts)
        )
        districtAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        binding.spinnerDistricts?.adapter = districtAdapter

    }

    private fun fetchPlacesByPricing(pricingType: String) {
        placesCollection.whereEqualTo("placePrice", pricingType)
            .get()
            .addOnSuccessListener { documents ->
                val matchingPlaces = mutableListOf<PlaceModel>()

                for (document in documents) {
                    val place = document.toObject(PlaceModel::class.java)
                    matchingPlaces.add(place)
                    Log.d(
                        "Firestore",
                        "Place Name: ${place.placeName}, Places Price: ${place.placePrice}"
                    )
                }

                notifyFilterResults(matchingPlaces)
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error getting places: $exception")
            }
    }

    private fun fetchPlacesByDistrict(selectedDistrict: String) {
        placesCollection.whereEqualTo("placeDescription", selectedDistrict)
            .get()
            .addOnSuccessListener { documents ->
                val matchingPlaces = mutableListOf<PlaceModel>()

                for (document in documents) {
                    val place = document.toObject(PlaceModel::class.java)
                    matchingPlaces.add(place)
                    Log.d(
                        "Firestore",
                        "Place Name: ${place.placeName}, Places Description: ${place.placeDescription}"
                    )
                }

                notifyFilterResults(matchingPlaces)
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error getting places: $exception")
            }
    }

    private fun fetchPlacesByCategory(category: String) {
        placesCollection.whereEqualTo("placeCategories", category)
            .get()
            .addOnSuccessListener { documents ->
                val matchingPlaces = mutableListOf<PlaceModel>()

                for (document in documents) {
                    val place = document.toObject(PlaceModel::class.java)
                    matchingPlaces.add(place)
                    Log.d("Firestore", "Place Name: ${place.placeName}, Categories: ${place.placeCategories}")
                }

                notifyFilterResults(matchingPlaces)
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error getting places: $exception")
            }
    }

    private fun resetFilters() {
        binding.spinnerDistricts.setSelection(0)
        binding.radioGroupPricing.clearCheck()
        binding.seekBarLocation.progress = 0
        binding.textViewSelectedDistance.text = "0 km"
        selectedCategory = ""
        val cardViews = arrayOf(
            binding.cardview1,
            binding.cardview2,
            binding.cardview3,
            binding.cardview4,
            binding.cardview5
        )
        for (cardView in cardViews) {
            cardView.setCardBackgroundColor(Color.WHITE)
        }
    }

    private fun notifyFilterResults(places: List<PlaceModel>) {
        filterResultListener?.onFilterResult(places)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}