package com.example.wayfindr.memories

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.wayfindr.R

class MemoriesDetail : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_memories_detail)

        val userComment = intent.getStringExtra("userComment")
        val photoLocation = intent.getStringExtra("photoLocation")
        val imageUrl = intent.getStringExtra("imageUrl")


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
}
