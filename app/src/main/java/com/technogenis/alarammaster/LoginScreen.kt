package com.technogenis.alarammaster

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class LoginScreen : AppCompatActivity() {

    private lateinit var etUserName: EditText
    private lateinit var etUserPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnExit: Button
    private lateinit var btnSignUp: Button
    private lateinit var mAuth: FirebaseAuth


    override fun onStart() {
        super.onStart()
        val user: FirebaseUser? = mAuth.currentUser
        if (user!= null) {
            // User is signed in, go to main activity
            val intent = Intent(this, DashboardScreen::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_screen)

        mAuth = FirebaseAuth.getInstance()

        // Initialize Views
        etUserName = findViewById(R.id.etUserName)
        etUserPassword = findViewById(R.id.etUserPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnExit = findViewById(R.id.btnExit)
        btnSignUp = findViewById(R.id.btnSignUp)

        btnLogin.setOnClickListener {
            loginUser()
        }

        btnExit.setOnClickListener {
            finish() // Close the app
        }

        btnSignUp.setOnClickListener {
            // move to next screen
            val intent = Intent(this, RegisterScreen::class.java)
            startActivity(intent)
        }

    }

    private fun loginUser() {
        val email = etUserName.text.toString().trim()
        val password = etUserPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            return
        }

        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, navigate to the next activity
                    val user: FirebaseUser? = mAuth.currentUser
                    Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                    // Redirect to your main activity
                    val intent = Intent(this, DashboardScreen::class.java) // Change to your main activity
                    startActivity(intent)
                    finish() // Close this activity
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}