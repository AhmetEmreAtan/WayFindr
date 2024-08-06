package com.example.wayfindr.memories

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.wayfindr.R
import com.example.wayfindr.search.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MemoriesDetail : AppCompatActivity() {

    private var selectedMemoryId: String? = null
    private lateinit var editText: EditText
    private var documentIds = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_memories_detail)

        selectedMemoryId = intent.getStringExtra("memoryId")

        val userComment = intent.getStringExtra("userComment")
        val photoLocation = intent.getStringExtra("photoLocation")
        val imageUrl = intent.getStringExtra("imageUrl")
        val userProfileImageUrl = intent.getStringExtra("userProfileImageUrl")
        val username = intent.getStringExtra("username")
        val currentUser = FirebaseAuth.getInstance().currentUser


        val imageView: ImageView = findViewById(R.id.memories_user_image)
        val profileImageView: ImageView = findViewById(R.id.memoriespp)
        val currentUserImageView: ImageView = findViewById(R.id.imageView3)
        val usernameTextView: TextView = findViewById(R.id.memoriesusername)


        usernameTextView.text = username

        Glide.with(this)
            .load(imageUrl)
            .centerCrop()
            .placeholder(R.drawable.placeholder_image)
            .error(R.drawable.error_image)
            .into(imageView)

        Glide.with(this)
            .load(userProfileImageUrl)
            .placeholder(R.drawable.placeholder_image)
            .error(R.drawable.profilephotoicon)
            .transform(CircleCrop())
            .into(profileImageView)

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

        val btnClosedMemories: ImageButton = findViewById(R.id.btn_closed_memories)
        btnClosedMemories.setOnClickListener {
            finish()
        }
    }


    private fun loadCurrentUserProfile(currentUserId: String) {
        val currentUserRef = FirebaseFirestore.getInstance().collection("users").document(currentUserId)
        currentUserRef.get().addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                val currentUser = document.toObject(User::class.java)
                if (currentUser != null) {
                    val currentUserImageView: ImageView = findViewById(R.id.imageView3)
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


    private fun showEditTextForComment(comment: Comment) {
        val parentLayout: LinearLayout = findViewById(R.id.comment_linearlayout)

        editText = EditText(this)
        editText.hint = "Yorumunuzu buraya girin"
        editText.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        val widthInDp = 20
        val heightInDp = 20
        val marginRightInDp = 8

        val width = (widthInDp * resources.displayMetrics.density).toInt()
        val height = (heightInDp * resources.displayMetrics.density).toInt()
        val marginRight = (marginRightInDp * resources.displayMetrics.density).toInt()

        val drawableRight = ContextCompat.getDrawable(this, R.drawable.icon_share)
        drawableRight?.setBounds(0, 0, width, height)

        editText.setCompoundDrawablesWithIntrinsicBounds(null, null, drawableRight, null)
        editText.compoundDrawablePadding = marginRight

        parentLayout.addView(editText)
    }

    private fun saveComment(commentText: String, userName: String) {
        val db = Firebase.firestore
        val commentsCollection = db.collection("yorumlar")

        val newComment = hashMapOf(
            "yorumMetni" to commentText,
            "userName" to userName,
            "memoryId" to selectedMemoryId
        )

        commentsCollection.add(newComment)
            .addOnSuccessListener { documentReference ->
                Log.d(
                    TAG,
                    "Yorum başarıyla eklendi, eklenen yorumun ID'si: ${documentReference.id}"
                )
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Yorum ekleme işlemi başarısız oldu", e)
            }
    }

    private fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.menuInflater.inflate(R.menu.memories_options_menu, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_delete -> {
                    if (documentIds.isNotEmpty()) {
                        deleteDocument(documentIds[0])
                    } else {
                        showToast("Silinecek belge bulunamadı.")
                    }
                    true
                }
                R.id.menu_report -> {
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }

    private fun deleteDocument(documentId: String) {
        val db = Firebase.firestore
        val userPhotosCollection = db.collection("user_photos").document("user_id").collection("memories")

        userPhotosCollection.document(documentId)
            .delete()
            .addOnSuccessListener {
                Log.d(TAG, "Belge başarıyla silindi.")
                showToast("Belge başarıyla silindi.")
                finish()
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Belge silme işlemi başarısız oldu", e)
                showToast("Belge silinemedi. Lütfen tekrar deneyin.")
            }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
