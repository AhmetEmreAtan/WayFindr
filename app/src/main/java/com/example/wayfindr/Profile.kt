package com.example.wayfindr

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.wayfindr.databinding.FragmentProfileBinding
import com.example.wayfindr.home.EventsFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.ismaeldivita.chipnavigation.ChipNavigationBar

class Profile : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private lateinit var storageReference: StorageReference
    private lateinit var myFriendsCounting: TextView
    private lateinit var myFriendsTitle: TextView
    private val PICK_IMAGE_REQUEST = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val view = binding.root

        val navBar: ChipNavigationBar = view.findViewById(R.id.profile_nav_bar)
        setupNavigationBar(navBar)
        if (savedInstanceState == null) {
            navBar.setItemSelected(R.id.memories, true)
            replaceFragment(Memories())
        }

        myFriendsCounting = view.findViewById(R.id.myFriendsCounting)
        myFriendsTitle = view.findViewById(R.id.myFriendsTitle)



        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()
        storageReference = storage.reference

        checkCurrentUser()

        binding.selectProfilePictureButton.setOnClickListener {
            Log.d("ProfileFragment", "Profile picture button clicked")
            openGallery()
        }

        binding.btnprofileedit.setOnClickListener {
            val profileEditFragment = ProfileEdit()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, profileEditFragment)
                .addToBackStack(null)
                .commit()
        }

        binding.btnsetting.setOnClickListener {
            val settingsFragment = Settings()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, settingsFragment)
                .addToBackStack(null)
                .commit()
        }

        binding.addImageBtn.setOnClickListener {
            val addimageFragment = ImageSelectionFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, addimageFragment)
                .addToBackStack(null)
                .commit()
        }

        val userId = auth.currentUser?.uid
        val clickListener = View.OnClickListener {
            if (userId != null) {
                val fragment = FriendListFragment().apply {
                    arguments = Bundle().apply {
                        putString("USER_ID", userId)
                    }
                }
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.profil_constraint_layout, fragment)
                    .commit()
            } else {
                Toast.makeText(context, "Kullanıcı bilgileri bulunamadı.", Toast.LENGTH_SHORT).show()
            }
        }

        myFriendsCounting.setOnClickListener(clickListener)
        myFriendsTitle.setOnClickListener(clickListener)


        return view
    }

    private fun checkCurrentUser() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            val intent = Intent(activity, Login::class.java)
            startActivity(intent)
            requireActivity().finish()
        } else {
            loadUserProfile()
        }
    }

    private fun loadUserProfile() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val docRef = FirebaseFirestore.getInstance().collection("users").document(userId)

            docRef.get().addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val name = document.getString("firstName")
                    val userName = document.getString("username")
                    val imageUrl = document.getString("profileImageUrl")

                    binding.profileName.text = name
                    binding.profileUserName.text = userName?.let { "@$it" }

                    if (!imageUrl.isNullOrEmpty()) {
                        context?.let { ctx ->
                            Glide.with(ctx)
                                .load(imageUrl)
                                .placeholder(R.drawable.placeholder_image)
                                .error(R.drawable.error_image)
                                .transform(CircleCrop())
                                .into(binding.userImage)
                        }
                    } else {
                        binding.userImage.setImageResource(R.drawable.profilephotoicon)
                    }

                    // Memories sayısını göster
                    fetchMemoriesCount(userId)

                    // Friends sayısını göster
                    fetchUserFriendsCount(userId)

                } else {
                    Log.d(ContentValues.TAG, "No such document")
                    Toast.makeText(context, "Kullanıcı bilgileri bulunamadı.", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener { exception ->
                Log.d(ContentValues.TAG, "get failed with ", exception)
                Toast.makeText(context, "Kullanıcı bilgileri yüklenemedi.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Kullanıcı bilgilerine erişilemiyor.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchMemoriesCount(userId: String) {
        val memoriesCountRef = FirebaseFirestore.getInstance()
            .collection("user_photos")
            .document(userId)
            .collection("memories")

        memoriesCountRef.get()
            .addOnSuccessListener { snapshot ->
                val count = snapshot.size()
                binding.myMemoriesCounting.text = count.toString()
            }
            .addOnFailureListener { exception ->
                Log.e("ProfileFragment", "Failed to get memories count", exception)
                binding.myMemoriesCounting.text = "0"
            }
    }

    private fun fetchUserFriendsCount(userId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val friends = document.get("friends") as? List<*>
                    val friendsCount = friends?.size ?: 0
                    myFriendsCounting.text = friendsCount.toString()
                } else {
                    Log.d(ContentValues.TAG, "No such document or document doesn't exist")
                    myFriendsCounting.text = "0"
                }
            }
            .addOnFailureListener { e ->
                Log.e(ContentValues.TAG, "Error fetching friends count: $e")
                myFriendsCounting.text = "0"
            }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            Log.d("ProfileFragment", "Image picked from gallery")
            val selectedImageUri: Uri = data.data!!
            binding.userImage.setImageURI(selectedImageUri)
            uploadProfileImage(selectedImageUri)
        } else {
            Log.d("ProfileFragment", "Failed to pick image from gallery")
        }
    }

    private fun uploadProfileImage(imageUri: Uri) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val ref = storageReference.child("images/$userId/profile_image.jpg")

            ref.putFile(imageUri).addOnSuccessListener { taskSnapshot ->
                taskSnapshot.metadata?.reference?.downloadUrl?.addOnSuccessListener { downloadUri ->
                    val imageUrl = downloadUri.toString()

                    FirebaseFirestore.getInstance().collection("users").document(userId)
                        .update("profileImageUrl", imageUrl).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                context?.let {
                                    Toast.makeText(it, "Profil resmi başarıyla güncellendi!", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                task.exception?.let { e ->
                                    context?.let { ctx ->
                                        Toast.makeText(ctx, "Profil resmi güncellenirken hata oluştu: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                }
            }.addOnFailureListener { e ->
                context?.let {
                    Toast.makeText(it, "Profil resmi yüklenirken hata oluştu: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    //PRofile nav bar
    private fun setupNavigationBar(navBar: ChipNavigationBar) {
        navBar.setOnItemSelectedListener { id ->
            when (id) {
                R.id.memories -> replaceFragment(Memories())
                R.id.favorites -> replaceFragment(Favorites())
                R.id.events -> replaceFragment(EventsFragment())
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        childFragmentManager.beginTransaction()
            .replace(R.id.profile_frame_layout, fragment)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
