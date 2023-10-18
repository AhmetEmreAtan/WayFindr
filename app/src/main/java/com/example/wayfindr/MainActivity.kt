package com.example.wayfindr

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.wayfindr.databinding.ActivityMainBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.ismaeldivita.chipnavigation.ChipNavigationBar

class MainActivity : AppCompatActivity() {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private lateinit var binding: ActivityMainBinding
    private lateinit var bottomNavBar: ChipNavigationBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        bottomNavBar = findViewById(R.id.bottom_nav_bar)

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

        if (savedInstanceState == null) {
            replaceFragment(Home())
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.commit()
    }
}
