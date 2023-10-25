package com.example.wayfindr

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.wayfindr.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.auth.FirebaseAuthUserCollisionException

class Register : AppCompatActivity() {

    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private val firebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        var sharedPreferences = this.getSharedPreferences("MY_PRE",Context.MODE_PRIVATE)


        val signUpButton = findViewById<TextView>(R.id.signUp)

        firebaseDatabase = FirebaseDatabase.getInstance()
        databaseReference = firebaseDatabase.reference.child("users")
        auth = FirebaseAuth.getInstance()

        signUpButton.setOnClickListener {
            val name = findViewById<TextView>(R.id.name).text.toString()
            val email = findViewById<TextView>(R.id.eMail2).text.toString()
            val password = findViewById<TextView>(R.id.passwords2).text.toString()
            val userName = findViewById<TextView>(R.id.userName).text.toString()

            sharedPreferences.edit().putString("NAME", name).apply()


            if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {

                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {

                            val user = auth.currentUser
                            val id = user?.uid

                            val userData = UserData(id, name, email, password, userName)
                            databaseReference.child(id!!).setValue(userData)

                            Toast.makeText(
                                applicationContext,
                                "Kayıt işlemi başarılı",
                                Toast.LENGTH_SHORT
                            ).show()

                            val intent = Intent(applicationContext, Login::class.java)
                            startActivity(intent)
                        } else {
                            if (task.exception is FirebaseAuthUserCollisionException) {
                                Toast.makeText(
                                    applicationContext,
                                    "Bu e-posta adresi zaten kullanılıyor.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Toast.makeText(
                                    applicationContext,
                                    "Kullanıcı kaydı başarısız: " + task.exception?.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
            } else {
                Toast.makeText(applicationContext, "Tüm alanları doldurmak zorunludur", Toast.LENGTH_SHORT).show()
            }
        }

        val notSignupTxt = findViewById<TextView>(R.id.notSingUp)
        notSignupTxt.setOnClickListener {
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
        }
        val letLoginTxt = findViewById<TextView>(R.id.letLogIn)
        letLoginTxt.setOnClickListener {
            val intent = Intent(applicationContext, Profile::class.java)
            startActivity(intent)
        }
    }
}
