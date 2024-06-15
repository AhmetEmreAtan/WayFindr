package com.example.wayfindr.home.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.wayfindr.R
import com.example.wayfindr.databinding.ItemNotificationBinding
import com.example.wayfindr.databinding.ItemFriendAddedBinding
import com.example.wayfindr.home.models.FriendRequest
import com.google.firebase.firestore.FirebaseFirestore

class NotificationAdapter(
    private var notifications: MutableList<FriendRequest>,
    private val onAcceptClickListener: (String, String) -> Unit,
    private val onRejectClickListener: (String) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val NOTIFICATION_VIEW_TYPE = 0
    private val FRIEND_ADDED_VIEW_TYPE = 1

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

                        binding.acceptButton.setOnClickListener {
                            onAcceptClickListener(notification.id, notification.fromUserId)
                        }
                        binding.rejectButton.setOnClickListener {
                            onRejectClickListener(notification.id)
                        }
                    }
                }
        }
    }

    inner class FriendAddedViewHolder(private val binding: ItemFriendAddedBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(notification: FriendRequest) {
            FirebaseFirestore.getInstance().collection("users")
                .document(notification.fromUserId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val profileImageUrl = document.getString("profileImageUrl")
                        Glide.with(binding.root.context)
                            .load(profileImageUrl)
                            .placeholder(R.drawable.placeholder_image)
                            .error(R.drawable.error_image)
                            .circleCrop()
                            .into(binding.friendProfileImage)
                    }
                }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            FRIEND_ADDED_VIEW_TYPE -> {
                val binding = ItemFriendAddedBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false)
                FriendAddedViewHolder(binding)
            }
            else -> {
                val binding = ItemNotificationBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false)
                NotificationViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is NotificationViewHolder -> holder.bind(notifications[position])
            is FriendAddedViewHolder -> holder.bind(notifications[position])
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (notifications[position].status == "accepted") {
            FRIEND_ADDED_VIEW_TYPE
        } else {
            NOTIFICATION_VIEW_TYPE
        }
    }

    override fun getItemCount(): Int = notifications.size

    fun updateNotifications(newNotifications: List<FriendRequest>) {
        notifications.clear()
        notifications.addAll(newNotifications)
        Log.d("NotificationAdapter", "Updated list with ${notifications.size} items")
        notifyDataSetChanged()
    }

    fun updateItemToFriendAdded(requestId: String) {
        val position = notifications.indexOfFirst { it.id == requestId }
        if (position != -1) {
            notifications[position].status = "accepted"
            notifyItemChanged(position)
        }
    }

    fun removeNotification(requestId: String) {
        val position = notifications.indexOfFirst { it.id == requestId }
        if (position != -1) {
            notifications.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}
