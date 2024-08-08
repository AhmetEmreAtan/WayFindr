package com.example.wayfindr

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.wayfindr.databinding.FragmentFriendListBinding
import com.google.firebase.firestore.FirebaseFirestore

class FriendListFragment : Fragment() {

    private var _binding: FragmentFriendListBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFriendListBinding.inflate(inflater, container, false)
        val view = binding.root
        val userId = arguments?.getString("USER_ID")

        if (userId != null) {
            loadFriends(userId)
        }


        binding.btnClosedFriends.setOnClickListener {
            val fragmentManager = requireActivity().supportFragmentManager
            if (fragmentManager.backStackEntryCount > 0) {
                fragmentManager.popBackStack()  // Geri git
            } else {
                requireActivity().onBackPressed()
            }
        }

        return view
    }

    private fun loadFriends(userId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val friends = document.get("friends") as? List<String> ?: emptyList()
                    Log.d("FriendListFragment", "Friends: $friends")
                    displayFriends(friends)
                } else {
                    Log.d("FriendListFragment", "No such document")
                }
            }
            .addOnFailureListener { e ->
                Log.e("FriendListFragment", "Error getting friends", e)
            }
    }

    private fun displayFriends(friends: List<String>) {
        val friendsLayout = binding.friendsContainer
        friendsLayout.removeAllViews()

        friends.forEach { friendId ->
            FirebaseFirestore.getInstance().collection("users").document(friendId).get()
                .addOnSuccessListener { document ->
                    val name = document.getString("firstName")
                    val userName = document.getString("username")
                    val imageUrl = document.getString("profileImageUrl")

                    val friendView = LayoutInflater.from(context).inflate(R.layout.item_friend, friendsLayout, false)

                    val friendImage = friendView.findViewById<ImageView>(R.id.friendImage)
                    val friendName = friendView.findViewById<TextView>(R.id.friendName)
                    val friendUserName = friendView.findViewById<TextView>(R.id.friendUserName)

                    friendName.text = name
                    friendUserName.text = userName?.let { "@$it" }

                    if (!imageUrl.isNullOrEmpty()) {
                        Glide.with(this)
                            .load(imageUrl)
                            .placeholder(R.drawable.placeholder_image)
                            .error(R.drawable.error_image)
                            .transform(CircleCrop())
                            .into(friendImage)
                    } else {
                        friendImage.setImageResource(R.drawable.profilephotoicon)
                    }

                    friendsLayout.addView(friendView)
                }
                .addOnFailureListener { e ->
                    Log.e("FriendListFragment", "Error loading friend data", e)
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
