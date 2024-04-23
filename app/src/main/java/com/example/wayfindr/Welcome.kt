package com.example.wayfindr

import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.wayfindr.databinding.FragmentWelcomeBinding

class Welcome : AppCompatActivity() {

    private lateinit var binding: FragmentWelcomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.textViewSwipe.setOnTouchListener(object : View.OnTouchListener {
            private var downX: Float = 0f

            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                event?.let {
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            downX = event.x
                            return true
                        }
                        MotionEvent.ACTION_UP -> {
                            val upX = event.x
                            if (Math.abs(upX - downX) > 100) { // Basit bir sağa kaydırma hareketi
                                navigateToMainActivity()
                                return true
                            }
                        }
                    }
                }
                return false
            }
        })
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
