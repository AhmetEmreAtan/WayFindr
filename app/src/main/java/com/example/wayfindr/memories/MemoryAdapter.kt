package com.example.wayfindr.memories

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.wayfindr.R

class MemoryAdapter(private val context: Context, private val memoriesList: List<Memory>) : RecyclerView.Adapter<MemoryAdapter.MemoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemoryViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_memories, parent, false)
        return MemoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: MemoryViewHolder, position: Int) {
        val currentMemory = memoriesList[position]


        holder.userComment.text = currentMemory.userComment
        holder.photoLocation.text = currentMemory.photoLocation


    }

    override fun getItemCount(): Int {
        return memoriesList.size
    }

    inner class MemoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.image_view)
        val userComment: TextView = itemView.findViewById(R.id.userComment)
        val photoLocation: TextView = itemView.findViewById(R.id.photo_location)
    }
}

