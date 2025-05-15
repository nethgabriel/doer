package com.example.doer

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.view.View
import android.view.ViewGroup
import android.view.ViewStub
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import android.text.InputType
import android.view.MotionEvent
import android.widget.CheckBox
import java.util.Calendar
import android.view.LayoutInflater
import android.widget.ScrollView
import androidx.appcompat.app.AlertDialog


class LoginActivity : AppCompatActivity() {
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_user_authentication)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // TODO: create another view stub for login
        //this is to make "signupOverlay" nullable
        var signUpOverlay: View? = null

        val signUpStub = findViewById<ViewStub>(R.id.sign_up_stub)
        val signUpButton = findViewById<Button>(R.id.btn_sign_up)

        signUpButton.setOnClickListener {
            if (signUpOverlay == null) {

                //this makes the objects in "signup_form.xml" functional and viewable
                try {
                    signUpOverlay = signUpStub.inflate().apply {
                        val closeButton = findViewById<ImageButton>(R.id.btn_close_signup)
                        val emailSection = findViewById<View>(R.id.section_enter_email)
                        val passwordSection = findViewById<View>(R.id.section_create_password)
                        val birthdateSection = findViewById<View>(R.id.section_birthdate)
                        val usernameSection = findViewById<View>(R.id.section_username)

                        //specifies which next button in a layout
                        val emailNextButton = emailSection.findViewById<Button>(R.id.btn_next)
                        val passwordNextButton = passwordSection.findViewById<Button>(R.id.btn_next)
                        val birthdateNextButton = birthdateSection.findViewById<Button>(R.id.btn_next)

                        // TODO: function after personal information
                        val createAccountButton = usernameSection.findViewById<Button>(R.id.btn_create_account)
                        createAccountButton.isEnabled = false

                        //initialized list for available sections
                        //add here if adding sections
                        val section = listOf(emailSection, passwordSection, birthdateSection, usernameSection)
                        var sectionIndex = 0

                        //initialized live section
                        //this is used, DO NOT DELETE, dismiss variable never accessed
                        var currentSection: View? = null

                        val termsCheckbox = findViewById<CheckBox>(R.id.checkbox_req_term_privacy)
                        var termsCheckboxWaitAgree = false
                        termsCheckbox.isChecked = false


                        //targets button depending on which section is active
                        val nextButtons: List<Button?> = section.map { it.findViewById(R.id.btn_next) }

                        val passwordGuide = resources.getStringArray(R.array.password_guide)
                        val passwordGuideBlock = findViewById<LinearLayout>(R.id.block_password_guide)
                        val passwordInput = findViewById<EditText>(R.id.input_password)
                        val confirmPasswordInput = findViewById<EditText>(R.id.input_confirm_password)

                        val layoutAgePicker = findViewById<LinearLayout>(R.id.container_date_picker)

                        //custom age selector style
                        val dateStyle = ContextThemeWrapper(context, R.style.SpinnerDatePickerDialogDark)
                        val datePicker = DatePicker(dateStyle, null, android.R.attr.datePickerStyle).apply {
                            calendarViewShown = false
                            spinnersShown = true

//                            val calendar = Calendar.getInstance()
//                            calendar.add(Calendar.YEAR, -13)
//                            maxDate = calendar.timeInMillis
                        }



                        //dynamically changes visibility of sections
                        fun updateStepState() {
                            section.forEachIndexed { index, section ->
                                section.visibility = if (index == sectionIndex) View.VISIBLE else View.GONE

                                //save live section
                                if (index == sectionIndex) currentSection = section
                            }
                        }

                        //clear all text input fields
                        fun clearAllEditTexts(viewGroup: ViewGroup) {
                            for (i in 0 until viewGroup.childCount) {
                                when (val view = viewGroup.getChildAt(i)) {
                                    is EditText -> view.text.clear()
                                    is ViewGroup -> clearAllEditTexts(view)
                                }
                            }
                        }

                        fun disableAllNextButton() {
                            //disable all next buttons initially
                            nextButtons.forEach {
                                if (it != null) {
                                    it.isEnabled = false
                                }
                            }

                            //open birthdate next button for development
                            //nextButtons[2].isEnabled = true
                        }

                        // TODO: include snackbar for success message
                        // TODO: refactor snackbar: not sure on placement
                        // Global flag to track Snackbar visibility
                        var isSnackbarVisible = false
                        // Global variable to store the reference to the current Snackbar
                        var currentSnackbar: Snackbar? = null

                        fun showSnackbarError( anchorView: View, message: String) {
                            // If a Snackbar is already visible, do not show it again
                            if (isSnackbarVisible) return

                            //val rootContainer = (anchorView.context as Activity).window.decorView.findViewById<ViewGroup>(android.R.id.content)

                            // Show Snackbar and set the flag to true
                            currentSnackbar = Snackbar.make(anchorView, message, Snackbar.LENGTH_LONG).setAnchorView(anchorView).apply {

                                val snackbarLayout = this.view
                                val textView = snackbarLayout.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)

                                val drawable = ContextCompat.getDrawable(view.context, R.drawable.error_icon)
                                drawable?.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
                                textView.setCompoundDrawablesRelative(drawable, null, null, null)
                                textView.compoundDrawablePadding = 30

                                textView.setTextColor(ContextCompat.getColor(view.context, R.color.white))
                                textView.textSize = 16f
                                textView.setTypeface(Typeface.DEFAULT_BOLD)

                                addCallback(object : Snackbar.Callback() {
                                    override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                                        isSnackbarVisible = false
                                    }
                                })
                                show()
                            }

                            // Set the flag to true when Snackbar is shown
                            isSnackbarVisible = true
                        }

                        //password visibility toggle
                        fun setupPasswordVisibilityToggle(
                            editText: EditText,
                            iconVisible: Int = R.drawable.visibility_on,
                            iconHidden: Int = R.drawable.visibility_off
                        ) {
                            var isPasswordVisible = false
                            editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, iconHidden, 0)

                            editText.setOnTouchListener { _, event ->
                                if (event.action == MotionEvent.ACTION_UP) {
                                    val drawableEnd = 2
                                    val drawable = editText.compoundDrawables[drawableEnd]
                                    val extraTapArea = 100 // increase this value as needed

                                    if (drawable != null && event.rawX >= (editText.right - drawable.bounds.width() - extraTapArea)) {
                                        isPasswordVisible = !isPasswordVisible
                                        if (isPasswordVisible) {
                                            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                                            editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, iconVisible, 0)
                                        } else {
                                            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                                            editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, iconHidden, 0)
                                        }
                                    }
                                }
                                false
                            }
                        }

                        fun hidePassword(editText: EditText, iconHidden: Int = R.drawable.visibility_off) {
                            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                            editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, iconHidden, 0)
                        }

                        fun resetDatePickerToToday(datePicker: DatePicker) {
                            val today = Calendar.getInstance()
                            datePicker.updateDate(
                                today.get(Calendar.YEAR),
                                today.get(Calendar.MONTH),
                                today.get(Calendar.DAY_OF_MONTH)
                            )
                        }


                        //todo snack bar success

                        //closes "signup_form.xml" without terminating functionality
                        closeButton.setOnClickListener {
                            visibility = View.GONE
                            termsCheckbox.isChecked = false
                            createAccountButton.isEnabled = false
                            resetDatePickerToToday(datePicker)
                            hidePassword(passwordInput)
                            hidePassword(confirmPasswordInput)
                            clearAllEditTexts(signUpOverlay as ViewGroup)

                            //dismiss the Snackbar if it's visible and the email is valid
                            currentSnackbar?.dismiss()

                            //reset the currentSnackbar reference to null
                            currentSnackbar = null
                            sectionIndex = 0
                            updateStepState()
                            disableAllNextButton()
                        }

                        //initialized back button for each section
                        val backButtons = section.mapIndexedNotNull { index, view ->
                            val backButton = view.findViewById<ImageButton?>(R.id.btn_back)

                            //checks for non-existent back button, if non-existent return null to index
                            if (backButton != null) index to backButton else null
                        }

                        //back button function
                        backButtons.forEach { (index, button) ->
                            button.setOnClickListener {
                                if (index > 0) {
                                    sectionIndex = index - 1
                                    updateStepState()

//                                    val enabledButton = currentSection?.findViewById<Button>(R.id.btn_next)
//                                    enabledButton?.isEnabled = true
                                }
                                hidePassword(passwordInput)
                                hidePassword(confirmPasswordInput)
                            }
                        }

                        //attach click listeners to each next button
                        nextButtons.forEachIndexed { index, button ->
                            button?.setOnClickListener {
                                if (sectionIndex < section.size - 1) {
                                    sectionIndex = index + 1
                                    updateStepState()
                                }
                            }
                        }

                        disableAllNextButton()

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

                                    // Dismiss the Snackbar if it's visible and the email is valid
                                    currentSnackbar?.dismiss()

                                    // Reset the currentSnackbar reference to null
                                    currentSnackbar = null
                                } else {
                                    emailNextButton.isEnabled = false
                                    showSnackbarError(emailInput, "Please enter a valid email address")
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



                        ///

                        setupPasswordVisibilityToggle(passwordInput)
                        setupPasswordVisibilityToggle(confirmPasswordInput)

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
//                                    // Dismiss the Snackbar if it's visible and the email is valid
//                                    currentSnackbar?.dismiss()
//
//                                    // Reset the currentSnackbar reference to null
//                                    currentSnackbar = null

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
                                                // Dismiss the Snackbar if it's visible and the email is valid
                                                currentSnackbar?.dismiss()

                                                // Reset the currentSnackbar reference to null
                                                currentSnackbar = null

                                            } else {
                                                passwordNextButton.isEnabled = false
                                                showSnackbarError(confirmPasswordInput, "Password mismatch")
                                            }
                                        }

                                        //unused "TextWatcher" functions
                                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                                        override fun afterTextChanged(s: Editable?) {}
                                    })

                                } else {
                                    passwordNextButton.isEnabled = false
//
//                                    //set error message depending on validation error
//                                    when {
//                                        !s.toString().contains(Regex("\\d")) -> showSnackbarError(passwordInput, "Password must contain at least one number.")
//                                        !s.toString().contains(Regex("[!@#\$%^&*()\\-_=+\\[{\\]}|;:'\",<.>/?]")) -> showSnackbarError(passwordInput, "Password must contain at least one special character (e.g., #, ?, !, $).")
//                                        s.toString().length < 10 -> showSnackbarError(passwordInput, "Password must be at least 10 characters long.")
//                                        else -> passwordInput.error = null // Clear error if everything is fine
//                                    }
                                }
                            }

                            //unused "TextWatcher" functions
                            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                            override fun afterTextChanged(s: Editable?) {}
                        })

                        //datePicker container
                        layoutAgePicker.addView(datePicker)

                        //set up a date change listener
                        datePicker.init(
                            datePicker.year,
                            datePicker.month,
                            datePicker.dayOfMonth
                        ) { _, year, month, dayOfMonth ->
                            val selectedDate = Calendar.getInstance().apply {
                                set(year, month, dayOfMonth)
                            }

                            val today = Calendar.getInstance()
                            val minAllowedDate = Calendar.getInstance().apply {
                                add(Calendar.YEAR, -13)
                            }

                            birthdateNextButton.isEnabled = !selectedDate.after(minAllowedDate)
                        }

                        val usernameInput = findViewById<EditText>(R.id.input_username)
                        termsCheckbox.isEnabled = usernameInput.text.toString().isNotBlank()

                        usernameInput.addTextChangedListener(object : TextWatcher{
                            override fun onTextChanged(
                                s: CharSequence?,
                                start: Int,
                                before: Int,
                                count: Int) {

                                //implements passwordAuthentication function to string input
                                val usernameText = s.toString()
                                termsCheckbox.isEnabled = usernameText.isNotBlank()
                            }

                            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                            override fun afterTextChanged(s: Editable?) {}

                        })

                        //application guidelines pop-up text
                        fun showTermsDialog(onAgreed: () -> Unit) {
                            val dialogView = LayoutInflater.from(context).inflate(R.layout.terms_dialog, null)
                            val dialog = AlertDialog.Builder(context)
                                .setView(dialogView)
                                .setCancelable(false)
                                .create()

                            val termsText = dialogView.findViewById<TextView>(R.id.termsText)
                            val agreeButton = dialogView.findViewById<Button>(R.id.agreeButton)
                            val scrollView = dialogView.findViewById<ScrollView>(R.id.termsScrollView)

                            // Load from res/raw
                            val termsContent = resources.openRawResource(R.raw.terms_privacy)
                                .bufferedReader()
                                .use { it.readText() }
                            termsText.text = termsContent

                            // Enable button only when scrolled to bottom
                            scrollView.viewTreeObserver.addOnScrollChangedListener {
                                val view = scrollView.getChildAt(scrollView.childCount - 1)
                                val diff = view.bottom - (scrollView.height + scrollView.scrollY)
                                if (diff <= 0) {
                                    agreeButton.isEnabled = true
                                }
                            }

                            agreeButton.setOnClickListener {
                                dialog.dismiss()
                                onAgreed()
                            }

                            dialog.show()
                        }

                        //Checkbox listener
                        // TODO: fix active checkbox style, issue: does not show custom style
                        ///

                        termsCheckbox.setOnCheckedChangeListener { _, isChecked ->
                            if (termsCheckboxWaitAgree) return@setOnCheckedChangeListener

                            if (isChecked) {
                                // Temporarily prevent re-trigger
                                termsCheckboxWaitAgree = true
                                termsCheckbox.isChecked = false
                                termsCheckboxWaitAgree = false

                                showTermsDialog {
                                    // Only set checked = true after user agrees
                                    termsCheckboxWaitAgree = true
                                    termsCheckbox.isChecked = true
                                    termsCheckboxWaitAgree = false
                                    createAccountButton.isEnabled = true
                                }
                            }
                        }



                    }
                } catch (e: Exception) {
                    TODO("Not yet implemented")
                }
            } else {
                //shows "signup_form.xml"
                signUpOverlay?.visibility = View.VISIBLE
            }
        }



    }
}