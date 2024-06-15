package com.example.wayfindr.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wayfindr.R
import com.example.wayfindr.home.adapters.MessagesAdapter
import com.example.wayfindr.home.models.ChatUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MessagesFragment : Fragment() {

    private lateinit var recyclerViewMessages: RecyclerView
    private lateinit var messagesAdapter: MessagesAdapter
    private val chatUsersList = mutableListOf<ChatUser>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_messages, container, false)

        recyclerViewMessages = view.findViewById(R.id.recyclerViewMessages)
        messagesAdapter = MessagesAdapter(chatUsersList) { chatUser ->
            openChatFragment(chatUser)
        }
        recyclerViewMessages.adapter = messagesAdapter
        recyclerViewMessages.layoutManager = LinearLayoutManager(context)

        fetchChatUsers()

        return view
    }

    private fun fetchChatUsers() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("messages")
            .whereIn("from", listOf(userId))
            .whereIn("to", listOf(userId))
            .get()
            .addOnSuccessListener { documents ->
                val userIds = mutableSetOf<String>()
                for (document in documents) {
                    val fromId = document.getString("from") ?: ""
                    val toId = document.getString("to") ?: ""
                    if (fromId != userId) userIds.add(fromId)
                    if (toId != userId) userIds.add(toId)
                }
                fetchUsersDetails(userIds)
            }
            .addOnFailureListener { e ->
                Log.w("MessagesFragment", "Error getting messages", e)
            }
    }

    private fun fetchUsersDetails(userIds: Set<String>) {
        val db = FirebaseFirestore.getInstance()
        userIds.forEach { userId ->
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val user = document.toObject(ChatUser::class.java)
                        if (user != null) {
                            chatUsersList.add(user)
                            messagesAdapter.notifyDataSetChanged()
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.w("MessagesFragment", "Error getting user details", e)
                }
        }
    }

    private fun openChatFragment(chatUser: ChatUser) {
        val chatFragment = ChatFragment().apply {
            arguments = Bundle().apply {
                putString("CHAT_USER_ID", chatUser.userId)
            }
        }

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, chatFragment)
            .addToBackStack(null)
            .commit()
    }
}
