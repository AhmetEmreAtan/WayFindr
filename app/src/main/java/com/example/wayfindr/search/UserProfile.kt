package com.example.wayfindr.search

import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.wayfindr.R
import com.example.wayfindr.home.ChatFragment
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class UserProfile : Fragment() {

    private lateinit var userNameTextView: TextView
    private lateinit var firstNameTextView: TextView
    private lateinit var profileImageView: ImageView
    private lateinit var followButton: Button
    private lateinit var sendMessageButton: Button
    private lateinit var userMemoriesInfoText: TextView
    private lateinit var userProfileFriendsText: TextView

    private val currentUser = FirebaseAuth.getInstance().currentUser
    private val usersRef = FirebaseFirestore.getInstance().collection("users")

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

                    userNameTextView = view.findViewById(R.id.userNameTextView)
                    userNameTextView.text = user?.username
                    userNameTextView.setBackgroundResource(R.drawable.blurbackground)

                    firstNameTextView = view.findViewById(R.id.firstNameTextView)
                    firstNameTextView.text = user?.firstName
                    firstNameTextView.setBackgroundResource(R.drawable.blurbackground)

                    profileImageView = view.findViewById(R.id.userImage)
                    Glide.with(this)
                        .load(user?.profileImageUrl)
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.profilephotoicon)
                        .transform(CircleCrop())
                        .into(profileImageView)

                    followButton = view.findViewById(R.id.add_friend_button)
                    setFollowButtonLabel(userId)
                    followButton.setOnClickListener {
                        toggleFollowButton(userId)
                    }

                    sendMessageButton = view.findViewById(R.id.send_message_button)
                    sendMessageButton.setOnClickListener {
                        openChatFragment(userId, userId)
                    }

                    userMemoriesInfoText = view.findViewById(R.id.userMemoriesInfoText)
                    userProfileFriendsText = view.findViewById(R.id.userProfileFriendsText)

                    // Fetch Memories Count
                    fetchMemoriesCount(userId)

                    // Fetch Friends Count
                    fetchUserFriendsCount(userId)
                } else {
                    Log.d(TAG, "No such document")
                }
            }.addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }
        }

        return view
    }

    private fun getUserData(userId: String): Task<DocumentSnapshot> {
        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("users").document(userId)
        return userRef.get()
    }

    private fun openChatFragment(chatId: String, receiverId: String) {
        val chatFragment = ChatFragment().apply {
            arguments = Bundle().apply {
                putString("CHAT_ID", chatId)
                putString("RECEIVER_ID", receiverId)
            }
        }

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, chatFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun checkFriendshipStatus(userId: String, callback: (Boolean) -> Unit) {
        usersRef.document(currentUser!!.uid).get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val friends = documentSnapshot.get("friends") as? List<*>
                    val isFriend = friends?.contains(userId) ?: false
                    callback(isFriend)
                } else {
                    callback(false)
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error checking friendship status: $exception")
                callback(false)
            }
    }


    private fun checkFriendRequestStatus(userId: String, callback: (Boolean) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val currentUserId = currentUser?.uid ?: return

        db.collection("users")
            .document(userId)
            .collection("friend_requests")
            .whereEqualTo("from", currentUserId)
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
                showRemoveFriendDialog(userId)
            } else {
                checkFriendRequestStatus(userId) { isRequested ->
                    if (isRequested) {
                        cancelFriendRequest(userId)
                    } else {
                        sendFriendRequest(userId)
                    }
                }
            }
        }
    }

    private fun sendFriendRequest(targetUserId: String) {
        val db = FirebaseFirestore.getInstance()
        val currentUserId = currentUser?.uid ?: return

        val friendRequest = hashMapOf(
            "from" to currentUserId,
            "status" to "pending",
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("users")
            .document(targetUserId)
            .collection("friend_requests")
            .add(friendRequest)
            .addOnSuccessListener { documentReference ->
                Log.d("FriendRequest", "Friend request sent with ID: ${documentReference.id}")
                setFollowButtonLabel(targetUserId)
            }
            .addOnFailureListener { e ->
                Log.w("FriendRequest", "Error sending friend request", e)
            }
    }





    private fun cancelFriendRequest(targetUserId: String) {
        val db = FirebaseFirestore.getInstance()
        val currentUserId = currentUser?.uid ?: return

        db.collection("users")
            .document(targetUserId)
            .collection("friend_requests")
            .whereEqualTo("from", currentUserId)
            .whereEqualTo("status", "pending")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    db.collection("users")
                        .document(targetUserId)
                        .collection("friend_requests")
                        .document(document.id)
                        .delete()
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
                followButton.isEnabled = true
                followButton.setOnClickListener {
                    showRemoveFriendDialog(userId)
                }
            } else {
                checkFriendRequestStatus(userId) { isRequested ->
                    if (isRequested) {
                        followButton.text = "İstek Yollandı"
                        followButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.request, 0, 0, 0)
                        followButton.isEnabled = false
                    } else {
                        followButton.text = "Arkadaş Ekle"
                        followButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_person_add_24, 0, 0, 0)
                        followButton.isEnabled = true
                        followButton.setOnClickListener {
                            sendFriendRequest(userId)
                        }
                    }
                }
            }
        }
    }

    private fun showRemoveFriendDialog(userId: String) {
        val dialogBuilder = AlertDialog.Builder(requireContext())
        dialogBuilder.setMessage("Kişiyi arkadaşlıktan çıkarırsanız tekrardan istek yollamanız gerekecek.")
            .setCancelable(false)
            .setPositiveButton("Evet") { dialog, id ->
                removeFriend(userId)
            }
            .setNegativeButton("Hayır") { dialog, id ->
                dialog.dismiss()
            }
        val alert = dialogBuilder.create()
        alert.setTitle("Arkadaş silinsin mi?")
        alert.show()
    }

    private fun removeFriend(userId: String) {
        val currentUserId = currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        val batch = db.batch()

        val currentUserRef = db.collection("users").document(currentUserId)
        batch.update(currentUserRef, "friends", FieldValue.arrayRemove(userId))

        val otherUserRef = db.collection("users").document(userId)
        batch.update(otherUserRef, "friends", FieldValue.arrayRemove(currentUserId))

        batch.commit().addOnSuccessListener {
            Toast.makeText(requireContext(), "Arkadaşlıktan çıkarıldı", Toast.LENGTH_SHORT).show()
            setFollowButtonLabel(userId)
        }.addOnFailureListener { e ->
            Log.e(TAG, "Arkadaşlıktan çıkarma işlemi başarısız: $e")
            Toast.makeText(requireContext(), "Arkadaşlıktan çıkarma işlemi başarısız", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchMemoriesCount(userId: String) {
        val memoriesCountRef = FirebaseFirestore.getInstance()
            .collection("user_photos")
            .document(userId)
            .collection("memories")

        memoriesCountRef.get()
            .addOnSuccessListener { snapshot ->
                val count = snapshot.size()
                userMemoriesInfoText.text = count.toString()
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Failed to get memories count", exception)
                userMemoriesInfoText.text = "0"
            }
    }

    private fun fetchUserFriendsCount(userId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val friends = document.get("friends") as? List<*>
                    val friendsCount = friends?.size ?: 0
                    userProfileFriendsText.text = friendsCount.toString()
                } else {
                    Log.d(TAG, "No such document or document doesn't exist")
                    userProfileFriendsText.text = "0"
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error fetching friends count: $e")
                userProfileFriendsText.text = "0"
            }
    }

    companion object {
        fun newInstance(userId: String): UserProfile {
            val fragment = UserProfile()
            val args = Bundle()
            args.putString("userId", userId)
            fragment.arguments = args
            return fragment
        }
    }
}
