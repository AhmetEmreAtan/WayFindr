package com.example.wayfindr

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.wayfindr.databinding.FragmentProfileEditBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileEdit : Fragment() {

    private var _binding: FragmentProfileEditBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var userId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        userId = auth.currentUser?.uid.orEmpty()

        loadUserProfile()

        binding.editButton.setOnClickListener {
            updateUserProfile()
        }

        binding.closeBtn.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    private fun loadUserProfile() {
        if (userId.isNotEmpty()) {
            val docRef = db.collection("users").document(userId)
            docRef.get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val firstName = document.getString("firstName")
                        val profileImageUrl = document.getString("profileImageUrl")
                        val username = document.getString("username")
                        val email = document.getString("email")

                        binding.editName.setText(firstName)
                        binding.editUserName.setText(username)
                        binding.editEmail.setText(email)

                        Glide.with(requireContext())
                            .load(profileImageUrl)
                            .into(binding.profileImage)
                    } else {
                        Log.d(TAG, "No such document")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d(TAG, "get failed with ", exception)
                    Toast.makeText(requireContext(), "Kullanıcı bilgileri alınırken hata oluştu.", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun updateUserProfile() {
        val newName = binding.editName.text.toString()
        val newUserName = binding.editUserName.text.toString()
        val newEmail = binding.editEmail.text.toString()
        val newPassword = binding.editPassword.text.toString()

        val user = auth.currentUser

        user?.let { currentUser ->
            currentUser.updateEmail(newEmail)
                .addOnSuccessListener {
                    Log.d(TAG, "User email address updated.")
                    val userRef = db.collection("users").document(currentUser.uid)

                    val updates = mapOf(
                        "firstName" to newName,
                        "username" to newUserName,
                        "email" to newEmail
                    )

                    userRef.update(updates)
                        .addOnSuccessListener {
                            Log.d(TAG, "User profile updated.")
                            if (newPassword.isNotEmpty()) {
                                currentUser.updatePassword(newPassword)
                                    .addOnSuccessListener {
                                        Log.d(TAG, "User password updated.")
                                        Toast.makeText(requireContext(), "Bilgiler başarıyla güncellendi.", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener { exception ->
                                        Log.d(TAG, "User password update failed: $exception")
                                        Toast.makeText(requireContext(), "Şifre güncellenirken bir hata oluştu.", Toast.LENGTH_SHORT).show()
                                    }
                            } else {
                                Toast.makeText(requireContext(), "Bilgiler başarıyla güncellendi.", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.d(TAG, "User profile update failed: $exception")
                            Toast.makeText(requireContext(), "Bilgiler güncellenirken bir hata oluştu.", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener { exception ->
                    Log.d(TAG, "Email address update failed: $exception")
                    Toast.makeText(requireContext(), "E-posta güncellenirken bir hata oluştu.", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "ProfileEdit"
    }
}