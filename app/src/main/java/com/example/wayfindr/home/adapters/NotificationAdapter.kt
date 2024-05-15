package com.example.wayfindr.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.wayfindr.R
import com.example.wayfindr.databinding.ItemNotificationBinding
import com.example.wayfindr.home.models.FriendRequest
import com.google.firebase.firestore.FirebaseFirestore

class NotificationAdapter(private var notifications: List<FriendRequest>) :
    RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    inner class NotificationViewHolder(private val binding: ItemNotificationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(notification: FriendRequest) {
            FirebaseFirestore.getInstance().collection("users")
                .document(notification.fromUserId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val username = document.getString("username")
                        val profileImageUrl = document.getString("profileImageUrl")


                        binding.notificationText.text = "$username size arkadaşlık isteği gönderdi"
                        Glide.with(binding.root.context)
                            .load(profileImageUrl)
                            .placeholder(R.drawable.placeholder_image)
                            .error(R.drawable.error_image)
                            .circleCrop()
                            .into(binding.notificationProfileImage)
                    }
                }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val binding = ItemNotificationBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)
        return NotificationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(notifications[position])
    }

    override fun getItemCount(): Int = notifications.size

    fun updateNotifications(newNotifications: List<FriendRequest>) {
        notifications = newNotifications
        notifyDataSetChanged()
    }
}
