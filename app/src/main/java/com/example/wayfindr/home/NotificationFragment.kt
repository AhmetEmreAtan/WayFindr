package com.example.wayfindr.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.wayfindr.Home
import com.example.wayfindr.R
import com.example.wayfindr.databinding.FragmentNotificationBinding
import com.example.wayfindr.home.models.FriendRequest
import com.google.firebase.auth.FirebaseAuth
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
        adapter = NotificationAdapter(emptyList())
        binding.notificationRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.notificationRecyclerView.adapter = adapter
    }

    private fun fetchNotifications() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("friend_requests")
            .whereEqualTo("to", currentUserId)
            .whereEqualTo("status", "pending")
            .get()
            .addOnSuccessListener { documents ->
                val notifications = documents.map { doc ->
                    FriendRequest(
                        doc.id,
                        doc.getString("from") ?: "",
                        doc.getString("to") ?: "",
                        doc.getString("status") ?: ""
                    )
                }
                adapter.updateNotifications(notifications)
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
