package com.example.doer

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.view.View
import android.view.ViewStub
import android.widget.Button
import android.widget.ImageButton

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

        //this is to make "signupOverlay" nullable
        var signUpOverlay: View? = null

        val signUpStub = findViewById<ViewStub>(R.id.sign_up_stub)
        val signUpButton = findViewById<Button>(R.id.btn_sign_up)

        signUpButton.setOnClickListener {
            if (signUpOverlay == null) {

                //this makes the objects in "login_form.xml" functional and viewable
                try {
                    signUpOverlay = signUpStub.inflate().apply {
                        val closeButton = findViewById<ImageButton>(R.id.btn_close_signup)
                        val emailSection = findViewById<View>(R.id.section_enter_email)
                        val passwordSection = findViewById<View>(R.id.section_create_password)
                        val nextButton = findViewById<Button>(R.id.btn_next)
                        val backButton = findViewById<ImageButton>(R.id.btn_back)

                        //closes "login_form.xml" without terminating functionality
                        closeButton.setOnClickListener {
                            visibility = View.GONE
                        }

                        //access previous user inputs
                        backButton.setOnClickListener {
                            emailSection.visibility = View.VISIBLE
                            passwordSection.visibility = View.GONE
                        }

                        //switches user inputs for profile creation
                        nextButton.setOnClickListener {
                            emailSection.visibility = View.GONE
                            passwordSection.visibility = View.VISIBLE
                        }
                    }
                } catch (e: Exception) {
                    TODO("Not yet implemented")
                }
            } else {
                //shows "login_form.xml"
                signUpOverlay?.visibility = View.VISIBLE
            }
        }



    }
}