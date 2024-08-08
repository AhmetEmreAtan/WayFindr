package com.example.wayfindr.search

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.wayfindr.R
import com.example.wayfindr.memories.MemoriesDetail

class FriendsMemoriesAdapter(private val context: Context, private val memoriesList: List<Memory>) :
    RecyclerView.Adapter<FriendsMemoriesAdapter.FriendsMemoriesViewHolder>() {

    inner class FriendsMemoriesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val memoryImageView: ImageView = itemView.findViewById(R.id.memoryImageView)
        val memoryLocationTextView: TextView = itemView.findViewById(R.id.memoryLocationTextView)
        val memoryCommentTextView: TextView = itemView.findViewById(R.id.memoryCommentTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendsMemoriesViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_memory, parent, false)
        return FriendsMemoriesViewHolder(view)
    }

    override fun onBindViewHolder(holder: FriendsMemoriesViewHolder, position: Int) {
        val memory = memoriesList[position]
        holder.memoryLocationTextView.text = memory.photoLocation
        holder.memoryCommentTextView.text = memory.userComment
        Glide.with(context)
            .load(memory.imageUrl)
            .placeholder(R.drawable.placeholder_image)
            .error(R.drawable.error_image)
            .into(holder.memoryImageView)

        holder.itemView.setOnClickListener {
            Log.d("MemoriesDetail", "Received memoryId: ${memory.memoryId}, userId: ${memory.userId}")

            val intent = MemoriesDetail.newIntent(
                context = it.context,
                memoryId = memory.memoryId,
                userComment = memory.userComment,
                photoLocation = memory.photoLocation,
                imageUrl = memory.imageUrl,
                userProfileImageUrl = memory.userProfileImageUrl,
                username = memory.username,
                userId = memory.userId
            )
            it.context.startActivity(intent)
        }
    }


    override fun getItemCount(): Int {
        return memoriesList.size
    }
}