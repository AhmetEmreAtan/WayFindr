package com.example.wayfindr.memories

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.wayfindr.R

class FriendsMemoriesAdapter(
    private val context: Context,
    private val memoriesList: List<Memory>
) : RecyclerView.Adapter<FriendsMemoriesAdapter.MemoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemoryViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_memories, parent, false)
        return MemoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: MemoryViewHolder, position: Int) {
        val currentMemory = memoriesList[position]

        //holder.userComment.text = currentMemory.userComment
        //holder.photoLocation.text = currentMemory.photoLocation

        Glide.with(context)
            .load(currentMemory.imageUrl)
            .centerCrop()
            .placeholder(R.drawable.placeholder_image)
            .error(R.drawable.error_image)
            .into(holder.imageView)
    }

    override fun getItemCount(): Int {
        return memoriesList.size
    }

    inner class MemoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.image_view)
        //val userComment: TextView = itemView.findViewById(R.id.userComment)
        //val photoLocation: TextView = itemView.findViewById(R.id.photo_location)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val currentMemory = memoriesList[position]
                    openMemoryDetailActivity(
                        currentMemory.userComment,
                        currentMemory.photoLocation,
                        currentMemory.imageUrl
                    )
                }
            }
        }
    }

    private fun openMemoryDetailActivity(userComment: String, photoLocation: String, imageUrl: String) {
        val intent = Intent(context, MemoriesDetail::class.java).apply {
            putExtra("userComment", userComment)
            putExtra("photoLocation", photoLocation)
            putExtra("imageUrl", imageUrl)
        }
        context.startActivity(intent)
    }
}
