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
import com.android.volley.toolbox.Volley
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import org.json.JSONObject


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
        btnSignIn.setOnClickListener {
            Log.e("Button clicked", "btnSignIn Clicked")
            val email = editEmail.text.toString()
            val password = editPassword.text.toString()
            Log.e("Entered credentials", "Email: $email, Password: $password")
            ApiService.login(
                this,
                email,
                password,
                { success, isAdmin ->

                    if (success) {

                        Log.i("Login", "User logged in")

                        val intent = Intent(this, MainMenuActivity::class.java)
                        intent.putExtra("is_admin", isAdmin)
                        startActivity(intent)
                    }

                },
                { error ->
                    Log.e("Login error", error)
                }
            )
        }
        //Same action as sign in, except creates a new account with default admin status = 0
        btnSignUp.setOnClickListener {

            val email = editEmail.text.toString()
            val password = editPassword.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Log.e("Signup", "Email or password cannot be empty")
                return@setOnClickListener
            }

            ApiService.signup(
                this,
                email,
                password,
                { success ->

                    if (success) {

                        Log.i("Signup", "User registered")

                        val intent = Intent(this, MainMenuActivity::class.java)
                        intent.putExtra("is_admin", 0)
                        startActivity(intent)
                    }

                },
                { error ->
                    Log.e("Signup error", error)
                }
            )
        }

    }
}