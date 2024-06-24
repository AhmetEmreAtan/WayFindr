package com.example.wayfindr.home.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.wayfindr.R
import com.example.wayfindr.home.models.Message
import com.google.firebase.Timestamp

class ChatMessagesAdapter(private val messages: List<Message>) :
    RecyclerView.Adapter<ChatMessagesAdapter.MessageViewHolder>() {

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewMessage: TextView = itemView.findViewById(R.id.textViewMessage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        holder.textViewMessage.text = message.message

        val timestamp = message.timestamp as? Timestamp
        if (timestamp != null) {
            holder.textViewMessage.append("\n" + timestamp.toDate().toString())
        }
    }

    override fun getItemCount(): Int = messages.size
}
