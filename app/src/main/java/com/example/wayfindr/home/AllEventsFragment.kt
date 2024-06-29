package com.example.wayfindr.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wayfindr.R
import com.example.wayfindr.home.adapters.EventsAdapter
import com.example.wayfindr.home.models.EventModel
import com.google.firebase.firestore.FirebaseFirestore

class AllEventsFragment : Fragment(), EventsAdapter.OnItemClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var eventsAdapter: EventsAdapter
    private val db = FirebaseFirestore.getInstance()
    private val eventsCollection = db.collection("events")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_all_events, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerViewAllEvents)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        eventsAdapter = EventsAdapter(emptyList(), this)
        recyclerView.adapter = eventsAdapter

        val closeButton = view.findViewById<ImageButton>(R.id.allEventsBackBtn)
        closeButton.setOnClickListener {
            Log.d("AllPopularFragment", "Close button clicked")
            parentFragmentManager.popBackStack()
        }

        fetchAllEvents()
    }

    private fun fetchAllEvents() {
        eventsCollection.get()
            .addOnSuccessListener { querySnapshot ->
                val eventsList = mutableListOf<EventModel>()
                for (document in querySnapshot.documents) {
                    val eventModel = document.toObject(EventModel::class.java)
                    eventModel?.let { eventsList.add(it) }
                }
                eventsAdapter.updateEvents(eventsList)
            }
            .addOnFailureListener { exception ->
                Log.e("AllEventsFragment", "Error getting documents: ", exception)
                Toast.makeText(requireContext(), "Error fetching events.", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onItemClick(event: EventModel) {
        // Handle item click event
    }
}