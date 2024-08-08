package com.example.wayfindr.memories

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.wayfindr.R
import com.example.wayfindr.search.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue

class MemoriesDetail : AppCompatActivity() {

    private var selectedMemoryId: String? = null
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_memories_detail)

        selectedMemoryId = intent.getStringExtra("memoryId")
        userId = intent.getStringExtra("userId")

        val userComment = intent.getStringExtra("userComment")
        val photoLocation = intent.getStringExtra("photoLocation")
        val imageUrl = intent.getStringExtra("imageUrl")
        val currentUser = FirebaseAuth.getInstance().currentUser

        val imageView: ImageView = findViewById(R.id.memories_user_image)
        val currentUserImageView: ImageView = findViewById(R.id.myCommentPP)
        val locationTextView: TextView = findViewById(R.id.memory_detail_location)
        val commentTextView: TextView = findViewById(R.id.memory_detail_comment)

        locationTextView.text = photoLocation
        commentTextView.text = userComment

        Glide.with(this)
            .load(imageUrl)
            .centerCrop()
            .placeholder(R.drawable.placeholder_image)
            .error(R.drawable.error_image)
            .into(imageView)

        if (currentUser != null && currentUser.photoUrl != null) {
            Glide.with(this)
                .load(currentUser.photoUrl)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.profilephotoicon)
                .transform(CircleCrop())
                .into(currentUserImageView)
        } else if (currentUser != null) {
            loadCurrentUserProfile(currentUser.uid)
        }

        // Sayfayı kapatma işlemi
        val btnClosedMemories: ImageButton = findViewById(R.id.btn_closed_memories)
        btnClosedMemories.setOnClickListener {
            finish()
        }

        // Gönderi beğenme işlemi
        val likeButton: ImageButton = findViewById(R.id.like_button)
        likeButton.setOnClickListener {
            handleLikeAction()
        }
    }

    private fun loadCurrentUserProfile(currentUserId: String) {
        val currentUserRef = FirebaseFirestore.getInstance().collection("users").document(currentUserId)
        currentUserRef.get().addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                val currentUser = document.toObject(User::class.java)
                if (currentUser != null) {
                    val currentUserImageView: ImageView = findViewById(R.id.myCommentPP)
                    Glide.with(this)
                        .load(currentUser.profileImageUrl)
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.profilephotoicon)
                        .transform(CircleCrop())
                        .into(currentUserImageView)
                }
            }
        }
    }

    companion object {
        fun newIntent(
            context: Context,
            memoryId: String,
            userComment: String,
            photoLocation: String,
            imageUrl: String,
            userProfileImageUrl: String,
            username: String,
            userId: String
        ): Intent {
            return Intent(context, MemoriesDetail::class.java).apply {
                putExtra("memoryId", memoryId)
                putExtra("userComment", userComment)
                putExtra("photoLocation", photoLocation)
                putExtra("imageUrl", imageUrl)
                putExtra("userProfileImageUrl", userProfileImageUrl)
                putExtra("username", username)
                putExtra("userId", userId)
            }
        }
    }

    private fun handleLikeAction() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val memoryId = selectedMemoryId
        val userId = this.userId

        if (currentUser != null && !memoryId.isNullOrEmpty() && !userId.isNullOrEmpty()) {
            val memoryRef = FirebaseFirestore.getInstance()
                .collection("user_photos")
                .document(userId)
                .collection("memories")
                .document(memoryId)

            memoryRef.update("likes", FieldValue.arrayUnion(currentUser.uid))
                .addOnSuccessListener {
                    val likeButton: ImageButton = findViewById(R.id.like_button)
                    likeButton.setColorFilter(ContextCompat.getColor(this, R.color.white))
                    Toast.makeText(this, "Beğendiniz", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Beğenme işlemi başarısız: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Geçersiz memory ID veya user ID", Toast.LENGTH_SHORT).show()
            Log.e("MemoriesDetail", "Invalid memory ID or user ID. memoryId: $memoryId, userId: $userId")
        }
    }
}