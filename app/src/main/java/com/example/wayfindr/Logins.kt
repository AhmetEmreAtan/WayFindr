package com.example.wayfindr

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AlertDialog
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.wayfindr.databinding.ActivityLoginsBinding
import com.google.firebase.database.*
import com.google.firebase.auth.FirebaseAuth

class Logins : AppCompatActivity() {

    private lateinit var binding: ActivityLoginsBinding
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private val firebaseAuth = FirebaseAuth.getInstance()
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseDatabase = FirebaseDatabase.getInstance()
        databaseReference = firebaseDatabase.reference.child("users")

        sharedPreferences = getSharedPreferences("MY_PRE", Context.MODE_PRIVATE)
        val getemail = sharedPreferences.getString("EMAIL", "")
        val getpassword = sharedPreferences.getString("PASSWORD", "")

        if (getemail != "" && getpassword != "") {
            val i = Intent(this, Profile::class.java)
            startActivity(i)
        }

        binding.singIn.setOnClickListener {

            val email = binding.eMail.text.toString()
            val password = binding.passwords.text.toString()
            val editor = sharedPreferences.edit()
            editor.putString("EMAIL", email)
            editor.putString("PASSWORD", password)
            editor.apply()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginUser(email, password)
            } else {
                Toast.makeText(this, "Tüm alanları doldurmak zorunludur", Toast.LENGTH_SHORT).show()
            }
        }

        binding.forgetPassword.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            val dialogView = layoutInflater.inflate(R.layout.dialog_forget, null)
            val userEmail = dialogView.findViewById<EditText>(R.id.editBox)

            builder.setView(dialogView)
            val dialog = builder.create()

            dialogView.findViewById<Button>(R.id.btnReset).setOnClickListener {
                compareEmail(userEmail)
                dialog.dismiss()
            }
            dialogView.findViewById<Button>(R.id.btnCancel).setOnClickListener {
                dialog.dismiss()
            }
            if (dialog.window != null) {
                dialog.window!!.setBackgroundDrawable(ColorDrawable(0))
            }
            dialog.show()
        }

        val letRegisterTxt = binding.singUp
        letRegisterTxt.setOnClickListener {
            val intent = Intent(this, Register::class.java)
            startActivity(intent)
        }
    }

    private fun compareEmail(email: EditText) {
        if (email.text.toString().isEmpty()) {
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email.text.toString()).matches()) {
            return
        }
        firebaseAuth.sendPasswordResetEmail(email.text.toString()).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "E-Postanızı kontrol edin.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loginUser(email: String, password: String) {
        databaseReference.orderByChild("email").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (userSnapshot in dataSnapshot.children) {
                            val userData = userSnapshot.getValue(UserData::class.java)

                            if (userData != null && userData.password == password) {
                                Toast.makeText(this@Logins, "Giriş işlemi gerçekleşti.", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this@Logins, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                        }
                    }
                    else Toast.makeText(this@Logins, "Giriş işlemi gerçekleşmedi.", Toast.LENGTH_SHORT).show()
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(
                        this@Logins,
                        "Database Error : ${databaseError.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
}
