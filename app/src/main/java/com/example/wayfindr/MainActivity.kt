package com.example.wayfindr

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.wayfindr.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.ismaeldivita.chipnavigation.ChipNavigationBar

class MainActivity : AppCompatActivity() {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private lateinit var binding : ActivityMainBinding
    private lateinit var bottomNavBar: ChipNavigationBar


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        replaceFragment(Home())

        bottomNavBar = findViewById(R.id.bottom_nav_bar)

        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, Home())
            .commit()

        bottomNavBar.setOnItemSelectedListener { itemId ->
            when (itemId) {
                R.id.home -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frame_layout, Home())
                        .commit()
                }
                R.id.places -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frame_layout, Places())
                        .commit()
                }
                R.id.profile -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frame_layout, Profile())
                        .commit()
                }
            }
        }




    }


    private fun replaceFragment(fragment: Fragment) {

        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.commit()

    }
}