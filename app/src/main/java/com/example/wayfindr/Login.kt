package com.example.wayfindr

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.wayfindr.databinding.FragmentLoginBinding
import com.google.firebase.auth.FirebaseAuth

class Login : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.singIn.setOnClickListener {
            val email = binding.eMail.text.toString()
            val password = binding.passwords.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                signInUser(email, password)
            } else {
                Toast.makeText(requireContext(), "Tüm alanları doldurmak zorunludur", Toast.LENGTH_SHORT).show()
            }
        }

        binding.forgetPassword.setOnClickListener {
            val email = binding.eMail.text.toString()

            if (email.isNotEmpty()) {
                resetPassword(email)
            } else {
                Toast.makeText(requireContext(), "Lütfen e-posta adresinizi girin", Toast.LENGTH_SHORT).show()
            }
        }

        val letRegisterTxt = binding.singUp
        letRegisterTxt.setOnClickListener {
            // Kayıt sayfasına yönlendirme kodu
            val intent = Intent(requireContext(), Register::class.java)
            startActivity(intent)
        }

        val without_logging_in = binding.withoutLoggingIn
        without_logging_in.setOnClickListener {
            val intent = Intent(requireContext(), MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun signInUser(email: String, password: String) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Giriş başarılı, kullanıcıyı yönlendirme kodu
                    val intent = Intent(requireContext(), MainActivity::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(requireContext(), "Giriş başarısız. Hata: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun resetPassword(email: String) {
        firebaseAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(requireContext(), "Şifre sıfırlama e-postası gönderildi.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Şifre sıfırlama başarısız. Hata: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
