package com.example.wayfindr

import androidx.appcompat.app.AppCompatActivity
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class AdminEdit : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var editName: EditText
    private lateinit var editEmail: EditText
    private lateinit var editPassword: EditText
    private lateinit var editButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_edit)

        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        val id = user?.uid

        database = FirebaseDatabase.getInstance().reference.child("users").child(id ?: "")

        editName = findViewById(R.id.editName)
        editEmail = findViewById(R.id.editEmail)
        editPassword = findViewById(R.id.editPassword)
        editButton = findViewById(R.id.editButton)


        val sharedPreferences = getSharedPreferences("MY_PRE", Context.MODE_PRIVATE)
        val name = sharedPreferences.getString("NAME", "")
        val email = sharedPreferences.getString("EMAIL", "")
        val password = sharedPreferences.getString("PASSWORD", "")


        editName.setText(name)
        editEmail.setText(email)
        editPassword.setText(password)

        editButton.setOnClickListener {
            val name = editName.text.toString()
            val email = editEmail.text.toString()
            val password = editPassword.text.toString()

            val newEmail = editEmail.text.toString()
            val newPassword = editPassword.text.toString()


            val user = FirebaseAuth.getInstance().currentUser


            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build()

            user?.updateProfile(profileUpdates)
                ?.addOnSuccessListener {
                    user.updateEmail(newEmail)
                        ?.addOnSuccessListener {

                            user.updatePassword(newPassword)
                                .addOnSuccessListener {

                                    val userUpdates = HashMap<String, Any>()
                                    userUpdates["name"] = name
                                    userUpdates["email"] = newEmail


                                    val id = user.uid
                                    val database = FirebaseDatabase.getInstance().reference.child("users")
                                    database.child(id).updateChildren(userUpdates)
                                        .addOnSuccessListener {
                                            Toast.makeText(this, "Başarıyla Güncellendi", Toast.LENGTH_SHORT).show()
                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(this, "Güncelleme Başarısız", Toast.LENGTH_SHORT).show()
                                        }
                                }
                                .addOnFailureListener {
                                    Toast.makeText(this, "Şifre Güncelleme Başarısız", Toast.LENGTH_SHORT).show()
                                }
                        }
                        ?.addOnFailureListener {
                            Toast.makeText(this, "E-posta Adresi Güncelleme Başarısız", Toast.LENGTH_SHORT).show()
                        }
                }
                ?.addOnFailureListener {
                    Toast.makeText(this, "Profil Güncelleme Başarısız", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
