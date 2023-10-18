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
    private lateinit var uid : String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(layoutInflater)
        val view = binding.root



        auth = FirebaseAuth.getInstance()
        uid = auth.currentUser?.uid.toString()


        val sharedPreferences = requireActivity().getSharedPreferences("MY_PRE", Context.MODE_PRIVATE)

        val email = sharedPreferences.getString("EMAIL", "")
        binding.profileName.text = "$email"

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
