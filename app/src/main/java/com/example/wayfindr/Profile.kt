package com.example.wayfindr

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
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

        binding.selectProfilePictureButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 1)
        }

        binding.saveProfilePictureButton.setOnClickListener {
            if (selectedImageUri != null) {
                uploadProfileImage()
            } else {
                Toast.makeText(requireContext(), "Lütfen bir profil resmi seçin.", Toast.LENGTH_SHORT).show()
            }
        }

        if (currentUser != null) {
            val userId = currentUser.uid
            val databaseReference = FirebaseDatabase.getInstance().getReference("users/$userId")

            databaseReference.child("profileImage").get().addOnSuccessListener { dataSnapshot ->
                val imageUrl = dataSnapshot.value as? String
                if (!imageUrl.isNullOrBlank()) {
                    val circularImageView = view.findViewById<ImageView>(R.id.userImage)
                    Glide.with(requireContext())
                        .load(imageUrl)
                        .transform(CircleCrop())
                        .into(circularImageView)
                }
            }

            databaseReference.child("name").get().addOnSuccessListener { dataSnapshot ->
                val username = dataSnapshot.value as? String
                if (!username.isNullOrBlank()) {
                    val profileName = view.findViewById<TextView>(R.id.profileName)
                    profileName.text = username
                }
            }
        }

        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1 && resultCode == -1 && data != null) {
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

                        Toast.makeText(requireContext(), "Profil resmi başarıyla güncellendi!", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Profil resmi güncellenirken hata oluştu: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}