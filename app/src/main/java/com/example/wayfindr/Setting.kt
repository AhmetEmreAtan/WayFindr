package com.example.wayfindr

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContentProviderCompat.requireContext
import com.example.wayfindr.databinding.ActivitySettingBinding
import com.google.firebase.auth.FirebaseAuth

class     Setting : AppCompatActivity() {

    private lateinit var binding: ActivitySettingBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        sharedPreferences = getSharedPreferences("MY_PRE", Context.MODE_PRIVATE)

       binding.logOutButton.setOnClickListener {
           auth.signOut()
           clearUserPreference()

           val intent = Intent(this, Logins::class.java)
           startActivity(intent)


       }
    }
    private fun clearUserPreference(){
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
    }
}
