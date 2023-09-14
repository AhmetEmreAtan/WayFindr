package com.example.wayfindr

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.wayfindr.databinding.ActivityProfilePageBinding

class Profile_page : AppCompatActivity() {

    private lateinit var binding : ActivityProfilePageBinding

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
            val intent = Intent(this, AdminEdit::class.java)
            startActivity(intent)
        }
    }
}