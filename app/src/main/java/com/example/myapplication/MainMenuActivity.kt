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

class MainMenuActivity : AppCompatActivity(){

    private lateinit var btnViewStats: Button
    private lateinit var btnStartGame: Button
    private lateinit var btnSetAdmin: Button
    private lateinit var btnLogout: Button

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main_menu)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        this.btnSetAdmin = findViewById(R.id.btnSetAdmin)
        this.btnViewStats = findViewById(R.id.btnViewStats)
        this.btnStartGame = findViewById(R.id.btnStartGame)
        this.btnLogout = findViewById(R.id.btnLogout)
        //On click listeners, sending user to different activities based on which one they press
        this.btnStartGame.setOnClickListener {
            val startGameIntent = Intent(this, GameActivity::class.java)
            startActivity(startGameIntent)
    }
        this.btnViewStats.setOnClickListener {
            val viewStatsIntent = Intent(this, StatisticsActivity::class.java)
            startActivity(viewStatsIntent)
        }
        this.btnSetAdmin.setOnClickListener {
            val setAdminIntent = Intent(this, SetAdminActivity::class.java)
            startActivity(setAdminIntent)
        }
        this.btnLogout.setOnClickListener { finish() }
    }
}