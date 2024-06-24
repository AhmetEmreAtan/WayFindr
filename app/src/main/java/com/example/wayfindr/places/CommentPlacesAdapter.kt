package com.example.wayfindr.places

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.wayfindr.R

class CommentPlacesAdapter(private val comments: List<CommentPlacesModel>) :
    RecyclerView.Adapter<CommentPlacesAdapter.CommentViewHolder>() {

    inner class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val commentTextView: TextView = itemView.findViewById(R.id.commentText)
        val commentUser: TextView = itemView.findViewById(R.id.commentUser)
        val commentUserName: TextView = itemView.findViewById(R.id.commentUserName)
        val commentProfile: ImageView = itemView.findViewById(R.id.commentProfile)

        fun bind(comment: CommentPlacesModel) {
            if (comment.commentText != null) {
                commentTextView.text = comment.commentText
            }
            if (comment.user?.firstName != null) {
                commentUser.text = comment.user?.firstName
            }
            if (comment.user?.username != null) {
                commentUserName.text = comment.user?.username
            }
            if (comment.user?.profileImageUrl != null) {
                Glide.with(itemView)
                    .load(comment.user?.profileImageUrl)
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.error_image)
                    .into(commentProfile)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = comments[position]
        holder.bind(comment)
    }

    override fun getItemCount(): Int {
        return comments.size
    }
}