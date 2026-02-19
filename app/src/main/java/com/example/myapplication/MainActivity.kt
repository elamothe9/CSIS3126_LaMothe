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
        this.btnSignIn.setOnClickListener {
            Log.i("ButtonClicked", "btnSignInClicked")
            val email = editEmail.text.toString()
            val password = editPassword.text.toString()
            val params = JSONObject()
            params.put("email", email)
            params.put("password", password)
            val queue = Volley.newRequestQueue(this)
            //Handle sign in logic here, JSON Request once API is set up
            val signin = JsonObjectRequest(
                Request.Method.POST, "https://elamothe.jwuclasses.com/login ",
                params,
                { response ->
                    Log.e("Signin data", response.toString())

                    val success = response.getBoolean("success")

                    if (success) {
                        Log.i("Signin", "User logged in")

                        val signInIntent = Intent(this, MainMenuActivity::class.java)
                        signInIntent.putExtra("is_admin", response.getInt("is_admin"))
                        startActivity(signInIntent)
                    }
                },
                { error ->
                    error.printStackTrace()
                }
            )
            signin.setShouldCache(false)
            queue.add(signin)


        }
        //Same action as sign in, except creates a new account with default admin status = 0
        this.btnSignUp.setOnClickListener {
            Log.i("ButtonClicked", "btnSignUpClicked")
            val email = editEmail.text.toString()
            val password = editPassword.text.toString()
            //Make sure email and password are not empty
            if(email.isEmpty() || password.isEmpty()){
                Log.e("Signup", "Email or password cannot be empty")
                //Exit onClickListner
                return@setOnClickListener
            }
            val params = JSONObject()
            params.put("email", email)
            params.put("password", password)
            val queue = Volley.newRequestQueue(this)

            //Handle signup logic here, JSON Request once API is set up
            val signup = JsonObjectRequest(
                Request.Method.POST, "https://elamothe.jwuclasses.com/signup",
                params,
                { response ->
                    Log.e("Signup data", response.toString())

                    val success = response.getBoolean("success")

                    if (success) {
                        Log.i("Signup", "User registered")

                        val signInIntent = Intent(this, MainMenuActivity::class.java)
                        signInIntent.putExtra("is_admin", 0)
                        startActivity(signInIntent)
                    }
                },
                { error ->
                    error.printStackTrace()
                }
            )

            signup.setShouldCache(false)
            queue.add(signup)
        }

    }
}