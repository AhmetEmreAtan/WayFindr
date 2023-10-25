package com.example.wayfindr

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.wayfindr.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop

class Profile : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private lateinit var storageReference: StorageReference
    private var selectedImageUri: Uri? = null
    private lateinit var fragmentContext: Context

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(layoutInflater)
        val view = binding.root

        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()
        storageReference = storage.reference

        val currentUser = auth.currentUser

        binding.favButton.setOnClickListener {
            val intent = Intent(context, Favorites::class.java)
            startActivity(intent)
        }
        binding.adminButton.setOnClickListener {
            val intent = Intent(context, AdminEdit::class.java)
            startActivity(intent)
        }
        binding.settingButton.setOnClickListener {
            val intent = Intent(context, Setting::class.java)
            startActivity(intent)
        }

        binding.selectProfilePictureButton.setOnClickListener {
            val permission = Manifest.permission.READ_EXTERNAL_STORAGE
            val granted = PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(fragmentContext, permission)

            if (!granted) {
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(permission), REQUEST_CODE)
            } else {
                openGallery()
            }
        }

        binding.saveProfilePictureButton.setOnClickListener {
            if (selectedImageUri != null) {
                uploadProfileImage()
            } else {
                Toast.makeText(fragmentContext, "Lütfen bir profil resmi seçin.", Toast.LENGTH_SHORT).show()
            }
        }

        if (currentUser != null) {
            val userId = currentUser.uid
            val databaseReference = FirebaseDatabase.getInstance().getReference("users/$userId")

            databaseReference.child("profileImage").get().addOnSuccessListener { dataSnapshot ->
                val imageUrl = dataSnapshot.value as? String
                if (imageUrl != null && !imageUrl.isBlank()) {
                    val circularImageView = view.findViewById<ImageView>(R.id.userImage)
                    Glide.with(fragmentContext)
                        .load(imageUrl)
                        .transform(CircleCrop())
                        .into(circularImageView)
                }
            }

            databaseReference.child("name").get().addOnSuccessListener { dataSnapshot ->
                val name = dataSnapshot.value as? String
                if (name != null && !name.isBlank()) {
                    val profileName = view.findViewById<TextView>(R.id.profileName)
                    profileName.text = name
                }
            }

            databaseReference.child("userName").get().addOnSuccessListener { dataSnapshot ->
                val username = dataSnapshot.value as? String
                if (username != null && !username.isBlank()) {
                    val profileName = view.findViewById<TextView>(R.id.profileUserName)
                    profileName.text = username
                }
            }

        }

        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE && resultCode == -1 && data != null) {
            selectedImageUri = data.data
            binding.userImage.setImageURI(selectedImageUri)
        }
    }

    private fun uploadProfileImage() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val filename = "profile_image.jpg"
            val ref = storageReference.child("images/$userId/$filename")

            ref.putFile(selectedImageUri!!)
                .addOnSuccessListener { taskSnapshot ->
                    taskSnapshot.metadata?.reference?.downloadUrl?.addOnSuccessListener { uri ->
                        val imageUrl = uri.toString()

                        val databaseReference = FirebaseDatabase.getInstance().getReference("users/$userId")
                        databaseReference.child("profileImage").setValue(imageUrl)

                        Toast.makeText(fragmentContext, "Profil resmi başarıyla güncellendi!", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(fragmentContext, "Profil resmi güncellenirken hata oluştu: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_CODE)
    }

    companion object {
        private const val REQUEST_CODE = 123 // Kendi isteğinize göre bir değer belirleyebilirsiniz
    }
}
