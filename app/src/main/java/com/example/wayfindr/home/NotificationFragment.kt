package com.example.wayfindr.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wayfindr.R
import com.example.wayfindr.home.adapters.NotificationAdapter
import com.example.wayfindr.home.models.FriendRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class NotificationFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NotificationAdapter
    private val db = FirebaseFirestore.getInstance()
    private val currentUser = FirebaseAuth.getInstance().currentUser

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_notification, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.notificationRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = NotificationAdapter(mutableListOf(), this::acceptFriendRequest, this::rejectFriendRequest)
        recyclerView.adapter = adapter
        fetchPendingFriendRequests()

        val closeButton = view.findViewById<ImageButton>(R.id.close_notificationfragment)
        closeButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

    }

    private fun fetchPendingFriendRequests() {
        currentUser?.let { user ->
            db.collection("users")
                .document(user.uid)
                .collection("friend_requests")
                .whereEqualTo("status", "pending")
                .get()
                .addOnSuccessListener { documents ->
                    val friendRequests = mutableListOf<FriendRequest>()
                    for (document in documents) {
                        val request = document.toObject(FriendRequest::class.java)
                        request.id = document.id
                        friendRequests.add(request)
                    }
                    adapter.updateNotifications(friendRequests)
                }
                .addOnFailureListener { exception ->
                    Log.e("NotificationFragment", "Error fetching friend requests: ", exception)
                    Toast.makeText(requireContext(), "Error fetching friend requests.", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun acceptFriendRequest(requestId: String, fromUserId: String) {
        currentUser?.let { user ->
            db.collection("users")
                .document(user.uid)
                .collection("friend_requests")
                .document(requestId)
                .update("status", "accepted")
                .addOnSuccessListener {
                    db.collection("users").document(user.uid).update("friends", FieldValue.arrayUnion(fromUserId))
                    db.collection("users").document(fromUserId).update("friends", FieldValue.arrayUnion(user.uid))
                    adapter.updateItemToFriendAdded(requestId)
                    Toast.makeText(requireContext(), "Friend request accepted", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Log.e("NotificationFragment", "Error accepting friend request", e)
                    Toast.makeText(requireContext(), "Error accepting friend request", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun rejectFriendRequest(requestId: String) {
        currentUser?.let { user ->
            db.collection("users")
                .document(user.uid)
                .collection("friend_requests")
                .document(requestId)
                .update("status", "rejected")
                .addOnSuccessListener {
                    adapter.removeNotification(requestId)
                    Toast.makeText(requireContext(), "Friend request rejected", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Log.e("NotificationFragment", "Error rejecting friend request", e)
                    Toast.makeText(requireContext(), "Error rejecting friend request", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
