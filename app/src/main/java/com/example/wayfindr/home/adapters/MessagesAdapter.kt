package com.example.wayfindr.home.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.wayfindr.R
import com.example.wayfindr.home.models.ChatUser

class MessagesAdapter(
    private val chatUsers: List<ChatUser>,
    private val onItemClick: (ChatUser) -> Unit
) : RecyclerView.Adapter<MessagesAdapter.MessageViewHolder>() {

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageViewProfile: ImageView = itemView.findViewById(R.id.messagesProfileImage)
        val textViewUserName: TextView = itemView.findViewById(R.id.messagesUserName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_messages_notification, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val chatUser = chatUsers[position]
        holder.textViewUserName.text = chatUser.username
        Glide.with(holder.itemView.context)
            .load(chatUser.profileImageUrl)
            .placeholder(R.drawable.placeholder_image)
            .into(holder.imageViewProfile)

        holder.itemView.setOnClickListener {
            onItemClick(chatUser)
        }
    }

    override fun getItemCount(): Int = chatUsers.size
}
