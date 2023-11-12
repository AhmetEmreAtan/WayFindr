package com.example.wayfindr.memories

import android.content.ContentValues.TAG
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.wayfindr.R
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class MemoriesDetail : AppCompatActivity() {

    private lateinit var editText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_memories_detail)


        val userComment = intent.getStringExtra("userComment")
        val photoLocation = intent.getStringExtra("photoLocation")
        val imageUrl = intent.getStringExtra("imageUrl")

        val recyclerView: RecyclerView = findViewById(R.id.commentsRecyclerView)
        val commentsList = listOf(
            Comment("1", "userId1", "Yorum 1"),
            Comment("2", "userId2", "Yorum 2"),
        )


        val adapter = CommentsAdapter(commentsList) { selectedComment ->
            Log.d("SelectedComment", "Selected comment: ${selectedComment.commentText}")
            showEditTextForComment(selectedComment)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter


        val userCommentTextView: TextView = findViewById(R.id.memories_user_comment)
        val photoLocationTextView: TextView = findViewById(R.id.memories_location)
        val imageView: ImageView = findViewById(R.id.memories_user_image)


        userCommentTextView.text = userComment
        photoLocationTextView.text = photoLocation
        Glide.with(this)
            .load(imageUrl)
            .centerCrop()
            .placeholder(R.drawable.placeholder_image)
            .error(R.drawable.error_image)
            .into(imageView)

        val btnClosedMemories: ImageButton = findViewById(R.id.btn_closed_memories)
        btnClosedMemories.setOnClickListener {
            finish()
        }
    }

    private fun showEditTextForComment(comment: Comment) {
        val parentLayout: LinearLayout = findViewById(R.id.comment_linearlayout)

        val editText = EditText(this)
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


        editText.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= (editText.right - editText.compoundDrawables[2].bounds.width())) {
                    val userComment = editText.text.toString()
                    val userName = "KullanıcıAdı"

                    saveComment(userComment, userName)
                    return@setOnTouchListener true
                }
            }
            false
        }
    }

    private fun saveComment(commentText: String, userName: String) {
        val userComment = editText.text.toString()
        val userName = "KullanıcıAdı"

        saveComment(userComment, userName)

        val db = Firebase.firestore
        val commentsCollection = db.collection("yorumlar")

        val newComment = hashMapOf(
            "yorumMetni" to commentText,
            "userName" to userName
        )

        commentsCollection.add(newComment)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "Yorum başarıyla eklendi, eklenen yorumun ID'si: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Yorum ekleme işlemi başarısız oldu", e)
            }
    }


}
