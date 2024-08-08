package com.example.wayfindr.home

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.wayfindr.R
import com.example.wayfindr.databinding.FragmentFilteredResultsBottomSheetBinding
import com.example.wayfindr.home.adapters.FilteredAdapter
import com.example.wayfindr.places.PlaceModel
import com.example.wayfindr.places.PlacesDetailFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.firestore.FirebaseFirestore
import java.text.Collator
import java.util.Locale

class FilteredResultsBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentFilteredResultsBottomSheetBinding? = null
    private val binding get() = _binding!!
    private lateinit var filteredAdapter: FilteredAdapter
    private var onDismissCallback: (() -> Unit)? = null
    private val db = FirebaseFirestore.getInstance()

    private lateinit var listener: FilteredAdapter.OnItemClickListener

    companion object {
        fun newInstance(listener: FilteredAdapter.OnItemClickListener): FilteredResultsBottomSheetFragment {
            val fragment = FilteredResultsBottomSheetFragment()
            fragment.listener = listener
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFilteredResultsBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listener = object : FilteredAdapter.OnItemClickListener {
            override fun onItemClick(placeId: String) {
                val selectedPlace = filteredAdapter.getPlaceByPlaceId(placeId)
                if (selectedPlace != null) {
                    showResultDetailFragment(selectedPlace)
                }
            }
        }

        filteredAdapter = FilteredAdapter(emptyList(), listener)
        binding.recyclerViewFilteredResults.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewFilteredResults.adapter = filteredAdapter
    }

    private fun showResultDetailFragment(selectedPlace: PlaceModel) {
        val fragment = PlacesDetailFragment()
        val bundle = Bundle()
        bundle.putSerializable("selectedPlace", selectedPlace)
        fragment.arguments = bundle

        dismiss()

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, fragment)
            .addToBackStack(null)
            .commit()
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener {
            val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let { view ->
                val behavior = BottomSheetBehavior.from(view)
                behavior.peekHeight = resources.displayMetrics.heightPixels / 2
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
        dialog.setOnDismissListener {
            onDismissCallback?.invoke()
        }
        return dialog
    }

    fun setOnDismissCallback(callback: () -> Unit) {
        onDismissCallback = callback
    }

    fun updateResults(newResults: List<PlaceModel>) {
        filteredAdapter.setPlacesList(newResults)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
