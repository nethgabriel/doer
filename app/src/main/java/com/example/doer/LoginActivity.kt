package com.example.doer

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.view.View
import android.widget.Button

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_user_authentication)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val signUpButton = findViewById<Button>(R.id.btn_sign_up)
        val signUpOverlay = findViewById<View>(R.id.sign_up_overlay)
       // val closeButton = signUpOverlay.findViewById<Button>(R.id.btn_close)

        signUpButton.setOnClickListener {
            signUpOverlay.visibility = View.VISIBLE
        }

      /*  closeButton.setOnClickListener {
            signUpOverlay.visibility = View.GONE
        }*/





    }
}