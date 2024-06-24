package com.example.wayfindr

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.wayfindr.databinding.ActivityMainBinding
import com.example.wayfindr.search.Search
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.ismaeldivita.chipnavigation.ChipNavigationBar
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var bottomNavBar: ChipNavigationBar
    private val firebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkFirstRun()
        setupBottomNavigationBar()
        loadDefaultFragment()
    }

    private fun checkFirstRun() {
        val sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val isFirstRun = sharedPreferences.getBoolean("isFirstRun", true)

        if (isFirstRun) {
            startActivity(Intent(this, Welcome::class.java))
            finish()
            sharedPreferences.edit().putBoolean("isFirstRun", false).apply()
        }
    }

    private fun setupBottomNavigationBar() {
        bottomNavBar = binding.bottomNavBar
        bottomNavBar.setOnItemSelectedListener { itemId ->
            when (itemId) {
                R.id.home -> replaceFragment(Home())
                R.id.places -> replaceFragment(Places())
                R.id.search -> replaceFragment(Search())
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

    private fun loadDefaultFragment() {
        bottomNavBar.setItemSelected(R.id.home, true)
    }


    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.commit()
    }

}
