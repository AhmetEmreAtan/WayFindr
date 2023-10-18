package com.example.wayfindr



import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.wayfindr.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth

class Profile : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var auth : FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(layoutInflater)
        val view = binding.root

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser != null) {
            val userEmail = currentUser.email


            binding.profileEmail.text = userEmail ?: "E-Posta Yok"
        } else {
            // Kullanıcı oturum açmamışsa, giriş yapma işlemine yönlendir
            binding.profileEmail.text = "Giriş Yapınız"
        }

        binding.favButton.setOnClickListener {
            val intent = Intent(requireContext(), Favorites::class.java)
            startActivity(intent)
        }

        binding.settingButton.setOnClickListener {
            val intent = Intent(requireContext(), Setting::class.java)
            startActivity(intent)
        }

        binding.adminButton.setOnClickListener {
            val intent = Intent(requireContext(), AdminEdit::class.java)
            startActivity(intent)
        }

        return view
    }
}