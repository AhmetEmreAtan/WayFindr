package com.example.wayfindr

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContentProviderCompat.requireContext
import com.example.wayfindr.databinding.ActivitySettingBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class     Setting : AppCompatActivity() {

    private lateinit var binding: ActivitySettingBinding
    private lateinit var firebaseDatabase: FirebaseDatabase

    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        sharedPreferences = getSharedPreferences("MY_PRE", Context.MODE_PRIVATE)

        binding.aboutButton.setOnClickListener {

            val builder= AlertDialog.Builder(this)
            val view = layoutInflater.inflate(R.layout.dialog_about_us,null)

            builder.setView(view)
            val dialog = builder.create()
            dialog.show()

        }

       binding.logOutButton.setOnClickListener {
           auth.signOut()
           clearUserPreference()

           val transaction = supportFragmentManager.beginTransaction()
           val loginFragment = Login()
           transaction.replace(R.id.constraint_layout, loginFragment)
           transaction.addToBackStack(null)
           transaction.commit()
       }
        binding.deleteAccountButton.setOnClickListener {
            val builder = AlertDialog.Builder(this)
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
                            Toast.makeText(this, "Hesap silme işlemi gerçekleşti !!", Toast.LENGTH_SHORT).show()

                            val transaction = supportFragmentManager.beginTransaction()
                            val loginFragment = Login()
                            transaction.replace(R.id.constraint_layout, loginFragment)
                            transaction.addToBackStack(null)
                            transaction.commit()
                        } else {
                            Log.e("error:", authTask.exception.toString())
                        }
                    }
                } else {
                    Log.e("error:", "kullanıcı yok")
                }
            }
            builder.setNegativeButton("No") { _, _ -> }

            val alertDialog = builder.create()
            alertDialog.show()
        }
    }
    private fun clearUserPreference(){
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
    }
}
