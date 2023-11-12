package com.example.wayfindr.places

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.DialogFragment
import com.example.wayfindr.R

class AddPlacesFragment : DialogFragment() {

    private lateinit var addPlacesBackBtn : ImageButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.add_places_fragment, container, false)


        addPlacesBackBtn = view.findViewById(R.id.addPlaces_back_btn)
        addPlacesBackBtn.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right)
                .remove(this)
                .addToBackStack(null)
                .commit()
        }

        return view
    }
}

