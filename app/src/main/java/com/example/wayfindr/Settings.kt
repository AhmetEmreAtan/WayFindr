package com.example.wayfindr

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.wayfindr.databinding.FragmentSettingsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.example.yourapp.BottomSheetFragment

class Settings : Fragment() {

    private lateinit var binding: FragmentSettingsBinding
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        sharedPreferences = requireContext().getSharedPreferences("MY_PRE", Context.MODE_PRIVATE)

        binding.aboutButton.setOnClickListener {
            val bottomSheetFragment = BottomSheetFragment()
            bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)
        }

        binding.logOutButton.setOnClickListener {
            auth.signOut()
            clearUserPreference()

            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            val loginFragment = Login()
            transaction.replace(R.id.frame_layout, loginFragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }

        binding.deleteAccountButton.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Kullanıcı Sil")
            builder.setMessage("Kullanıcı silinsin mi ?")
            builder.setIcon(R.drawable.baseline_delete_24)
            builder.setCancelable(false)

            builder.setPositiveButton("Yes") { _, _ ->
                val user = auth.currentUser

                if (user != null) {
                    val databaseReference = FirebaseDatabase.getInstance().getReference()
                    val userUid = user.uid
                    databaseReference.child("users").child(userUid).removeValue()

                    user.delete().addOnCompleteListener { authTask ->
                        if (authTask.isSuccessful) {
                            Toast.makeText(requireContext(), "Hesap silme işlemi gerçekleşti !!", Toast.LENGTH_SHORT).show()

                            val transaction = requireActivity().supportFragmentManager.beginTransaction()
                            val loginFragment = Login()
                            transaction.replace(R.id.frame_layout, loginFragment)
                            transaction.addToBackStack(null)
                            transaction.commit()
                        } else {
                            // Hata durumu
                        }
                    }
                } else {
                    // Kullanıcı null durumunda yapılacak işlemler
                }
            }

            builder.setNegativeButton("No") { _, _ -> }

            val alertDialog = builder.create()
            alertDialog.show()
        }
    }

    private fun clearUserPreference() {
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
    }
}
