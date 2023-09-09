package com.example.wayfindr

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.wayfindr.Login
import com.example.wayfindr.R
import com.example.wayfindr.UserData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Profile : Fragment() {

    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        val signUpButton = view.findViewById<Button>(R.id.singUp)

        firebaseDatabase = FirebaseDatabase.getInstance()
        databaseReference = firebaseDatabase.reference.child("users")

        signUpButton.setOnClickListener {
            val name = view.findViewById<TextView>(R.id.name).text.toString()
            val email = view.findViewById<TextView>(R.id.eMail2).text.toString()
            val password = view.findViewById<TextView>(R.id.passwords2).text.toString()

            if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                signupUser(name, email, password)
            } else {
                Toast.makeText(requireContext(), "Tüm alanları doldurmak zorunludur", Toast.LENGTH_SHORT).show()
            }
        }

        val notSignupTxt = view.findViewById<TextView>(R.id.notSingUp)
        notSignupTxt.setOnClickListener {
            val intent = Intent(requireActivity(), Profile_page::class.java)
            startActivity(intent)
        }

        return view
    }

    private fun signupUser(name: String, email: String, password: String) {
        databaseReference.orderByChild("name").equalTo(name)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (!dataSnapshot.exists()) {
                        val id = databaseReference.push().key
                        val userData = UserData(id, name, email, password)
                        databaseReference.child(id!!).setValue(userData)
                        Toast.makeText(requireContext(), "Kayıt gerçekleşti", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(requireContext(), Login::class.java))
                        requireActivity()
                    } else {
                        Toast.makeText(requireContext(), "Kullanıcı zaten var", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(
                        requireContext(),
                        "Database Error : ${databaseError.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
}
