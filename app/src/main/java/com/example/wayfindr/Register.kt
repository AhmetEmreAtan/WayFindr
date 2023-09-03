package com.example.giris

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.giris.databinding.ActivityLoginBinding
import com.example.giris.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth

class Register : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        fun showToast(message: String) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
        binding.singUp.setOnClickListener{

            val name =binding.name.text.toString()
            val date = binding.date.text.toString()
            val gender = binding.gender.text.toString()
            val email = binding.eMail2.text.toString()
            val passwords = binding.passwords2.text.toString()
            val confirmPasswords = binding.confirmPasswords2.text.toString()

            if(name.isNotEmpty() || date.isNotEmpty() || gender.isNotEmpty() || email.isNotEmpty() || passwords.isNotEmpty() || confirmPasswords.isNotEmpty()){
                if(passwords == confirmPasswords){

                    firebaseAuth.createUserWithEmailAndPassword(email,passwords).addOnCompleteListener{
                        if (it.isSuccessful){
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                        }
                        else{
                            Toast.makeText(this,it.exception.toString(), Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                else{
                    Toast.makeText(this,"Şifre giriniz", Toast.LENGTH_SHORT).show()
                }
            }
            else{
                showToast("Lütfen tüm boş alanları doldurduğunuza emin olun.")
            }
        }

        binding.notSingUp.setOnClickListener {

        }

        binding.letLogIn.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }
    }
}