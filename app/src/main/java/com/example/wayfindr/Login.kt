package com.example.wayfindr


import android.content.Intent
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.wayfindr.databinding.ActivityLoginBinding
import com.google.firebase.database.*

class Login : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseDatabase = FirebaseDatabase.getInstance()
        databaseReference =firebaseDatabase.reference.child("users")

        binding.singIn.setOnClickListener {

            val email = binding.eMail.text.toString()
            val password = binding.passwords.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginUser( email, password)
            } else {
                Toast.makeText(this@Login, "Tüm alanları doldurmak zorunludur", Toast.LENGTH_SHORT).show()
            }
        }

        binding.forgetPassword.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            val view = layoutInflater.inflate(R.layout.dialog_forget, null)
            val userEmail = view.findViewById<EditText>(R.id.editBox)

            builder.setView(view)
            val dialog = builder.create()

            view.findViewById<Button>(R.id.btnReset).setOnClickListener {
                compareEmail(userEmail)
                dialog.dismiss()
            }
            view.findViewById<Button>(R.id.btnCancel).setOnClickListener {
                dialog.dismiss()
            }
            if (dialog.window != null) {
                dialog.window!!.setBackgroundDrawable(ColorDrawable(0))
            }
            dialog.show()
        }

        // Diğer kodlar buraya gelebilir
    }

    private fun compareEmail(email: EditText) {
        val userEmail = email.text.toString().trim()

        if (userEmail.isEmpty()) {
            return
        }

        // Kullanıcının e-posta adresini Firebase Realtime Database'de kontrol edin
        databaseReference.child(userEmail).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    // E-posta veritabanında bulundu, sıfırlama bağlantısı gönderin
                    // Burada sıfırlama bağlantısı gönderme işlemini gerçekleştirebilirsiniz
                    // Örneğin, kullanıcıya sıfırlama bağlantısını e-posta ile göndermek için JavaMail gibi bir kütüphane kullanabilirsiniz.
                    Toast.makeText(this@Login, "E-postanızı kontrol edin.", Toast.LENGTH_SHORT).show()
                } else {
                    // E-posta veritabanında bulunamadı
                    Toast.makeText(this@Login, "Bu e-posta kayıtlı değil.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Veritabanı hatası durumunda işlem yapabilirsiniz
                Toast.makeText(this@Login, "Veritabanı hatası: ${databaseError.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loginUser(email:String, password: String){
        databaseReference.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot.exists()){
                    for(userSnapshot in dataSnapshot.children){
                        val userData = userSnapshot.getValue(UserData::class.java)

                        if(userData != null && userData.password == password){
                            Toast.makeText(this@Login, "Giriş işlemi gerçekleşti.", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@Login,Profile_page::class.java))
                            finish()
                            return
                        }
                    }
                }
                Toast.makeText(this@Login, "Giriş işlemi gerçekleşmedi.", Toast.LENGTH_SHORT).show()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(
                    this@Login,
                    "Database Error : ${databaseError.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}
