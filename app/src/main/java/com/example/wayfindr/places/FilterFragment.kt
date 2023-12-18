package com.example.wayfindr.places

import android.Manifest
import android.R
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SeekBar
import androidx.core.content.ContextCompat
import com.example.wayfindr.databinding.FragmentFilterFragmentBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore

class FilterFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentFilterFragmentBinding? = null
    private val binding get() = _binding!!

    var filterResultListener: FilterResultListener? = null

    private val firestore = FirebaseFirestore.getInstance()
    private val placesCollection = firestore.collection("places")

    private var selectedCategory = ""
    private var selectedPricingType = ""
    private var selectedDistrict = ""
    private var selectedDistance: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFilterFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRadioButton()

        setupSeekBar()

        setupDistricts()

        selectedCategories()

        binding.resetButton.setOnClickListener {
            resetFilters()
        }

        binding.filterButton.setOnClickListener {
            fetchPlacesWithMultipleFilters()
            dismiss()
        }
    }

    private fun fetchPlacesByField(fieldName: String, value: String) {
        placesCollection.whereEqualTo(fieldName, value)
            .get()
            .addOnSuccessListener { documents ->
                val matchingPlaces = mutableListOf<PlaceModel>()

                for (document in documents) {
                    val place = document.toObject(PlaceModel::class.java)
                    matchingPlaces.add(place)
                    Log.d(
                        "Firestore",
                        "Place Name: ${place.placeName}, Field Name: $fieldName, Value: $value"
                    )
                }
                notifyFilterResults(matchingPlaces)
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error getting places: $exception")
            }
    }

    private fun fetchAllPlaces() {
        placesCollection.get()
            .addOnSuccessListener { documents ->
                val allPlaces = mutableListOf<PlaceModel>()
                for (document in documents) {
                    val place = document.toObject(PlaceModel::class.java)
                    allPlaces.add(place)
                }
                notifyFilterResults(allPlaces)
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error getting all places: $exception")
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

    private fun fetchPlacesWithMultipleFilters() {
        if (selectedCategory.isEmpty() && binding.spinnerDistricts.selectedItemPosition == 0
            && selectedPricingType.isEmpty()
        ) {
            fetchAllPlaces()
        } else if (selectedCategory.isNotEmpty() && binding.spinnerDistricts.selectedItemPosition == 0
            && selectedPricingType.isEmpty()
        ) {
            fetchPlacesByField("placeCategories", selectedCategory)
        } else if (selectedCategory.isEmpty() && selectedDistrict.isNotEmpty() && selectedPricingType.isEmpty()

        ) {
            fetchPlacesByField("placeDescription", selectedDistrict)
        } else if (selectedCategory.isEmpty() && binding.spinnerDistricts.selectedItemPosition == 0
            && selectedPricingType.isNotEmpty()
        ) {
            fetchPlacesByField("placePrice", selectedPricingType)
        } else {
            fetchAllPlaces()
        }
    }

    private fun setupRadioButton() {
        binding.radioGroupPricing.setOnCheckedChangeListener { group, checkedId ->
            selectedPricingType = when (checkedId) {
                binding.radioButtonPaid.id -> "Ücretli"
                binding.radioButtonFree.id -> "Ücretsiz"
                else -> "Bilinmeyen"
            }

            Log.d("RadioButton", "Selected Pricing Type: $selectedPricingType")
        }
    }

    private fun setupSeekBar() {
        binding.seekBarLocation.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                selectedDistance = seekBar?.progress!!
                binding.textViewSelectedDistance.text = "Seçilen mesafe: $selectedDistance km"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun setupDistricts() {

        setupDistrictSpinner()

        binding.spinnerDistricts.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    selectedDistrict =
                        binding.spinnerDistricts.getItemAtPosition(position).toString()
                    Log.d("Spinner", "Selected District: $selectedDistrict")
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {

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

    private fun selectedCategories() {
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
                        if (selectedCategory == categories[j]) resources.getColor(R.color.darker_gray)
                        else Color.WHITE
                    )
                }

                Log.d("SelectedCategory", "Selected Category: $selectedCategory")
            }
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