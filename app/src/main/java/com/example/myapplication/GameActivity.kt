package com.example.myapplication

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class GameActivity : AppCompatActivity() {

    private lateinit var field: BaseballFieldView

    // Game state
    private var balls = 0
    private var strikes = 0
    private var outs = 0
    private var inning = 1
    private var isTop = true

    private var homeScore = 0
    private var awayScore = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_game)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // ✅ Initialize field view
        field = findViewById(R.id.baseballFieldView)

        val homeTeam = intent.getStringExtra("home_team_id") ?: ""
        val awayTeam = intent.getStringExtra("away_team_id") ?: ""

        field.setTeams(homeTeam, awayTeam)

        setupButtons()
        updateUI()
    }

    /* ---------------- BUTTON SETUP ---------------- */

    private fun setupButtons() {

        findViewById<Button>(R.id.btnBall).setOnClickListener {
            Log.e("Play", "Ball recorded")
            balls++
            if (balls == 4) {
                Log.e("Play", "Walk recorded")
                recordPlay(walk = 1)
                resetCount()
            }
            updateUI()
        }

        findViewById<Button>(R.id.btnStrike).setOnClickListener {
            Log.e("Play", "Strike recorded")
            strikes++
            if (strikes == 3) {
                Log.e("Play", "Strikeout recorded")
                recordPlay(strikeout = 1)
                strikeOut()
            }
            updateUI()
        }

        findViewById<Button>(R.id.btnFoul).setOnClickListener {
            Log.e("Play", "Foul recorded")
            if (strikes < 2) strikes++
            recordPlay()
            updateUI()
        }

        findViewById<Button>(R.id.btnOut).setOnClickListener {
            Log.e("Play", "Out recorded")
            recordPlay(battedOut = 1)
            registerOut(1)
        }

        findViewById<Button>(R.id.btnDoublePlay).setOnClickListener {
            Log.e("Play", "Double play recorded")
            recordPlay(doublePlay = 1)
            registerOut(2)
        }

        findViewById<Button>(R.id.btnTriplePlay).setOnClickListener {
            Log.e("Play", "Triple play recorded")
            recordPlay(triplePlay = 1)
            registerOut(3)
        }

        findViewById<Button>(R.id.btnSingle).setOnClickListener {
            Log.e("Play", "Single recorded")
            recordPlay(single = 1)
            registerHit(1)
        }

        findViewById<Button>(R.id.btnDouble).setOnClickListener {
            Log.e("Play", "Double recorded")
            recordPlay(doubleHit = 1)
            registerHit(2)
        }

        findViewById<Button>(R.id.btnTriple).setOnClickListener {
            Log.e("Play", "Triple recorded")
            recordPlay(triple = 1)
            registerHit(3)
        }

        findViewById<Button>(R.id.btnHomerun).setOnClickListener {
            Log.e("Play", "Homerun recorded")
            recordPlay(homerun = 1)
            registerHit(4)
        }

        findViewById<Button>(R.id.btnFC).setOnClickListener {
            Log.e("Play", "Fielder's choice recorded")
            recordPlay(fieldersChoice = 1)
            registerOut(1)
        }
    }

    /* ---------------- GAME LOGIC ---------------- */

    private fun registerOut(numOuts: Int) {
        outs += numOuts
        resetCount()

        if (outs >= 3) {
            Log.e("Play", "Switching sides")
            switchSides()
        }

        updateUI()
    }

    private fun registerHit(bases: Int) {
        resetCount()

        // Simplified scoring
        if (bases == 4) {
            if (isTop) awayScore++ else homeScore++
        }

        updateUI()
    }

    private fun strikeOut() {
        resetCount()
        registerOut(1)
    }

    private fun switchSides() {
        outs = 0
        resetCount()

        isTop = !isTop

        if (isTop) inning++
    }

    private fun resetCount() {
        balls = 0
        strikes = 0
    }

    private fun updateUI() {
        field.updateCount(balls, strikes, outs)
        field.updateScore(homeScore, awayScore)
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
            inning = inning,
            half = if (isTop) "TOP" else "BOTTOM",

            home_score = homeScore,
            away_score = awayScore,

            pitcher_id = field.getPitcherId(),
            batter_id = field.getHitterId(),

            first_base_runner_id = null,
            second_base_runner_id = null,
            third_base_runner_id = null,

            balls = balls,
            strikes = strikes,
            outs = outs,

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

        ApiService.insertPlay(
            this,
            play,
            onSuccess = { Log.i("InsertPlay", "Play recorded") },
            onError = { error -> Log.e("InsertPlay", error) }
        )
    }
}