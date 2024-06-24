package com.example.wayfindr.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.wayfindr.Home
import com.example.wayfindr.R
import com.example.wayfindr.databinding.FragmentNotificationBinding
import com.example.wayfindr.home.adapters.NotificationAdapter
import com.example.wayfindr.home.models.FriendRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class NotificationFragment : Fragment() {

    private var _binding: FragmentNotificationBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: NotificationAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        fetchNotifications()

        binding.closeNotificationfragment.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, Home())
                .commit()
        }
    }

    private fun setupRecyclerView() {
        adapter = NotificationAdapter(mutableListOf(), { requestId, fromUserId ->
            acceptFriendRequest(requestId, fromUserId)
        }, { requestId ->
            rejectFriendRequest(requestId)
        })
        binding.notificationRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.notificationRecyclerView.adapter = adapter
    }

    private fun fetchNotifications() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        Log.d("NotificationFragment", "Fetching notifications for user ID: $currentUserId")

        db.collection("friend_requests")
            .whereEqualTo("toUserId", currentUserId)
            .whereEqualTo("status", "pending")
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Log.d("NotificationFragment", "No pending friend requests found.")
                } else {
                    val notifications = documents.map { doc ->
                        FriendRequest(
                            doc.id,
                            doc.getString("fromUserId") ?: "",
                            doc.getString("toUserId") ?: "",
                            doc.getString("status") ?: "pending" // Varsayılan olarak "pending" yap
                        )
                    }
                    Log.d("NotificationFragment", "Fetched ${notifications.size} notifications")
                    adapter.updateNotifications(notifications)
                }
            }
            .addOnFailureListener { e ->
                Log.e("NotificationFragment", "Error fetching notifications", e)
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun acceptFriendRequest(requestId: String, fromUserId: String) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("users").document(currentUserId).update("friends", FieldValue.arrayUnion(fromUserId))
        db.collection("users").document(fromUserId).update("friends", FieldValue.arrayUnion(currentUserId))
            .addOnSuccessListener {
                db.collection("friend_requests").document(requestId).update("status", "accepted")
                    .addOnSuccessListener {
                        adapter.updateItemToFriendAdded(requestId)
                    }
            }
            .addOnFailureListener { e ->
                Log.e("NotificationsFragment", "Error accepting friend request", e)
            }
    }

    private fun rejectFriendRequest(requestId: String) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("friend_requests").document(requestId).delete()
            .addOnSuccessListener {
                adapter.removeNotification(requestId)
            }
            .addOnFailureListener { e ->
                Log.e("NotificationsFragment", "Error rejecting friend request", e)
            }
    }
}
