package com.example.wayfindr

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.example.wayfindr.databinding.ActivityFavoritesBinding
import com.example.wayfindr.databinding.ActivityProfilePageBinding
import kotlin.math.round

class Profile_page : AppCompatActivity() {

    private lateinit var binding : ActivityProfilePageBinding
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfilePageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPreferences = getSharedPreferences("MY_PRE", Context.MODE_PRIVATE)

        val email = sharedPreferences.getString("EMAIL", "")
        val password = sharedPreferences.getString("PASSWORD", "")
        binding.profileName.text = "$email"




        binding.favButton.setOnClickListener{
            val intent = Intent(this, Favorites::class.java)
            startActivity(intent)
        }

        binding.settingButton.setOnClickListener {
            val intent = Intent(this, Setting::class.java)
            startActivity(intent)
        }

        binding.adminButton.setOnClickListener {
            val intent = Intent(this, admin::class.java)
            startActivity(intent)
        }
    }
}