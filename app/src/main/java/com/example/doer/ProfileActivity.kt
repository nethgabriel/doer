package com.example.doer // Ensure this matches your app's package name

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
// If you set up ViewBinding (highly recommended for easier view access and null safety)
// import com.example.doer.databinding.ProfilePageBinding // Assuming your layout file is profile_page.xml

class ProfileActivity : AppCompatActivity() {

    private val TAG = "ProfileActivity"

    // Firebase instances
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    // --- Option 1: Declare UI Elements (if using findViewById) ---
    private var btnHome: Button? = null
    private var btnGroup: Button? = null
    private var btnProfile: Button? = null
    private var btnChallenge: Button? = null

    private var currentLayoutResId: Int = 0 //

    // --- Option 2: For ViewBinding (Recommended) ---
    // private lateinit var binding: ProfilePageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate started")

        fun setPageContentView(layoutResId: Int) {
            if (currentLayoutResId == layoutResId && currentLayoutResId != 0) {
                Log.d(TAG, "Layout $layoutResId is already set. Skipping.")
                // Optional: You might still want to re-run page-specific logic if needed for refresh
                // setupPageSpecificViewsAndLogic(layoutResId, auth.currentUser)
                return
            } else {
                Log.d(TAG, "Setting layout: $layoutResId")
                currentLayoutResId = layoutResId
                setContentView(layoutResId)
            }
        }

        // *** 1. Inflate the layout (Show profile_page.xml) ***
        // Make sure your layout file is named 'profile_page.xml' in your res/layout/ folder
        if (savedInstanceState == null) {
            setPageContentView(R.layout.profile_page)
        } else {
            // Restore the last layout if activity was recreated
            val restoredLayoutId = savedInstanceState.getInt("CURRENT_LAYOUT_ID", R.layout.profile_page)
            setPageContentView(restoredLayoutId)
        }

        fun onSaveInstanceState(outState: Bundle) {
            super.onSaveInstanceState(outState)
            outState.putInt("CURRENT_LAYOUT_ID", currentLayoutResId)
        }

        fun initializeNavigationButtons() {
            Log.d(TAG, "Initializing navigation buttons for layout: ${resources.getResourceEntryName(currentLayoutResId)}")
            try {
                btnHome = findViewById(R.id.btn_home)
                btnGroup = findViewById(R.id.btn_group)
                btnProfile =
                    findViewById(R.id.btn_profile) // Use the ID from navigation_buttons.xml
                btnChallenge = findViewById(R.id.btn_challenge)

                // Check if any button is null (which means it wasn't found in the current layout)
                if (btnHome == null || btnGroup == null || btnProfile == null || btnChallenge == null) {
                    Log.e(
                        TAG,
                        "One or more navigation buttons not found in layout ${
                            resources.getResourceEntryName(currentLayoutResId)
                        }. Ensure your XML includes 'navigation_buttons.xml' or defines these button IDs."
                    )
                    Toast.makeText(this, "Navigation error.", Toast.LENGTH_SHORT).show()
                    return // Stop if essential buttons are missing
                }

                btnHome?.setOnClickListener {
                    Log.d(TAG, "Home button clicked")
                    setPageContentView(R.layout.home_page)
                }

                btnGroup?.setOnClickListener {
                    Log.d(TAG, "Group button clicked")
                    setPageContentView(R.layout.spaces_page)
                }

                btnProfile?.setOnClickListener {
                    Log.d(TAG, "Profile button clicked")
                    setPageContentView(R.layout.profile_page)

                }
            }catch (e: Exception) {
                // TODO:
            }


        // Initialize Firebase (can be done before or after setContentView)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // *** 2. Initialize Views (if you need to interact with them) ***

        // --- Using findViewById (Traditional way) ---
        // Replace R.id.xxx with the actual IDs from your profile_page.xml
        try {
//            imageViewProfile = findViewById(R.id.overlappingImage) // Example ID, use your actual ID
//            textViewUsername = findViewById(R.id.profile_username) // Example ID, use your actual ID
//            textViewEmail = findViewById(R.id.profile_email)       // Example ID, use your actual ID
//            buttonLogout = findViewById(R.id.buttonLogout)         // Example ID, use your actual ID

            Log.d(TAG, "Views initialized using findViewById.")

        } catch (e: Exception) {
            Log.e(TAG, "Error initializing views with findViewById: ", e)
            // Handle cases where a view ID might be missing, though this shouldn't happen
            // if your layout and IDs are correct.
            Toast.makeText(this, "Error loading profile view components.", Toast.LENGTH_LONG).show()
            // You might want to finish the activity or show a more specific error UI
            // finish()
            // return // Stop further execution in onCreate if views can't be found
        }


        // Check if a user is currently signed in
        val currentUser = auth.currentUser
        if (currentUser == null) {
            // No user is signed in, redirect to LoginActivity
            Log.w(TAG, "No user signed in. Redirecting to LoginActivity.")
            val intent = Intent(this, LoginActivity::class.java)
            // Clear the back stack so the user can't navigate back to ProfileActivity
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish() // Close ProfileActivity
            return // Stop further execution in onCreate
        }
//        btnHome = findViewById(R.id.btn_home)
//        btnGroup = findViewById(R.id.btn_group)
//        btnProfile = findViewById(R.id.btn_profile)
//        btnChallenge = findViewById(R.id.btn_challenge)
//
//        btnHome?.setOnClickListener {
//            setContentView(R.layout.home_page)
//        }
//
//        btnGroup?.setOnClickListener {
//            setContentView(R.layout.spaces_page)
//        }
//
//        btnProfile?.setOnClickListener {
//            setContentView(R.layout.profile_page)
//        }
//
//        btnChallenge?.setOnClickListener {
//            setContentView(R.layout.challenge_page)
//        }

    }
    }
    // Other methods for ProfileActivity can go here
}