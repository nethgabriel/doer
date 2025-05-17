package com.example.doer

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Rect
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import android.util.Log
import android.view.MotionEvent
import android.widget.CheckBox
import java.util.Calendar
import android.view.LayoutInflater
import android.view.inputmethod.InputMethodManager
import android.widget.ScrollView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.text.contains
import kotlin.text.toLowerCase
import java.util.Locale


class LoginActivity : AppCompatActivity() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.windowToken, 0)

                    // Clear focus from the EditText to prevent it from regaining focus immediately
                    v.clearFocus()
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

    // Global flag to track Snackbar visibility
    var isSnackbarVisible = false
    // Global variable to store the reference to the current Snackbar
    var currentSnackbar: Snackbar? = null

    private fun showSnackbarError( anchorView: View, message: String) {
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

    private fun showSnackbarSuccess(anchorView: View, message: String) {
        // If a Snackbar is already visible, do not show it again
        if (isSnackbarVisible) return

        //val rootContainer = (anchorView.context as Activity).window.decorView.findViewById<ViewGroup>(android.R.id.content)

        // Show Snackbar and set the flag to true
        currentSnackbar = Snackbar.make(anchorView, message, Snackbar.LENGTH_LONG).setAnchorView(anchorView).apply {

            val snackbarLayout = this.view
            val textView = snackbarLayout.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)

            val drawable = ContextCompat.getDrawable(view.context, R.drawable.success_icon)
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

    data class User(
        val email: String,
        val username: String,
        val age: String,
        val birthDate: com.google.firebase.Timestamp? = null
    )

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

                        fun isUsernameAvailable(username: String, onComplete: (isAvailable: Boolean, error: Exception?) -> Unit) {
                        val lowercaseUsername = username.toLowerCase(Locale.ROOT) // Normalize

                        db.collection("usernames").document(lowercaseUsername).get()
                            .addOnSuccessListener { documentSnapshot ->
                                // If the document does NOT exist, the username is available
                                onComplete(!documentSnapshot.exists(), null)
                            }
                            .addOnFailureListener { e ->
                                // Handle the error (e.g., network issue)
                                Log.e("UsernameCheck", "Error checking username availability", e)
                                onComplete(false, e) // Assume not available or show an error
                            }
                        }

                        fun getBirthDateFromDatePicker(datePicker: DatePicker): com.google.firebase.Timestamp {
                            val birthDateCalendar = Calendar.getInstance().apply {
                                set(datePicker.year, datePicker.month, datePicker.dayOfMonth)
                            }
                            return com.google.firebase.Timestamp(birthDateCalendar.time)
                        }

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

                                //cycle through passwordAuthentication function to check for conditions, if true, radioButton = active
                                checks.forEachIndexed { index, passed ->
                                    radioButtons[index].isChecked = passed
                                }

                                //this enables confirm password input field
                                if(radioButtonActive()) {
                                    confirmPasswordInput.isEnabled = true
                                } else {
                                    confirmPasswordInput.isEnabled = false
                                    confirmPasswordInput.text.clear()
                                    hidePassword(confirmPasswordInput)
                                    passwordNextButton.isEnabled = false
//
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

                        usernameInput.addTextChangedListener(object : TextWatcher {
                            private var searchForUsernameJob: Job? = null

                            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                                val usernameText = s.toString().trim()
                                termsCheckbox.isEnabled = usernameText.isNotBlank()
                                createAccountButton.isEnabled = false // Disable button while checking

                                searchForUsernameJob?.cancel() // Cancel previous check

                                if (usernameText.isBlank()) {
                                    // Handle empty username case
                                    usernameInput.error = null
                                    createAccountButton.isEnabled = false
                                    return
                                }

                                // Debounce the availability check
                                searchForUsernameJob = lifecycleScope.launch {
                                    delay(100) // Adjust debounce delay as needed (e.g., 500ms)
                                    isUsernameAvailable(usernameText) { isAvailable, error ->
                                        if (error != null) {
                                            // Handle error checking availability (e.g., show a temporary message)
                                            usernameInput.error = "Error checking username."
                                            createAccountButton.isEnabled = false
                                        } else {
                                            if (isAvailable) {
                                                usernameInput.error = null // Clear error
                                            } else {
                                                usernameInput.error = "Username is already taken."
                                                createAccountButton.isEnabled = false
                                            }
                                        }
                                    }
                                }
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
                            val exitButton = dialogView.findViewById<ImageButton>(R.id.btn_exit)

                            exitButton.setOnClickListener{
                                dialog.dismiss()
                                termsCheckbox.isChecked = false
                                termsCheckboxWaitAgree = false
                            }
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
                        termsCheckbox.setOnCheckedChangeListener { _, isChecked ->
                            if (termsCheckboxWaitAgree) {
                                if (!isChecked) {
                                    termsCheckboxWaitAgree = false
                                }
                                return@setOnCheckedChangeListener
                            }

                            if (isChecked) {
                                // Temporarily prevent re-trigger
                                termsCheckboxWaitAgree = true
                                termsCheckbox.isChecked = false

                                showTermsDialog {
                                    // Only set checked = true after user agrees
                                    termsCheckboxWaitAgree = true
                                    termsCheckbox.isChecked = true
                                    termsCheckboxWaitAgree = false
                                    createAccountButton.isEnabled = true
                                }
                            } else {
                                createAccountButton.isEnabled = false
                            }
                        }

                        fun calculateAgeFromDatePicker(datePicker: DatePicker): Int {
                            val birthDate = Calendar.getInstance().apply {
                                set(datePicker.year, datePicker.month, datePicker.dayOfMonth)
                            }

                            val today = Calendar.getInstance()
                            var age = today.get(Calendar.YEAR) - birthDate.get(Calendar.YEAR)
                            if (today.get(Calendar.DAY_OF_YEAR) < birthDate.get(Calendar.DAY_OF_YEAR)) {
                                age--
                            }
                            return age
                        }

                        // Your registerUserWithUsername function (needs to accept the User data class)
                        // Define this function outside the createAccountButton.setOnClickListener
                        fun registerUserWithUsername(uid: String, username: String, email: String, userData: User, onComplete: (isSuccess: Boolean, error: Exception?) -> Unit) {
                            val lowercaseUsername = username.toLowerCase(Locale.ROOT) // Normalize

                            val batch = db.batch()

                            // Add the user document to the batch
                            val userRef = db.collection("users").document(uid)
                            // Use the data class object directly here!
                            batch.set(userRef, userData)


                            // Add the username document to the batch
                            val usernameRef = db.collection("usernames").document(lowercaseUsername)
                            batch.set(usernameRef, hashMapOf(
                                "uid" to uid, // Store the UID for easy lookup
                                "registeredAt" to FieldValue.serverTimestamp()
                            ))

                            // Commit the batch
                            batch.commit()
                                .addOnSuccessListener {
                                    onComplete(true, null) // Both writes succeeded
                                }
                                .addOnFailureListener { e ->
                                    // If any write in the batch fails, the entire batch fails
                                    onComplete(false, e)
                                }
                        }



                        createAccountButton.setOnClickListener createAccountBtnListener@ {
                            val email = emailInput.text.toString().trim()
                            val password = confirmPasswordInput.text.toString().trim()
                            val username = usernameInput.text.toString().trim()
                            val age = calculateAgeFromDatePicker(datePicker).toString().trim() // Get age here
                            val birthDateTimestamp = getBirthDateFromDatePicker(datePicker)

                            val anchorViewForSnackbar = findViewById<Button>(R.id.btn_create_account)

                            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                                showSnackbarError(anchorViewForSnackbar, "Please enter a valid email address.")
                                emailInput.requestFocus()
                                createAccountButton.isEnabled = false
                                termsCheckbox.isChecked = false
                                return@createAccountBtnListener
                            }
                            if (username.isEmpty() || username.length < 3) { // Example: Minimum 3 characters
                                showSnackbarError(anchorViewForSnackbar, "Username must be at least 3 characters.")
                                usernameInput.requestFocus()
                                createAccountButton.isEnabled = false
                                termsCheckbox.isChecked = false
                                return@createAccountBtnListener
                            }

                            isUsernameAvailable(username) { isAvailable, error ->
                                if (error != null) {
                                    showSnackbarError(anchorViewForSnackbar, "Error checking username availability. Please try again.")
                                } else if (!isAvailable) {
                                    // Username is already taken
                                    showSnackbarError(anchorViewForSnackbar, "Username is already taken. Please choose another.")
                                    usernameInput.requestFocus()
                                } else {
                                    // Username is available, proceed with Firebase Authentication registration
                                    auth.createUserWithEmailAndPassword(email, password)
                                        .addOnCompleteListener(this@LoginActivity) { task ->
                                            if (task.isSuccessful) {
                                                val user = auth.currentUser
                                                user?.let { firebaseUser ->
                                                    val uid = firebaseUser.uid
                                                    // **Now call registerUserWithUsername to save user data to Firestore atomically**

                                                    // Create the User data object to pass to the function
                                                    val userData = User(
                                                        email = email,
                                                        username = username,
                                                        age = age ,
                                                        birthDate = birthDateTimestamp
                                                    )

                                                    registerUserWithUsername(uid, username, email, userData) { isSuccess, dbError ->
                                                        if (isSuccess) {
                                                            // Successfully registered user in Auth and saved data in Firestore
                                                            showSnackbarSuccess(
                                                                anchorViewForSnackbar,
                                                                "Account created successfully"
                                                            )
                                                            val delayMillis = 2000L
                                                            Handler(Looper.getMainLooper()).postDelayed({
                                                                closeButton.performClick()
                                                            }, delayMillis)
                                                        } else {
                                                            showSnackbarError(
                                                                anchorViewForSnackbar,
                                                                "Error saving user data: ${dbError?.message ?: "Unknown error"}. Username might be taken."
                                                            )
                                                        }
                                                    }
                                                } ?: run {
                                                    // Should ideally not happen if task.isSuccessful is true
                                                    showSnackbarError(
                                                        anchorViewForSnackbar,
                                                        "Account created, but failed to get user information."
                                                    )
                                                }
                                            } else {
                                                // Firebase Authentication failed (e.g., email already in use, weak password)
                                                showSnackbarError(
                                                    anchorViewForSnackbar,
                                                    "Authentication failed: ${task.exception?.message}"
                                                )
                                            }
                                        }
                                }
                            }
                        }

                    }
                } catch (e: Exception) {
                    Log.e("LoginActivity", "Error inflating signup form", e)

                    Toast.makeText(
                        this,
                        "An error occurred while loading the signup form.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } else {
                //shows "signup_form.xml"
                signUpOverlay?.visibility = View.VISIBLE
            }
        }

        var loginOverlay: View? = null

        val loginStub = findViewById<ViewStub>(R.id.login_stub)
        val loginButton = findViewById<Button>(R.id.btn_login)

        loginButton.setOnClickListener {
            if (loginOverlay == null) {
                try {
                    loginOverlay = loginStub.inflate().apply {
                        val closeButton = findViewById<ImageButton>(R.id.btn_close_login)

                        val emailLoginInput = findViewById<EditText>(R.id.input_email_login)
                        val passwordLoginInput = findViewById<EditText>(R.id.input_password_login)

                        val logInButton = findViewById<Button>(R.id.btn_log_in)

                        //clear all text input fields
                        fun clearAllEditTexts(viewGroup: ViewGroup) {
                            for (i in 0 until viewGroup.childCount) {
                                when (val view = viewGroup.getChildAt(i)) {
                                    is EditText -> view.text.clear()
                                    is ViewGroup -> clearAllEditTexts(view)
                                }
                            }
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

                        setupPasswordVisibilityToggle(passwordLoginInput)




                        fun hidePassword(editText: EditText, iconHidden: Int = R.drawable.visibility_off) {
                            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                            editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, iconHidden, 0)
                        }

                        //closes "login_form.xml" without terminating functionality
                        closeButton.setOnClickListener {
                            visibility = View.GONE
                            hidePassword(passwordLoginInput)
                            clearAllEditTexts(loginOverlay as ViewGroup)

                            //dismiss the Snackbar if it's visible and the email is valid
                            currentSnackbar?.dismiss()

                            //reset the currentSnackbar reference to null
                            currentSnackbar = null
                        }

                        //Log In Button
                        logInButton.setOnClickListener logInBtn@{
                            val email = emailLoginInput.text.toString().trim()
                            val password = passwordLoginInput.text.toString().trim()

                            // Basic Validation
                            if (email.isEmpty()) {
                                showSnackbarError(
                                    emailLoginInput,
                                    "Please enter your email address."
                                )
                                emailLoginInput.requestFocus()//move focus to the email field
                                return@logInBtn //stop further processing if email is empty
                            }

                            if (password.isEmpty()) {
                                showSnackbarError(
                                    passwordLoginInput,
                                    "Please enter your password."
                                )
                                passwordLoginInput.requestFocus() // Optional: move focus to the password field
                                return@logInBtn // Stop further processing if password is empty
                            }

                            // More advanced email format validation (optional but recommended)
                            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                                showSnackbarError(
                                    emailLoginInput,
                                    "Please enter a valid email address."
                                )
                                emailLoginInput.requestFocus()
                                return@logInBtn
                            }

                            //proceed to firebase authentication
                            auth.signInWithEmailAndPassword(email, password)
                                .addOnCompleteListener(this@LoginActivity) { task ->
                                    if (task.isSuccessful) {
                                        val user = auth.currentUser

                                        user?.let { firebaseUser ->
                                            //get the user's UID
                                            val uid = firebaseUser.uid

                                            //fetch user data from Firestore
                                            db.collection("users").document(uid)
                                                .get()
                                                .addOnSuccessListener { documentSnapshot ->
                                                    if (documentSnapshot.exists()) {
                                                        //if document exists, get the username
                                                        val username = documentSnapshot.getString("username")

                                                        //show success Snackbar with username
                                                        showSnackbarSuccess(
                                                            logInButton,
                                                            "Login successful! Welcome, ${username ?: "User"}" // Use "User" as a fallback
                                                        )

                                                        // Example: Navigate to your main activity after a delay
                                                        val delayMillis = 1000L // 2 second delay
                                                        Handler(Looper.getMainLooper()).postDelayed({
                                                            val intent = Intent(this@LoginActivity, ProfileActivity::class.java)
                                                            startActivity(intent)
                                                            finish()
                                                        }, delayMillis)
                                                    } else {
                                                        // Document doesn't exist (shouldn't happen if signup worked)
                                                        Log.w("LoginActivity", "User document not found for UID: $uid")
                                                        showSnackbarError(
                                                            logInButton,
                                                            "Login successful, but couldn't retrieve username."
                                                        )
                                                        // Still navigate, but maybe log out or handle this edge case
                                                        val delayMillis = 1000L
                                                        Handler(Looper.getMainLooper()).postDelayed({
                                                            val intent = Intent(this@LoginActivity, LoginActivity::class.java)
                                                            startActivity(intent)
                                                            finish()
                                                        }, delayMillis)
                                                    }
                                                }
                                                .addOnFailureListener { e ->
                                                    // Error fetching document
                                                    Log.e("LoginActivity", "Error fetching user document", e)
                                                    showSnackbarError(
                                                        logInButton,
                                                        "Login successful, but error retrieving username: ${e.message}"
                                                    )
                                                    // Still navigate, but handle the error appropriately
                                                    val delayMillis = 1000L
                                                    Handler(Looper.getMainLooper()).postDelayed({
                                                        val intent = Intent(this@LoginActivity, LoginActivity::class.java)
                                                        startActivity(intent)
                                                        finish()
                                                    }, delayMillis)
                                                }
                                        } ?: run {
                                            // Handle the case where firebaseUser is null after successful login (unlikely)
                                            showSnackbarError(
                                                logInButton,
                                                "Login successful, but failed to get user information."
                                            )
                                            val delayMillis = 1000L
                                            Handler(Looper.getMainLooper()).postDelayed({
                                                val intent = Intent(this@LoginActivity, LoginActivity::class.java)
                                                startActivity(intent)
                                                finish()
                                            }, delayMillis)
                                        }

                                    } else {
                                        //login failed (Firebase Auth error)
                                        showSnackbarError(
                                            logInButton,
                                            "Login failed: ${task.exception?.message}"
                                        )
                                    }
                                }

                        }

                    }
                }catch (e: Exception) {
                    // TODO: something for debugging
                }

            } else {
                //shows "login_form.xml"
                loginOverlay?.visibility = View.VISIBLE
            }
        }
    }
}