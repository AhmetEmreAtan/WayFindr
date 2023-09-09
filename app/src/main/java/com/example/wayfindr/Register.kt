package com.example.wayfindr

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.wayfindr.databinding.ActivityRegisterBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Register : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseDatabase = FirebaseDatabase.getInstance()
        databaseReference = firebaseDatabase.reference.child("users")

        binding.singUp.setOnClickListener {
            val name = binding.name.text.toString()
            val email = binding.eMail2.text.toString()
            val password = binding.passwords2.text.toString()

            if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                signupUser(name, email, password)
            } else {
                Toast.makeText(this@Register, "Tüm alanları doldurmak zorunludur", Toast.LENGTH_SHORT).show()
            }
        }

        binding.letLogIn.setOnClickListener {
            startActivity(Intent(this@Register, Login::class.java))
            finish()
        }


        val signUpTextView = findViewById<Button>(R.id.singUp)
        signUpTextView.setOnClickListener {
            val intent = Intent(this@Register, Profile_page::class.java)
            startActivity(intent)
        }
    }

    private fun signupUser(name: String, email: String, password: String) {
        databaseReference.orderByChild("name").equalTo(name)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (!dataSnapshot.exists()) {
                        val id = databaseReference.push().key
                        val userData = UserData(id, name, email, password)
                        databaseReference.child(id!!).setValue(userData)
                        Toast.makeText(this@Register, "Kayıt gerçekleşti", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@Register, Login::class.java))
                        finish()
                    } else {
                        Toast.makeText(this@Register, "Kullanıcı zaten var", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(
                        this@Register,
                        "Database Error : ${databaseError.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
}
