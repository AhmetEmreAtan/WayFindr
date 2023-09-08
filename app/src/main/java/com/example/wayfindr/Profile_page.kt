package com.example.wayfindr

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.wayfindr.databinding.ActivityFavoritesBinding
import com.example.wayfindr.databinding.ActivityProfilePageBinding
import kotlin.math.round

class Profile_page : AppCompatActivity() {

    private lateinit var binding : ActivityProfilePageBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityProfilePageBinding.inflate(layoutInflater)
        setContentView(binding.root)



        binding.favButton.setOnClickListener{
            val intent = Intent(this, Favorites::class.java)
            startActivity(intent)
        }
    }
}