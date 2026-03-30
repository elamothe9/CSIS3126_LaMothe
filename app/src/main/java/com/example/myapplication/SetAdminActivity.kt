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
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import com.android.volley.Request


class SetAdminActivity  : AppCompatActivity() {
    private lateinit var btnBack: Button
    private lateinit var btnConfirmEmail: Button
    private lateinit var editEmail: EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_set_admin)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        this.btnConfirmEmail = findViewById(R.id.btnConfirmEmail)
        this.btnBack = findViewById(R.id.btnBack)
        this.btnBack.setOnClickListener { finish() }
        this.editEmail = findViewById(R.id.editEmail)
        val queue = Volley.newRequestQueue(this)

        this.btnConfirmEmail.setOnClickListener {
            val email = editEmail.text.toString()
            Log.i("SetAdmin", "Email entered: $email")
            val params = JSONObject().apply {
                put("email", email)
            }

            val setAdmin = JsonObjectRequest(
                Request.Method.POST,
                "https://elamothe.jwuclasses.com/createAdmin",
                params,
                { response ->

                    Log.e("CreateAdmin", response.toString())

                    val success = response.getBoolean("success")

                    if (success) {
                        Log.i("CreateAdmin", "Admin created successfully")
                    } else {
                        Log.e("CreateAdmin", "Failed to create admin")
                    }
                },
                { error ->
                    Log.e("CreateAdmin Error", error.toString())

                    error.networkResponse?.data?.let {
                        Log.e("CreateAdmin Body", String(it))
                    }
                }
            )

            setAdmin.setShouldCache(false)
            queue.add(setAdmin)
        }
    }
}