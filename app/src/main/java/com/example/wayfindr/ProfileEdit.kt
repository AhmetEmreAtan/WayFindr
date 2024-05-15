package com.example.wayfindr

import android.app.Dialog
import android.content.ContentValues.TAG
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.wayfindr.databinding.FragmentProfileEditBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.StorageReference
import de.hdodenhof.circleimageview.CircleImageView

class ProfileEdit : Fragment() {

    private lateinit var binding: FragmentProfileEditBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var storageReference: StorageReference
    private lateinit var imageUri: Uri
    private lateinit var dialog: Dialog


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileEditBinding.inflate(inflater, container, false)
        val view = binding.root

        val db = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        val editName: EditText = binding.editName
        val editUserName: EditText = binding.editUserName
        val editEmail: EditText = binding.editEmail
        val profile_image: CircleImageView = binding.profileImage
        val editPassword: EditText = binding.editPassword

        if (userId != null) {
            val docRef = db.collection("users").document(userId)

            docRef.get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val firstName = document.getString("firstName")
                        val profileImageUrl = document.getString("profileImageUrl")
                        val username = document.getString("username")
                        val email = document.getString("email")

                        editName.setText(firstName)
                        editUserName.setText(username)
                        editEmail.setText(email)

                        Glide.with(requireContext())
                            .load(profileImageUrl)
                            .into(profile_image)
                    } else {
                        Log.d(TAG, "No such document")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d(TAG, "get failed with ", exception)
                }
        }

        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            val email = user.email
            editEmail.setText(email)


            val password = "********"
            editPassword.setText(password)
        }

        //Update Fun
        val editButton: Button = binding.editButton
        editButton.setOnClickListener {
            val newName = binding.editName.text.toString()
            val newUserName = binding.editUserName.text.toString()
            val newEmail = binding.editEmail.text.toString()
            val newPassword = binding.editPassword.text.toString()

            val user = FirebaseAuth.getInstance().currentUser

            user?.let { currentUser ->
                currentUser.updateEmail(newEmail)
                    .addOnSuccessListener {
                        Log.d(TAG, "User email address updated.")
                        val db = FirebaseFirestore.getInstance()
                        val userId = currentUser.uid
                        val userRef = db.collection("users").document(userId)

                        userRef.update(mapOf(
                            "firstName" to newName,
                            "username" to newUserName
                        ))
                            .addOnSuccessListener {
                                Log.d(TAG, "User profile updated.")
                                if (newPassword.isNotEmpty()) {
                                    currentUser.updatePassword(newPassword)
                                        .addOnSuccessListener {
                                            Log.d(TAG, "User password updated.")
                                            Toast.makeText(requireContext(), "Bilgiler başarıyla güncellendi.", Toast.LENGTH_SHORT).show()
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

        val closeBtn: ImageButton = binding.closeBtn
        closeBtn.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }





        return view
    }


}
