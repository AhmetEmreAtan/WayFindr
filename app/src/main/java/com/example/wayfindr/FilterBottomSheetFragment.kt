package com.example.wayfindr

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.TextView
import androidx.cardview.widget.CardView
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
        val resetButton: Button? = view.findViewById(R.id.resetButton)
        val radioGroupPricing: RadioGroup = view.findViewById(R.id.radioGroupPricing)
        val cardView1:CardView=view.findViewById((R.id.cardview1))
        val cardView2:CardView=view.findViewById((R.id.cardview2))
        val cardView3:CardView=view.findViewById((R.id.cardview3))
        val cardView4:CardView=view.findViewById((R.id.cardview4))
        val cardView5:CardView=view.findViewById((R.id.cardview5))

        val cardViews = arrayOf(cardView1, cardView2, cardView3, cardView4, cardView5)
        var selectedCardIndex=-1

        for (i in cardViews.indices) {
            val cardView = cardViews[i]

            cardView.setOnClickListener {

                if (selectedCardIndex != -1) {
                    cardViews[selectedCardIndex].setCardBackgroundColor(Color.WHITE)
                }


                selectedCardIndex = i


                cardView.setCardBackgroundColor(resources.getColor(R.color.gradient))
            }
        }

        seekBarLocation.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            var selectedProgress = 0

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

                selectedProgress = progress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                val selectedDistance = "$selectedProgress km"
                textViewSelectedDistance.text = "Seçilen Mesafe: $selectedDistance"
            }
        })

        resetButton?.setOnClickListener {
            radioGroupPricing.clearCheck()
            seekBarLocation.progress = 0
            textViewSelectedDistance.text = "Seçilen Mesafe: 0 km"
            if (selectedCardIndex != -1) {
                cardViews[selectedCardIndex].setCardBackgroundColor(Color.WHITE)
                selectedCardIndex = -1
            }

        }


    }
}