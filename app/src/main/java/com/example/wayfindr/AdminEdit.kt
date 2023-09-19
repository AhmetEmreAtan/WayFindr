package com.example.wayfindr

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class AdminEdit : AppCompatActivity() {

    private lateinit var editName: EditText
    private lateinit var editEmail: EditText
    private lateinit var editPassword: EditText
    private lateinit var saveButton: Button
    private lateinit var name: String
    private lateinit var email: String
    private lateinit var password: String
    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_edit)

        firebaseDatabase = FirebaseDatabase.getInstance()
        databaseReference = firebaseDatabase.reference.child("users")
        auth = FirebaseAuth.getInstance()

        editName = findViewById(R.id.editName)
        editEmail = findViewById(R.id.editEmail)
        editPassword = findViewById(R.id.editPassword)
        saveButton = findViewById(R.id.saveButton)

        showData()

        saveButton.setOnClickListener {
            if (isNameChanged() || isPasswordChanged() || isEmailChanged()) {
                Toast.makeText(this@AdminEdit, "Saved", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@AdminEdit, "No Changes Found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isNameChanged(): Boolean {
        val newName = editName.text.toString()
        if (name != newName) {
            databaseReference.child(name).child("name").setValue(newName)
            name = newName
            return true
        }
        return false
    }

    private fun isEmailChanged(): Boolean {
        val newEmail = editEmail.text.toString()
        if (email != newEmail) {
            databaseReference.child(name).child("email").setValue(newEmail)
            email = newEmail
            return true
        }
        return false
    }

    private fun isPasswordChanged(): Boolean {
        val newPassword = editPassword.text.toString()
        if (password != newPassword) {
            databaseReference.child(name).child("password").setValue(newPassword)
            password = newPassword
            return true
        }
        return false
    }

    private fun showData() {
        val intent = intent
        name = intent.getStringExtra("name") ?: ""
        email = intent.getStringExtra("email") ?: ""
        password = intent.getStringExtra("password") ?: ""

        editName.setText(name)
        editEmail.setText(email)
        editPassword.setText(password)
    }
}
