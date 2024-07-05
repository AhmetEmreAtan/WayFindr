package com.example.wayfindr.places

import android.R
import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.LinearLayoutManager
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
    private var selectedPricingType = ""
    private var selectedDistrict = ""

    private lateinit var categoryList: MutableList<PlacesCategories>
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

        setupDistricts()

        binding.resetButton.setOnClickListener {
            resetFilters()
        }

        binding.filterButton.setOnClickListener {
            placesFilterResults()
            dismiss()
        }

        setupRecyclerCategories()

    }

    private fun placesFilterResults() {
        val query = placesCollection
            .whereEqualTo("placesPrice", selectedPricingType)
            .whereEqualTo("placesCategories", selectedCategory)
            .whereEqualTo("placesDescription", selectedDistrict)

        query.get()
            .addOnSuccessListener { documents ->
                val places = mutableListOf<PlaceModel>()
                for (document in documents) {
                    val place = document.toObject(PlaceModel::class.java)
                    places.add(place)
                    Log.d(TAG, "${document.id} => ${document.data}")
                }
                notifyFilterResults(places)
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }
    }

    private fun setupRecyclerCategories() {
        categoryList = getCategoriesListFromArray()
        val adapter = PlacesCategoriesAdapter(categoryList)
        binding.categoryRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.categoryRecyclerView.adapter = adapter

        adapter.setOnItemClickListener { category ->
            selectedCategory = category.name
            Log.d("Category", "Selected Category: $selectedCategory")

        }
    }

    private fun getCategoriesListFromArray(): MutableList<PlacesCategories> {
        val categoriesList = mutableListOf<PlacesCategories>()
        val rawArray = resources.getStringArray(com.example.wayfindr.R.array.array_categories)
        val imageResIds =
            resources.obtainTypedArray(com.example.wayfindr.R.array.array_category_images)

        rawArray.forEachIndexed { index, category ->
            val imageResId = imageResIds.getResourceId(index, -1)
            categoriesList.add(PlacesCategories(imageResId, category))
        }

        imageResIds.recycle()
        return categoriesList
    }


    private fun resetFilters() {
        binding.spinnerDistricts.setSelection(0)
        binding.radioGroupPricing.clearCheck()
        selectedCategory = ""
        selectedPricingType = ""
        selectedDistrict = ""

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

    private fun notifyFilterResults(places: List<PlaceModel>) {
        filterResultListener?.onFilterResult(places)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}