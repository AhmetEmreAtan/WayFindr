package com.example.wayfindr

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.wayfindr.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class FilterBottomSheetFragment : Fragment(){


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_filter_bottom_sheet_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val seekBarLocation: SeekBar = view.findViewById(R.id.seekBarLocation)
        val textViewSelectedDistance: TextView = view.findViewById(R.id.textViewSelectedDistance)

        seekBarLocation.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            var selectedProgress = 0

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Progress değiştiğinde burada yapılacak işlemler
                selectedProgress = progress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Kullanıcı seekbar'a dokunmaya başladığında yapılacak işlemler
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Kullanıcı seekbar'dan elini çektiğinde yapılacak işlemler
                val selectedDistance = "$selectedProgress km"
                textViewSelectedDistance.text = "Seçilen Mesafe: $selectedDistance"
            }
        })


    }
}