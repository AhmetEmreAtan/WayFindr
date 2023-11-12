package com.example.wayfindr

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.wayfindr.databinding.ActivityMainBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.ismaeldivita.chipnavigation.ChipNavigationBar

class MainActivity : AppCompatActivity() {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private lateinit var binding: ActivityMainBinding
    private lateinit var bottomNavBar: ChipNavigationBar
    private lateinit var storageReference: StorageReference
    private lateinit var storage: FirebaseStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        bottomNavBar = findViewById(R.id.bottom_nav_bar)

        storage = FirebaseStorage.getInstance()
        storageReference = storage.reference

        val homeFragment = Home()
        supportFragmentManager.beginTransaction().replace(R.id.frame_layout, homeFragment).commit()

        bottomNavBar.setOnItemSelectedListener { itemId ->
            when (itemId) {
                R.id.home -> {
                    replaceFragment(Home())
                }
                R.id.places -> {
                    replaceFragment(Places())
                }
                R.id.profile -> {
                    if (firebaseAuth.currentUser != null) {
                        replaceFragment(Profile())
                    } else {
                        replaceFragment(Login())
                    }
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
