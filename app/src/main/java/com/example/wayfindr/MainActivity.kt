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

    private val PERMISSION_REQUEST_CODE = 200
    private val PICK_IMAGE_REQUEST = 1
    private lateinit var filePath: Uri

    private lateinit var addImageBtn: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        bottomNavBar = findViewById(R.id.bottom_nav_bar)
        addImageBtn = findViewById(R.id.add_image_btn)

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

        addImageBtn.setOnClickListener {
            val fragment = ImageSelectionFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, fragment)
                .addToBackStack(null)
                .commit()
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.commit()
    }

    private fun openGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSION_REQUEST_CODE)
            } else {
                val intent = Intent()
                intent.type = "image/*"
                intent.action = Intent.ACTION_GET_CONTENT
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)
            }
        } else {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery()
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            filePath = data.data!!
            if (filePath != null) {
                uploadImage(filePath)
            }
        }
    }

    private fun uploadImage(filePath: Uri) {
        val ref = storageReference.child("images/" + System.currentTimeMillis())

        ref.putFile(filePath)
            .addOnSuccessListener {
                Toast.makeText(this@MainActivity, "Fotoğrafınız başarı ile yüklendi.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this@MainActivity, "Failed " + e.message, Toast.LENGTH_SHORT).show()
            }
    }
}
