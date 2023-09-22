package com.example.wayfindr


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.wayfindr.databinding.ActivityProfilePageBinding

class Profile : Fragment() {

    private lateinit var binding: ActivityProfilePageBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ActivityProfilePageBinding.inflate(inflater, container, false)
        val view = binding.root

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
