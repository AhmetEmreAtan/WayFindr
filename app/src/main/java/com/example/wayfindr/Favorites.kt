package com.example.wayfindr

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.wayfindr.databinding.ActivityFavoritesBinding
import com.example.wayfindr.databinding.ActivityLoginBinding

class Favorites : AppCompatActivity() {

    private lateinit var binding: ActivityFavoritesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =ActivityFavoritesBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}