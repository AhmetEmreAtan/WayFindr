package com.example.wayfindr.home.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.wayfindr.R
import com.example.wayfindr.home.models.EventModel

class EventsAdapter(private var eventsList: List<EventModel>, private val listener: OnItemClickListener) :
    RecyclerView.Adapter<EventsAdapter.EventViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(event: EventModel)
    }

    inner class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val eventsName: TextView = itemView.findViewById(R.id.eventName)
        val eventsLocation: TextView = itemView.findViewById(R.id.eventLocation)
        val eventsImage: ImageView = itemView.findViewById(R.id.eventImage)

        init {
            itemView.setOnClickListener {
                listener.onItemClick(eventsList[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_event, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = eventsList[position]
        holder.eventsName.text = event.eventsName
        holder.eventsLocation.text = event.eventsLocation
        Glide.with(holder.itemView.context)
            .load(event.eventsImage)
            .fitCenter()
            .into(holder.eventsImage)
    }

    override fun getItemCount(): Int {
        return eventsList.size
    }

    fun setEventsList(newEventsList: List<EventModel>) {
        eventsList = newEventsList
        notifyDataSetChanged()
    }

    fun getEventByEventId(eventId: String): EventModel? {
        return eventsList.find { it.eventId == eventId }
    }
}