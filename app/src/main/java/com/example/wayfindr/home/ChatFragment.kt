package com.example.wayfindr.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wayfindr.R
import com.example.wayfindr.home.adapters.ChatMessagesAdapter
import com.example.wayfindr.home.models.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class ChatFragment : Fragment() {

    private lateinit var recyclerViewChatMessages: RecyclerView
    private lateinit var chatMessagesAdapter: ChatMessagesAdapter
    private val chatMessagesList = mutableListOf<Message>()
    private lateinit var chatUserId: String
    private lateinit var editTextMessage: EditText
    private lateinit var buttonSend: Button

    private var isSendingMessage = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chat, container, false)

        recyclerViewChatMessages = view.findViewById(R.id.recyclerViewChatMessages)
        chatMessagesAdapter = ChatMessagesAdapter(chatMessagesList)
        recyclerViewChatMessages.adapter = chatMessagesAdapter
        recyclerViewChatMessages.layoutManager = LinearLayoutManager(context)

        chatUserId = arguments?.getString("CHAT_USER_ID") ?: return view

        editTextMessage = view.findViewById(R.id.editTextMessage)
        buttonSend = view.findViewById(R.id.buttonSend)

        buttonSend.setOnClickListener {
            if (!isSendingMessage) {
                sendMessage()
            }
        }

        fetchChatMessages()

        return view
    }

    private fun sendMessage() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val messageText = editTextMessage.text.toString().trim()

        if (messageText.isNotEmpty()) {
            isSendingMessage = true
            buttonSend.isEnabled = false

            val message = hashMapOf(
                "senderId" to userId,
                "receiverId" to chatUserId,
                "message" to messageText,
                "timestamp" to FieldValue.serverTimestamp()
            )

            Log.d("ChatFragment", "Sending message: $message")

            val db = FirebaseFirestore.getInstance()
            db.collection("messages").add(message)
                .addOnSuccessListener {
                    Log.d("ChatFragment", "Message sent successfully")
                    editTextMessage.text.clear()
                    fetchChatMessages()  // Mesaj gönderildikten sonra tekrar mesajları çek
                    isSendingMessage = false
                    buttonSend.isEnabled = true
                }
                .addOnFailureListener { e ->
                    Log.e("ChatFragment", "Error sending message", e)
                    e.printStackTrace()
                    isSendingMessage = false
                    buttonSend.isEnabled = true
                    Toast.makeText(requireContext(), "Gönderme işlemi başarısız oldu.", Toast.LENGTH_SHORT).show()
                }
        } else {
            Log.d("ChatFragment", "Message text is empty")
        }
    }

    private fun fetchChatMessages() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("messages")
            .whereEqualTo("from", userId)
            .whereEqualTo("to", chatUserId)
            .orderBy("timestamp")
            .get()
            .addOnSuccessListener { documents ->
                chatMessagesList.clear()
                for (document in documents) {
                    val message = document.toObject(Message::class.java)
                    chatMessagesList.add(message)
                }
                chatMessagesAdapter.notifyDataSetChanged()
                recyclerViewChatMessages.scrollToPosition(chatMessagesList.size - 1)
            }
            .addOnFailureListener { e ->
                Log.e("ChatFragment", "Error fetching messages", e)
                e.printStackTrace()
            }
    }
}
