package com.example.wayfindr

import android.Manifest
import android.app.Activity
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
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.wayfindr.databinding.FragmentProfileBinding
import com.ismaeldivita.chipnavigation.ChipNavigationBar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class Profile : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private lateinit var storageReference: StorageReference
    private lateinit var fragmentContext: Context
    private lateinit var profileNavBar: ChipNavigationBar
    private val PERMISSION_REQUEST_CODE = 200
    private val PICK_IMAGE_REQUEST = 1
    private lateinit var selectedImageUri: Uri
    private lateinit var addImageBtn: FloatingActionButton

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

        profileNavBar = view.findViewById(R.id.profile_nav_bar)

        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()
        storageReference = storage.reference

        val currentUser = auth.currentUser

        val circularImageView = view.findViewById<ImageView>(R.id.userImage)
        val placeholderImage = R.drawable.blurimage
        Glide.with(requireContext())
            .load(placeholderImage)
            .transform(CircleCrop())
            .into(circularImageView)

        binding.selectProfilePictureButton.setOnClickListener {
            val permission = Manifest.permission.READ_EXTERNAL_STORAGE
            val granted = PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(fragmentContext, permission)

            if (!granted) {
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(permission), PERMISSION_REQUEST_CODE)
            } else {
                openGallery()
            }
        }

        binding.saveProfilePictureButton.setOnClickListener {
            if (::selectedImageUri.isInitialized) {
                uploadProfileImage()
                Toast.makeText(fragmentContext, "Profil fotoğrafınız başarı ile kaydedildi.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(fragmentContext, "Lütfen bir profil resmi seçin.", Toast.LENGTH_SHORT).show()
            }
        }

        // Add Memories
        addImageBtn = view.findViewById(R.id.add_image_btn)

        addImageBtn.setOnClickListener {
            openImageSelectionFragment()
        }

        if (currentUser != null) {
            val userId = currentUser.uid
            val databaseReference = FirebaseDatabase.getInstance().getReference("users/$userId")

            Glide.with(requireContext())
                .load(placeholderImage)
                .into(circularImageView)

            databaseReference.child("profileImage").get().addOnSuccessListener { dataSnapshot ->
                val imageUrl = dataSnapshot.value as? String
                if (imageUrl != null && !imageUrl.isBlank()) {
                    Glide.with(requireContext())
                        .load(imageUrl)
                        .placeholder(placeholderImage)
                        .error(placeholderImage)
                        .transform(CircleCrop())
                        .into(circularImageView)
                }
            }

            val profileNameTextView = view.findViewById<TextView>(R.id.profileName)
            val profileUserNameTextView = view.findViewById<TextView>(R.id.profileUserName)
            profileNameTextView.setBackgroundResource(placeholderImage)
            profileUserNameTextView.setBackgroundResource(placeholderImage)

            databaseReference.child("name").get().addOnSuccessListener { dataSnapshot ->
                val name = dataSnapshot.value as? String
                if (name != null && !name.isBlank()) {
                    profileNameTextView.text = name
                    profileNameTextView.setBackgroundResource(0)
                }
            }

            databaseReference.child("userName").get().addOnSuccessListener { dataSnapshot ->
                val userName = dataSnapshot.value as? String
                if (userName != null && !userName.isBlank()) {
                    val formattedUserName = "@$userName"
                    profileUserNameTextView.text = formattedUserName
                    profileUserNameTextView.setBackgroundResource(0)
                }
            }
        }

        profileNavBar.setOnItemSelectedListener { itemId ->
            when (itemId) {
                R.id.anilar -> {
                    replaceFragment(Memories())
                }
                R.id.favorites -> {
                    replaceFragment(Favorites())
                }
            }
        }

        val initialFragment = Memories()
        childFragmentManager.beginTransaction()
            .replace(R.id.profile_frame_layout, initialFragment)
            .commit()

        binding.btnprofileedit.setOnClickListener {
            val fragment = ProfileEdit()
            requireActivity().supportFragmentManager
                .beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                .replace(R.id.frame_layout, fragment)
                .addToBackStack(null)
                .commit()
        }

        binding.btnsetting.setOnClickListener {
            val fragment = Settings()
            requireActivity().supportFragmentManager
                .beginTransaction()
                .replace(R.id.frame_layout, fragment)
                .addToBackStack(null)
                .commit()
        }

        return view
    }

    private fun openImageSelectionFragment() {
        val fragment = ImageSelectionFragment()
        requireActivity().supportFragmentManager
            .beginTransaction()
            .replace(android.R.id.content, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            selectedImageUri = data.data!!
            binding.userImage.setImageURI(selectedImageUri)
        }
    }

    private fun uploadProfileImage() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val filename = "profile_image.jpg"
            val ref = storageReference.child("images/$userId/$filename")

            ref.putFile(selectedImageUri)
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

    private fun replaceFragment(fragment: Fragment) {
        requireActivity().supportFragmentManager
            .beginTransaction()
            .replace(R.id.profile_frame_layout, fragment)
            .addToBackStack(null)
            .commit()
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 123
    }
}
