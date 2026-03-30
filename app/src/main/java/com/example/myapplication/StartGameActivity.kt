package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.Toast

class StartGameActivity : AppCompatActivity() {
    private lateinit var btnStartGame: Button
    private lateinit var btnBack: Button
    private lateinit var homeTeamGroup: RadioGroup
    private lateinit var awayTeamGroup: RadioGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_start_game)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        homeTeamGroup = findViewById(R.id.radioGroupHome)
        awayTeamGroup = findViewById(R.id.radioGroupAway)

        btnStartGame = findViewById(R.id.btnStartGame)
        btnBack = findViewById(R.id.btnBack)
        btnBack.setOnClickListener { finish() }
        btnStartGame.setOnClickListener {

            val homeId = homeTeamGroup.checkedRadioButtonId
            val awayId = awayTeamGroup.checkedRadioButtonId

            // ✅ Check FIRST before using IDs
            if (homeId == -1 || awayId == -1) {
                Toast.makeText(this, "Select both a home and away team", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val homeButton = findViewById<RadioButton>(homeId)
            val awayButton = findViewById<RadioButton>(awayId)

            val homeTeam = homeButton.tag.toString()
            val awayTeam = awayButton.tag.toString()

            // ✅ Compare actual teams
            if (homeTeam == awayTeam) {
                Toast.makeText(this, "Home and away teams cannot be the same", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Log.e("Home team", homeTeam)
            Log.e("Away team", awayTeam)


            ApiService.createGame(this, homeTeam, awayTeam, { gameId ->

                if (gameId != -1) {
                    var homeLoaded = false
                    var awayLoaded = false

                    val tryStartGame = {
                        if (homeLoaded && awayLoaded) {
                            val startGameIntent = Intent(this, GameActivity::class.java).apply {
                                putExtra("game_id", gameId)
                                putExtra("home_team", homeTeam)
                                putExtra("away_team", awayTeam)
                            }
                            startActivity(startGameIntent)
                        }
                    }

                    ApiService.getPlayersByTeam(this, homeTeam, "home",
                        onSuccess = {
                            homeLoaded = true
                            tryStartGame()
                        },
                        onError = { error -> Log.e("PlayerFetch", error) }
                    )

                    ApiService.getPlayersByTeam(this, awayTeam, "away",
                        onSuccess = {
                            awayLoaded = true
                            tryStartGame()
                        },
                        onError = { error -> Log.e("PlayerFetch", error) }
                    )
                } else {
                    Toast.makeText(this, "Failed to start game", Toast.LENGTH_SHORT).show()
                }

            }, { error ->
                Log.e("StartGame Error", error)
                Toast.makeText(this, "Failed to start game", Toast.LENGTH_SHORT).show()
            })

        }
    }

}