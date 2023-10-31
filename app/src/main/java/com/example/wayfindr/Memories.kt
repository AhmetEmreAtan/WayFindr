package com.example.wayfindr

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wayfindr.memories.Memory
import com.example.wayfindr.memories.MemoryAdapter
import com.google.firebase.firestore.FirebaseFirestore

class Memories : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MemoryAdapter
    private lateinit var memoriesList: MutableList<Memory>
    private val db = FirebaseFirestore.getInstance()
    private val memoriesCollection = db.collection("memories")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_memories, container, false)

        
        recyclerView = view.findViewById(R.id.recyclerView_memories)
        memoriesList = mutableListOf()
        adapter = MemoryAdapter(requireContext(), memoriesList)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        fetchMemoriesFromFirebase()



        return view
    }

    private fun fetchMemoriesFromFirebase() {
        memoriesCollection.get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot != null) {
                    for (document in querySnapshot) {
                        val userComment = document.getString("userComment")
                        val photoLocation = document.getString("photoLocation")
                        val photoUrl = document.getString("photoUrl")

                        if (userComment != null && photoLocation != null && photoUrl != null) {
                            val memory = Memory(userComment, photoLocation, photoUrl)
                            memoriesList.add(memory)
                        }
                    }
                    adapter.notifyDataSetChanged()
                }
            }
            .addOnFailureListener { exception ->
            }
    }
}
