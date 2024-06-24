package com.example.wayfindr.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.wayfindr.R
import com.example.wayfindr.home.models.EventModel

class EventsDetailFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_events_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val selectedEvent = arguments?.getSerializable("selectedEvent") as EventModel?

        selectedEvent?.let {
            view.findViewById<TextView>(R.id.eventNameDetail).text = it.eventsName
            view.findViewById<TextView>(R.id.eventLocationDetail).text = it.eventsLocation
            view.findViewById<TextView>(R.id.eventDetail).text = it.eventsDetail // Yeni alan
            val eventImageView = view.findViewById<ImageView>(R.id.eventImageDetail)
            Glide.with(this).load(it.eventsImage).into(eventImageView)
        }
    }
}