package com.example.wayfindr

import androidx.appcompat.app.AppCompatActivity
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class AdminEdit : AppCompatActivity() {

    private lateinit var editName: EditText
    private lateinit var editEmail: EditText
    private lateinit var editPassword: EditText
    private lateinit var editButton: Button

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_edit)

        editName = findViewById(R.id.editName)
        editEmail = findViewById(R.id.editEmail)
        editPassword = findViewById(R.id.editPassword)
        editButton = findViewById(R.id.editButton)

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


                        editName.setText(name)
                        editEmail.setText(email)
                        editPassword.setText(password)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {

                }
            })

            editButton.setOnClickListener {
                val newName = editName.text.toString()
                val newEmail = editEmail.text.toString()
                val newPassword = editPassword.text.toString()


                database.child(userId).child("name").setValue(newName)
                database.child(userId).child("email").setValue(newEmail)
                database.child(userId).child("password").setValue(newPassword)


            }
        } else {

        }
    }
}