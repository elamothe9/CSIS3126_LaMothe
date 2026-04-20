package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainMenuActivity : AppCompatActivity() {

    private lateinit var btnViewStats: Button
    private lateinit var btnStartGame: Button
    private lateinit var btnSetAdmin: Button
    private lateinit var btnLogout: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main_menu)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btnSetAdmin = findViewById(R.id.btnSetAdmin)
        btnViewStats = findViewById(R.id.btnViewStats)
        btnStartGame = findViewById(R.id.btnStartGame)
        btnLogout = findViewById(R.id.btnLogout)

        /* ---------- ADMIN PERMISSIONS ---------- */

        val isAdmin = Repository.isAdmin()

        if (!isAdmin) {
            btnStartGame.visibility = View.INVISIBLE
            btnSetAdmin.visibility = View.INVISIBLE
        }

        /* ---------- BUTTON LISTENERS ---------- */

        btnStartGame.setOnClickListener {
            startActivity(Intent(this, StartGameActivity::class.java))
        }

        btnViewStats.setOnClickListener {
            startActivity(Intent(this, StatisticsActivity::class.java))
        }

        btnSetAdmin.setOnClickListener {
            startActivity(Intent(this, SetAdminActivity::class.java))
        }

        btnLogout.setOnClickListener {
            finish()
        }
    }
}