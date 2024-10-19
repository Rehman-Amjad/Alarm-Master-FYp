package com.technogenis.alarammaster

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterScreen : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var edName: EditText
    private lateinit var edEmail: EditText
    private lateinit var edPassword: EditText
    private lateinit var edConfirmPassword: EditText
    private lateinit var btnSignUp: Button
    private lateinit var btnLogin: Button
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_screen) // Your XML layout file

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Initialize UI components
        edName = findViewById(R.id.edName)
        edEmail = findViewById(R.id.etUserName)
        edPassword = findViewById(R.id.etUserPassword)
        edConfirmPassword = findViewById(R.id.edConfirmPassword)
        btnSignUp = findViewById(R.id.btnSignUp)
        btnLogin = findViewById(R.id.btnLogin)
        progressBar = findViewById(R.id.progressBar)

        // Set click listeners
        btnSignUp.setOnClickListener { registerUser() }
        btnLogin.setOnClickListener { finish() } // Just finish the activity for now
    }

    private fun registerUser() {
        val name = edName.text.toString().trim()
        val email = edEmail.text.toString().trim()
        val password = edPassword.text.toString().trim()
        val confirmPassword = edConfirmPassword.text.toString().trim()

        // Validate input
        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showToast("Please fill all fields")
            return
        }

        if (password != confirmPassword) {
            showToast("Passwords do not match")
            return
        }

        // Show the progress bar
        progressBar.visibility = View.VISIBLE
        // Create user with email and password
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // User registered successfully, now store user data in Firestore
                    val userId = auth.currentUser?.uid
                    val user = hashMapOf(
                        "name" to name,
                        "email" to email
                    )

                    firestore.collection("users").document(userId!!)
                        .set(user)
                        .addOnSuccessListener {
                            showToast("Registration successful")
                            // move to next screen
                            val intent = Intent(this, DashboardScreen::class.java) // Change to your main activity
                            startActivity(intent)
                            finish()

                        }
                        .addOnFailureListener { e ->
                            showToast("Error saving user data: ${e.message}")
                        }
                } else {
                    // If registration fails
                    showToast("Registration failed: ${task.exception?.message}")
                }
            }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
