package com.example.myapplication

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.GridLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class GameActivity : AppCompatActivity() {
        private lateinit var pitchPanel: GridLayout
        private lateinit var btnPitch: Button
        private lateinit var field: BaseballFieldView
        private val state = GameStateManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_game)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        Log.e("GameActivity", "GameActivity started")

        // ✅ FIXED (no val)
        pitchPanel = findViewById(R.id.pitchPanel)
        btnPitch = findViewById(R.id.btnPitch)
        field = findViewById(R.id.baseballFieldView)

        btnPitch.setOnClickListener {
            val pitcherId = field.getPitcherId()
            val hitterId = field.getHitterId()

            if (pitcherId == -1 || hitterId == -1) {
                Log.e("Game", "Select pitcher and hitter first")
                Toast.makeText(this, "Must choose hitter and pitcher", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            pitchPanel.visibility = View.VISIBLE
        }

        val homeTeam = intent.getStringExtra("home_team_id") ?: ""
        val awayTeam = intent.getStringExtra("away_team_id") ?: ""

        field.setTeams(homeTeam, awayTeam)

        setupButtons()
        updateUI()
    }

        /* ---------------- BUTTONS ---------------- */

        private fun setupButtons() {

            findViewById<Button>(R.id.btnBall).setOnClickListener {
                if (state.addBall()) {
                    recordPlay(walk = 1)
                    state.walk(field.getHitterId())
                    pitchPanel.visibility = View.GONE
                }
                updateUI()
            }

            findViewById<Button>(R.id.btnStrike).setOnClickListener {
                if (state.addStrike()) {
                    recordPlay(strikeout = 1)
                    state.addOut(1)
                    pitchPanel.visibility = View.GONE
                }
                updateUI()
            }

            findViewById<Button>(R.id.btnFoul).setOnClickListener {
                state.addFoul()
                recordPlay()
                updateUI()
                pitchPanel.visibility = View.GONE
            }

            findViewById<Button>(R.id.btnOut).setOnClickListener {
                recordPlay(battedOut = 1)
                state.addOut(1)
                updateUI()
                pitchPanel.visibility = View.GONE
            }

            findViewById<Button>(R.id.btnDoublePlay).setOnClickListener {
                recordPlay(doublePlay = 1)
                state.addOut(2)
                updateUI()
                pitchPanel.visibility = View.GONE
            }

            findViewById<Button>(R.id.btnTriplePlay).setOnClickListener {
                recordPlay(triplePlay = 1)
                state.addOut(3)
                updateUI()
                pitchPanel.visibility = View.GONE
            }

            findViewById<Button>(R.id.btnSingle).setOnClickListener {
                recordPlay(single = 1)
                state.hit(1, field.getHitterId())
                updateUI()
                pitchPanel.visibility = View.GONE
            }

            findViewById<Button>(R.id.btnDouble).setOnClickListener {
                recordPlay(doubleHit = 1)
                state.hit(2, field.getHitterId())
                updateUI()
                pitchPanel.visibility = View.GONE
            }

            findViewById<Button>(R.id.btnTriple).setOnClickListener {

                val hitterId = field.getHitterId()
                val pitcherId = field.getPitcherId()

                if (hitterId == -1 || pitcherId == -1) {
                    Log.e("Game", "Missing player selection")
                    Toast.makeText(this, "Must select hitter and pitcher", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                recordPlay(triple = 1)
                state.hit(3, hitterId)

                updateUI()
                pitchPanel.visibility = View.GONE
            }

            findViewById<Button>(R.id.btnHomerun).setOnClickListener {
                recordPlay(homerun = 1)
                state.hit(4, field.getHitterId())
                updateUI()
                pitchPanel.visibility = View.GONE
            }

            findViewById<Button>(R.id.btnFC).setOnClickListener {
                recordPlay(fieldersChoice = 1)
                state.addOut(1)
                updateUI()
                pitchPanel.visibility = View.GONE
            }
        }

        /* ---------------- UI ---------------- */

        private fun updateUI() {
            field.updateCount(state.balls, state.strikes, state.outs)
            field.updateScore(state.homeScore, state.awayScore)
            field.setBaseRunners(state.firstBase, state.secondBase, state.thirdBase)
            field.updateInning(state.inning, state.isTop)
        }

        /* ---------------- DATABASE ---------------- */

        private fun recordPlay(
            single: Int = 0,
            doubleHit: Int = 0,
            triple: Int = 0,
            homerun: Int = 0,
            strikeout: Int = 0,
            walk: Int = 0,
            fieldersChoice: Int = 0,
            battedOut: Int = 0,
            doublePlay: Int = 0,
            triplePlay: Int = 0
        ) {

            val play = Play(
                game_id = Repository.getGameId(),
                inning = state.inning,
                half = if (state.isTop) "TOP" else "BOTTOM",

                home_score = state.homeScore,
                away_score = state.awayScore,

                pitcher_id = field.getPitcherId(),
                batter_id = field.getHitterId(),

                first_base_runner_id = state.firstBase,
                second_base_runner_id = state.secondBase,
                third_base_runner_id = state.thirdBase,

                balls = state.balls,
                strikes = state.strikes,
                outs = state.outs,

                single_hit = single,
                double_hit = doubleHit,
                triple_hit = triple,
                homerun = homerun,
                strikeout = strikeout,
                walk = walk,
                fielders_choice = fieldersChoice,
                batted_out = battedOut,
                double_play = doublePlay,
                triple_play = triplePlay
            )

            Log.e("InsertPlay", play.toString())

            ApiService.insertPlay(
                this,
                play,
                onSuccess = { Log.i("InsertPlay", "SUCCESS") },
                onError = { error -> Log.e("InsertPlay", error) }
            )
        }
}