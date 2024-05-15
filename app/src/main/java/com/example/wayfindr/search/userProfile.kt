package com.example.wayfindr.userProfile

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.wayfindr.R
import com.example.wayfindr.search.User
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth

class userProfile : Fragment() {

    private lateinit var userNameTextView: TextView
    private lateinit var firstNameTextView: TextView
    private lateinit var profileImageView: ImageView
    private lateinit var followButton: Button

    private val currentUser = FirebaseAuth.getInstance().currentUser
    private val usersRef = FirebaseFirestore.getInstance().collection("users")
    private val friendsRef = currentUser?.uid?.let { usersRef.document(it).collection("friends") }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_user_profile, container, false)

        val userId = arguments?.getString("userId")
        if (userId != null) {
            getUserData(userId).addOnSuccessListener { document ->
                if (document != null) {
                    val user = document.toObject(User::class.java)

                    userNameTextView = view.findViewById<TextView>(R.id.userNameTextView)
                    userNameTextView.text = user?.username
                    userNameTextView.setBackgroundResource(R.drawable.blurbackground)

                    firstNameTextView = view.findViewById<TextView>(R.id.firstNameTextView)
                    firstNameTextView.text = user?.firstName
                    firstNameTextView.setBackgroundResource(R.drawable.blurbackground)

                    profileImageView = view.findViewById<ImageView>(R.id.userImage)
                    Glide.with(this)
                        .load(user?.profileImageUrl)
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.error_image)
                        .transform(CircleCrop())
                        .into(profileImageView)

                    followButton = view.findViewById<Button>(R.id.add_friend_button)
                    setFollowButtonLabel(userId)
                    followButton.setOnClickListener {
                        toggleFollowButton(userId)
                    }
                } else {
                    Log.d(TAG, "No such document")
                }
            }.addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val userId = arguments?.getString("userId")
        if (userId != null) {
            getUserData(userId)
        }
    }

    private fun getUserData(userId: String): Task<DocumentSnapshot> {
        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("users").document(userId)
        return userRef.get()
    }

    private fun checkFriendshipStatus(userId: String, callback: (Boolean) -> Unit) {
        friendsRef?.document(userId)?.get()
            ?.addOnSuccessListener { documentSnapshot ->
                callback(documentSnapshot.exists())
            }
            ?.addOnFailureListener { exception ->
                Log.e(TAG, "Error checking friendship status: $exception")
                callback(false)
            }
    }

    private fun checkFriendRequestStatus(userId: String, callback: (Boolean) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("friend_requests")
            .whereEqualTo("from", currentUser?.uid)
            .whereEqualTo("to", userId)
            .whereEqualTo("status", "pending")
            .get()
            .addOnSuccessListener { documents ->
                callback(!documents.isEmpty)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error checking friend request status: $e")
                callback(false)
            }
    }

    private fun toggleFollowButton(userId: String) {
        checkFriendshipStatus(userId) { isFriend ->
            if (isFriend) {
                friendsRef?.document(userId)?.delete()
                setFollowButtonLabel(userId)
            } else {
                checkFriendRequestStatus(userId) { isRequested ->
                    if (isRequested) {
                        cancelFriendRequest(currentUser?.uid ?: "", userId)
                    } else {
                        sendFriendRequest(currentUser?.uid ?: "", userId)
                    }
                }
            }
        }
    }

    private fun sendFriendRequest(currentUserId: String, targetUserId: String) {
        val db = FirebaseFirestore.getInstance()
        val friendRequest = hashMapOf(
            "from" to currentUserId,
            "to" to targetUserId,
            "status" to "pending"
        )

        db.collection("friend_requests").add(friendRequest)
            .addOnSuccessListener { documentReference ->
                Log.d("FriendRequest", "Friend request sent with ID: ${documentReference.id}")
                setFollowButtonLabel(targetUserId)
            }
            .addOnFailureListener { e ->
                Log.w("FriendRequest", "Error sending friend request", e)
            }
    }

    private fun cancelFriendRequest(currentUserId: String, targetUserId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("friend_requests")
            .whereEqualTo("from", currentUserId)
            .whereEqualTo("to", targetUserId)
            .whereEqualTo("status", "pending")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    db.collection("friend_requests").document(document.id).delete()
                        .addOnSuccessListener {
                            Log.d("FriendRequest", "Friend request canceled")
                            setFollowButtonLabel(targetUserId)
                        }
                        .addOnFailureListener { e ->
                            Log.w("FriendRequest", "Error canceling friend request", e)
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.w("FriendRequest", "Error finding friend request to cancel", e)
            }
    }

    private fun setFollowButtonLabel(userId: String) {
        checkFriendshipStatus(userId) { isFriend ->
            if (isFriend) {
                followButton.text = "Arkadaşsınız"
                followButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.user_trust, 0, 0, 0)
                followButton.isEnabled = false
            } else {
                checkFriendRequestStatus(userId) { isRequested ->
                    if (isRequested) {
                        followButton.text = "İstek Yollandı"
                        followButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.request, 0, 0, 0)
                        followButton.isEnabled = true
                    } else {
                        followButton.text = "Arkadaş Ekle"
                        followButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_person_add_24, 0, 0, 0)
                        followButton.isEnabled = true
                    }
                }
            }
        }
    }

    companion object {
        fun newInstance(userId: String): userProfile {
            val fragment = userProfile()
            val args = Bundle()
            args.putString("userId", userId)
            fragment.arguments = args
            return fragment
        }
    }
}
