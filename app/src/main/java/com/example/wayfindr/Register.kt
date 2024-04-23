package com.example.wayfindr

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Register : AppCompatActivity() {

    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        firebaseDatabase = FirebaseDatabase.getInstance()
        databaseReference = firebaseDatabase.reference.child("users")
        auth = FirebaseAuth.getInstance()

        val signUpButton = findViewById<TextView>(R.id.signUp)
        val userNameField = findViewById<TextView>(R.id.userName)

        signUpButton.setOnClickListener {
            val name = findViewById<TextView>(R.id.firstName).text.toString()
            val email = findViewById<TextView>(R.id.eMail2).text.toString()
            val password = findViewById<TextView>(R.id.passwords2).text.toString()
            val userName = userNameField.text.toString()

            if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && userName.isNotEmpty()) {
                checkUsernameUnique(userName, onSuccess = {
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val user = auth.currentUser

                                saveUserNameToDatabase(user?.uid, userName)

                                val userData = UserData(user?.uid, name, email, userName, "")
                                databaseReference.child(user?.uid!!).setValue(userData)
                                    .addOnCompleteListener { databaseTask ->
                                        if (databaseTask.isSuccessful) {
                                            Toast.makeText(
                                                applicationContext,
                                                "Kayıt işlemi başarılı",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            val intent = Intent(applicationContext, Login::class.java)
                                            startActivity(intent)
                                        } else {
                                            Toast.makeText(
                                                applicationContext,
                                                "Firebase Realtime Database'e kayıt yapılırken bir hata oluştu: " + databaseTask.exception?.message,
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
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
                }, onFail = {
                    Toast.makeText(applicationContext, "Kullanıcı adı zaten kullanılıyor. Lütfen başka bir kullanıcı adı seçin.", Toast.LENGTH_LONG).show()
                })
            } else {
                Toast.makeText(applicationContext, "Tüm alanları doldurmak zorunludur", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun saveUserNameToDatabase(userId: String?, userName: String) {
        if (userId != null) {
            val databaseReference = firebaseDatabase.reference.child("usernames")
            databaseReference.child(userName).setValue(userId)
        }
    }

    // Kullanıcı adının benzersizliğini kontrol etme
    private fun checkUsernameUnique(userName: String, onSuccess: () -> Unit, onFail: () -> Unit) {
        firebaseDatabase.reference.child("usernames").child(userName)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        onFail()
                    } else {
                        onSuccess()
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(applicationContext, "Veritabanı hatası: ${databaseError.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }


}
