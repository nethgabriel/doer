package com.example.doer

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.view.View
import android.view.ViewStub
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RadioButton
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible

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
                        val backButton = findViewById<ImageButton>(R.id.btn_back)
                        val nextButton = findViewById<Button>(R.id.btn_next)

                        //specifies which next button in a layout
                        val emailNextButton = emailSection.findViewById<Button>(R.id.btn_next)
                        val passwordNextButton = passwordSection.findViewById<Button>(R.id.btn_next)

//                        //paired each next button to its section
//                        val sectionPair = listOf(
//                            emailSection to emailNextButton,
//                            passwordSection to passwordNextButton
//                        )

                        //initialized list for available sections
                        //add here if adding sections
                        val section = listOf(emailSection, passwordSection)
                        var sectionIndex = 0

                        //initialized live section
                        var currentSection: View? = null

                        //dynamically changes visibility of sections
                        fun updateStepState() {
                            section.forEachIndexed { index, section ->
                                section.visibility = if (index == sectionIndex) View.VISIBLE else View.GONE

                                //saved live section
                                if (isVisible) currentSection = section
                            }
                        }

                        //closes "login_form.xml" without terminating functionality
                        closeButton.setOnClickListener {
                            visibility = View.GONE
                        }

                        //access previous user inputs
                        backButton.setOnClickListener {
                            sectionIndex--
                            updateStepState()

                            //used live section to target specific button
                            val enabledButton = currentSection?.findViewById<Button>(R.id.btn_next)
                            enabledButton?.isEnabled = true
                        }

                        nextButton.setOnClickListener {
                            if (sectionIndex < section.size - 1) {
                                sectionIndex++
                                updateStepState()
                            }
                        }

                        //set initial value to false for section control
                        emailNextButton.isEnabled = false
                        passwordNextButton.isEnabled = false

                        val emailInput = findViewById<EditText>(R.id.input_email)

                        //listens to email input field
                        emailInput.addTextChangedListener(object : TextWatcher {
                            override fun onTextChanged(
                                s: CharSequence?,
                                start: Int,
                                before: Int,
                                count: Int) {

                                val emailInputString = s.toString()

                                //uses android library to check for emailInputString syntax
                                //does not check for domain existence
                                val emailValid = Patterns.EMAIL_ADDRESS.matcher(emailInputString).matches()

                                if (emailValid) {
                                    emailNextButton.isEnabled = true
                                    emailInput.error = null
                                } else {
                                    emailNextButton.isEnabled = false
                                    emailInput.error = "Please enter a valid email address"
                                }
                            }

                            //unused text watcher function
                            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                            override fun afterTextChanged(s: Editable?) {}
                        })

                        //input password will scan for the following parameters
                        fun passwordAuthentication(password: String): List<Boolean> {
                            val hasNumber = password.any { it.isDigit() }
                            val hasSpecialChar = password.any { "!@#\$%^&*()-_=+[{]}|;:'\",<.>/?".contains(it) }
                            val hasMinLength = password.length >= 10

                            return listOf(hasNumber, hasSpecialChar, hasMinLength)
                        }

                        val passwordGuide = resources.getStringArray(R.array.password_guide)
                        val passwordGuideBlock = findViewById<LinearLayout>(R.id.block_password_guide)
                        val passwordInput = findViewById<EditText>(R.id.input_password)


                        //dynamically create radioButtons
                        val radioButtons = mutableListOf<RadioButton>()
                        passwordGuide.forEach { condition ->
                            val radioButtonStyle = ContextThemeWrapper(context, R.style.CustomRadioButtonStyle)
                            val radioButton = RadioButton(radioButtonStyle)

                            radioButton.text = condition
                            radioButton.isClickable = false
                            radioButtons.add(radioButton)
                            passwordGuideBlock.addView(radioButton)
                            radioButton.setTextColor(ContextCompat.getColor(context, R.color.gray))
                        }

                        //check whether radio buttons are checked
                        fun radioButtonActive(): Boolean {
                            return radioButtons.all { it.isChecked }
                        }

                        val confirmPasswordInput = findViewById<EditText>(R.id.input_confirm_password)

                        //initially disable confirm input field
                        confirmPasswordInput.isEnabled = false

                        //listen for password input
                        passwordInput.addTextChangedListener(object : TextWatcher {
                            override fun onTextChanged(
                                s: CharSequence?,
                                start: Int,
                                before: Int,
                                count: Int) {

                                //implements passwordAuthentication function to string input
                                val checks = passwordAuthentication(s.toString())

                                //cycle through passwordAuthentication function to check for conditions, if true, radioButton = active
                                checks.forEachIndexed { index, passed ->
                                    radioButtons[index].isChecked = passed
                                }

                                //this enables confirm password input field
                                if(radioButtonActive()) {
                                    confirmPasswordInput.isEnabled = true

                                    //included text watcher for password authentication matching
                                    confirmPasswordInput.addTextChangedListener(object : TextWatcher {
                                        override fun onTextChanged(
                                            s: CharSequence?,
                                            start: Int,
                                            before: Int,
                                            count: Int
                                        ) {

                                            val confirmPasswordText = confirmPasswordInput.text.toString()
                                            val passwordText = passwordInput.text.toString()

                                            //enables next button on password section
                                            if (confirmPasswordText == passwordText) {
                                                passwordNextButton.isEnabled = true
                                                passwordInput.error = null

                                            } else {
                                                confirmPasswordInput.error = "Password mismatch."
                                            }
                                        }

                                        //unused "TextWatcher" functions
                                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                                        override fun afterTextChanged(s: Editable?) {}
                                    })

                                } else {
                                    passwordNextButton.isEnabled = false

                                    //set error message depending on validation error
                                    when {
                                        !s.toString().contains(Regex("\\d")) -> passwordInput.error = "Password must contain at least one number."
                                        !s.toString().contains(Regex("[!@#\$%^&*()\\-_=+\\[{\\]}|;:'\",<.>/?]")) -> passwordInput.error = "Password must contain at least one special character (e.g., #, ?, !, $)."
                                        s.toString().length < 10 -> passwordInput.error = "Password must be at least 10 characters long."
                                        //s.toString().contains(confirmPasswordText) -> passwordInput.error = "Password mismatch."
                                        else -> passwordInput.error = null // Clear error if everything is fine
                                    }
                                }
                            }

                            //unused "TextWatcher" functions
                            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                            override fun afterTextChanged(s: Editable?) {}
                        })

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