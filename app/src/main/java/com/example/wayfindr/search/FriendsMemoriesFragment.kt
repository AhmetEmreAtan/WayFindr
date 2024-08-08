package com.example.wayfindr.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.wayfindr.databinding.FragmentFriendsMemoriesBinding
import com.google.firebase.firestore.FirebaseFirestore

class FriendsMemoriesFragment : Fragment() {

    private var _binding: FragmentFriendsMemoriesBinding? = null
    private val binding get() = _binding!!
    private lateinit var memoriesAdapter: FriendsMemoriesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFriendsMemoriesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        memoriesAdapter = FriendsMemoriesAdapter(requireContext(), mutableListOf())
        binding.recyclerViewFriendsMemories.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = memoriesAdapter
        }

        fetchMemories()
    }

    private fun fetchMemories() {
        val userId = arguments?.getString("userId") ?: return

        val db = FirebaseFirestore.getInstance()
        db.collection("user_photos").document(userId).collection("memories")
            .get()
            .addOnSuccessListener { result ->
                val memories = result.toObjects(Memory::class.java)
                memoriesAdapter = FriendsMemoriesAdapter(requireContext(), memories)
                binding.recyclerViewFriendsMemories.adapter = memoriesAdapter
            }
            .addOnFailureListener { exception ->

            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
