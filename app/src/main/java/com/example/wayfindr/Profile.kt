package com.example.wayfindr


import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.wayfindr.R
import com.example.wayfindr.UserData
import com.example.wayfindr.databinding.FragmentProfileBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.database.*

class Profile : Fragment() {


    private lateinit var binding: FragmentProfileBinding
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private val firebaseAuth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        val signUpButton = view.findViewById<Button>(R.id.singUp)

        firebaseDatabase = FirebaseDatabase.getInstance()
        databaseReference = firebaseDatabase.reference.child("users")
        auth = FirebaseAuth.getInstance()



        signUpButton.setOnClickListener {
            val name = view.findViewById<TextView>(R.id.name).text.toString()
            val email = view.findViewById<TextView>(R.id.eMail2).text.toString()
            val password = view.findViewById<TextView>(R.id.passwords2).text.toString()

            if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {

                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {

                            val user = auth.currentUser
                            val id = user?.uid

                            val userData = UserData(id, name, email, password)
                            databaseReference.child(id!!).setValue(userData)

                            Toast.makeText(
                                requireContext(),
                                "Kayıt işlemi başarılı",
                                Toast.LENGTH_SHORT
                            ).show()


                            val intent = Intent(requireContext(), Login::class.java)
                            startActivity(intent)
                        } else {
                            if (task.exception is FirebaseAuthUserCollisionException) {
                                Toast.makeText(
                                    requireContext(),
                                    "Bu e-posta adresi zaten kullanılıyor.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    "Kullanıcı kaydı başarısız: " + task.exception?.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
            } else {
                Toast.makeText(requireContext(), "Tüm alanları doldurmak zorunludur", Toast.LENGTH_SHORT).show()
            }
        }


        val notSignupTxt = view.findViewById<TextView>(R.id.notSingUp)
        notSignupTxt.setOnClickListener {
            val intent = Intent(requireActivity(), Profile_page::class.java)
            startActivity(intent)
        }
        val letLoginTxt = view.findViewById<TextView>(R.id.letLogIn)
        letLoginTxt.setOnClickListener {
            val intent = Intent(requireActivity(),Login::class.java)
            startActivity(intent)
        }

        return view
    }


}
