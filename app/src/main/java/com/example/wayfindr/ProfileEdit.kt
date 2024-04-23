package com.example.wayfindr

import android.app.Dialog
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.wayfindr.databinding.FragmentProfileEditBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

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
        val view = inflater.inflate(R.layout.fragment_profile_edit, container, false)
        binding = FragmentProfileEditBinding.inflate(layoutInflater)

        auth = FirebaseAuth.getInstance()
        val uid = auth.currentUser?.uid
        databaseReference = FirebaseDatabase.getInstance().getReference("users")
        binding.editButton.setOnClickListener {

            showProgressBar(requireContext())
            val name = binding.editName.text.toString()
            val userName = binding.editUserName.text.toString()
            val email = binding.editEmail.text.toString()
            val password = binding.editPassword.text.toString()

            val user = UserData(name, userName, email, password)
            if (uid != null) {

                databaseReference.child(uid).setValue(user).addOnCompleteListener {

                    if (it.isSuccessful) {

                        uploadProfilePic()

                    } else {

                        hideProgressBar()
                        Toast.makeText(context, "Failed to update profile", Toast.LENGTH_SHORT).show()

                    }

                }

            }

        }

        return view
    }

    private fun uploadProfilePic() {

        imageUri = Uri.parse("android.resource://${requireContext().packageName}/${R.drawable.profilephotoicon}")
        storageReference = FirebaseStorage.getInstance().getReference("users/" + auth.currentUser?.uid)
        storageReference.putFile(imageUri).addOnSuccessListener {

            hideProgressBar()
            Toast.makeText(context, "Profile successfully updated", Toast.LENGTH_SHORT).show()

        }.addOnFailureListener {

            hideProgressBar()
            Toast.makeText(context, "Failed to upload the image", Toast.LENGTH_SHORT).show()

        }

    }

    private fun showProgressBar(context: Context) {

        dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_wait)
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()

    }

    private fun hideProgressBar() {

        dialog.dismiss()

    }
}
