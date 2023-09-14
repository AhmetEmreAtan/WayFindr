package com.example.wayfindr
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class AdminEdit: AppCompatActivity() {

    private lateinit var editName: EditText
    private lateinit var editEmail: EditText
    private lateinit var editUsername: EditText
    private lateinit var editPassword: EditText
    private lateinit var saveButton: Button
    private lateinit var nameUser: String
    private lateinit var emailUser: String
    private lateinit var usernameUser: String
    private lateinit var passwordUser: String
    private lateinit var reference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_edit)
        reference = FirebaseDatabase.getInstance().getReference("users")

        editName = findViewById(R.id.editName)
        editEmail = findViewById(R.id.editEmail)
        editUsername = findViewById(R.id.editUsername) // Bu satırı ekledim
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
        if (!nameUser.equals(editName.text.toString())) {
            reference.child(usernameUser).child("name").setValue(editName.text.toString())
            nameUser = editName.text.toString()
            return true
        } else {
            return false
        }
    }

    private fun isEmailChanged(): Boolean {
        if (!emailUser.equals(editEmail.text.toString())) {
            reference.child(usernameUser).child("email").setValue(editEmail.text.toString())
            emailUser = editEmail.text.toString()
            return true
        } else {
            return false
        }
    }

    private fun isPasswordChanged(): Boolean {
        if (!passwordUser.equals(editPassword.text.toString())) {
            reference.child(usernameUser).child("password").setValue(editPassword.text.toString())
            passwordUser = editPassword.text.toString()
            return true
        } else {
            return false
        }
    }

    private fun showData() {
        val intent = intent

        nameUser = intent.getStringExtra("name") ?: ""
        emailUser = intent.getStringExtra("email") ?: ""
        usernameUser = intent.getStringExtra("username") ?: "" // Buradaki "username" alanını değiştirdim
        passwordUser = intent.getStringExtra("password") ?: ""

        editName.setText(nameUser)
        editEmail.setText(emailUser)
        editUsername.setText(usernameUser)
        editPassword.setText(passwordUser)
    }
}
