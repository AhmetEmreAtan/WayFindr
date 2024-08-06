package com.example.wayfindr.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wayfindr.R
import com.example.wayfindr.home.EventsFragment
import com.example.wayfindr.memories.FriendsMemoriesAdapter
import com.example.wayfindr.memories.Memory
import com.google.firebase.firestore.FirebaseFirestore
import com.ismaeldivita.chipnavigation.ChipNavigationBar

class FriendsMemoriesFragment : Fragment() {

    private lateinit var adapter: FriendsMemoriesAdapter
    private lateinit var recyclerView: RecyclerView
    private var memoriesList: MutableList<Memory> = mutableListOf()
    private lateinit var chipNavigationBar: ChipNavigationBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        chipNavigationBar = requireView().findViewById(R.id.userprofile_nav_bar)

        val userId = arguments?.getString("userId") ?: return view

        setupNavigationBar(userId)
        setupRecyclerView(userId)

        return view
    }

    private fun setupNavigationBar(userId: String) {
        chipNavigationBar.setItemSelected(R.id.userMemoriesMenu, true)
        chipNavigationBar.setOnItemSelectedListener { id ->
            when (id) {
                R.id.userMemoriesMenu -> {
                    setupRecyclerView(userId)
                }
                R.id.userEventMenu -> {
                    setupRecyclerView(userId)
                }
            }
        }
    }


    private fun setupRecyclerView(userId: String) {
        val db = FirebaseFirestore.getInstance()
        val memoriesRef = db.collection("user_photos").document(userId).collection("memories")

        memoriesRef.get().addOnSuccessListener { documents ->
            if (isAdded) {
                memoriesList.clear()
                for (document in documents) {
                    val memory = document.toObject(Memory::class.java)
                    memoriesList.add(memory)
                }
                adapter = FriendsMemoriesAdapter(requireContext(), memoriesList)
                recyclerView.adapter = adapter
                adapter.notifyDataSetChanged()
            }
        }.addOnFailureListener { exception ->
            if (isAdded) {
                Toast.makeText(requireContext(), "Veriler alınamadı: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (::adapter.isInitialized) {
            adapter.notifyDataSetChanged()
        }
    }

    override fun onStop() {
        super.onStop()
    }

    companion object {
        fun newInstance(userId: String): FriendsMemoriesFragment {
            val fragment = FriendsMemoriesFragment()
            val args = Bundle()
            args.putString("userId", userId)
            fragment.arguments = args
            return fragment
        }
    }
}
