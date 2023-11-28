package com.example.wayfindr

import com.example.wayfindr.memories.MemoryAdapter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wayfindr.memories.Memory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Memories : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MemoryAdapter
    private lateinit var memoriesList: MutableList<Memory>
    private val db = FirebaseFirestore.getInstance()
    private val memoriesCollection = db.collection("user_photos")

    private var documentIds = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        memoriesList = mutableListOf()
        val view = inflater.inflate(R.layout.fragment_memories, container, false)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView_memories)
        val memoryAdapter = MemoryAdapter(requireContext(), memoriesList)
        adapter = memoryAdapter
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        val auth = FirebaseAuth.getInstance()


        auth.currentUser?.uid?.let { fetchAllMemoriesFromFirebase(it) }

        return view
    }

    private fun fetchAllMemoriesFromFirebase(Identifier: String) {
        val userPhotosCollection = db.collection("user_photos").document(Identifier).collection("memories")

        userPhotosCollection.get().addOnSuccessListener { documents ->
            for (document in documents) {
                val userComment = document.getString("userComment")
                val photoLocation = document.getString("photoLocation")
                val imageUrl = document.getString("imageUrl")

                if (userComment != null && photoLocation != null && imageUrl != null) {
                    memoriesList.add(Memory(userComment!!, photoLocation!!, imageUrl!!))
                }
            }


            adapter.notifyDataSetChanged()
        }.addOnFailureListener { exception ->
            Toast.makeText(context, "Firestore'dan veri alınamadı: ${exception.message}", Toast.LENGTH_SHORT).show()
            Log.e("Firestore", "Hata: ${exception.message}")
        }
    }


}