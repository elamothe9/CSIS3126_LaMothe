package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.util.Log

class MainActivity : AppCompatActivity() {

    private lateinit var btnSignIn: Button
    private lateinit var btnSignUp: Button
    private lateinit var editEmail: EditText
    private lateinit var editPassword: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        this.editEmail = findViewById(R.id.editEmail)
        this.editPassword = findViewById(R.id.editPassword)
        this.btnSignIn = findViewById(R.id.btnSignIn)
        this.btnSignUp = findViewById(R.id.btnSignUp)
        //Attempt to sign in if btnSignIn is clicked
        this.btnSignIn.setOnClickListener {
            Log.i("ButtonClicked", "btnSignInClicked")
            val email = editEmail.text.toString()
            val password = editPassword.text.toString()

            //Handle sign in logic here, JSON Request once API is set up


            //Launch next activity upon successful sign in
            //If(logged==successful)
            val signInIntent = Intent(this, MainMenuActivity::class.java)
            startActivity(signInIntent)
        }

        //Same action as sign in, except creates a new account with default admin status = 0
        this.btnSignUp.setOnClickListener {
            Log.i("ButtonClicked", "btnSignUpClicked")
            //Handle signup logic here, JSON Request once API is set up
            val signUpIntent = Intent(this, MainMenuActivity::class.java)
            startActivity(signUpIntent)
        }
    }
}