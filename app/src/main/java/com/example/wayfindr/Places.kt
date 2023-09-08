package com.example.wayfindr

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView


class Places : Fragment() {


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

        val resetButton: Button? = view.findViewById(R.id.resetButton)
        resetButton?.setOnClickListener {
            activity?.supportFragmentManager?.beginTransaction()?.remove(this)?.commit()
        }


    }
}