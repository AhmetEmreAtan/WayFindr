package com.example.wayfindr


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ProfileEdit : Fragment() {

    private lateinit var editName: EditText
    private lateinit var editUserName: EditText
    private lateinit var editEmail: EditText
    private lateinit var editPassword: EditText
    private lateinit var editButton: Button

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_admin_edit, container, false)

        editName = view.findViewById(R.id.editName)
        editEmail = view.findViewById(R.id.editEmail)
        editPassword = view.findViewById(R.id.editPassword)
        editButton = view.findViewById(R.id.editButton)
        editUserName = view.findViewById(R.id.editUserName)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("users")

        val user = auth.currentUser
        if (user != null) {

            val userId = user.uid
            database.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        val name = dataSnapshot.child("name").getValue(String::class.java)
                        val email = dataSnapshot.child("email").getValue(String::class.java)
                        val password = dataSnapshot.child("password").getValue(String::class.java)
                        val userName = dataSnapshot.child("userName").getValue(String::class.java)

                        editName.setText(name)
                        editEmail.setText(email)
                        editPassword.setText(password)
                        editUserName.setText(userName)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle onCancelled event if needed
                }
            })

            editButton.setOnClickListener {
                val newName = editName.text.toString()
                val newEmail = editEmail.text.toString()
                val newPassword = editPassword.text.toString()
                val newUserName = editUserName.text.toString()

                database.child(userId).child("name").setValue(newName)
                database.child(userId).child("email").setValue(newEmail)
                database.child(userId).child("password").setValue(newPassword)
                database.child(userId).child("userName").setValue(newUserName)
                Toast.makeText(requireContext(), "Bilgiler güncellendi.", Toast.LENGTH_SHORT).show()
            }
        }
        return view
    }
}
