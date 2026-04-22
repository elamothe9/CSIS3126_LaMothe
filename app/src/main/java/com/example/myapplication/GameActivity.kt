package com.example.myapplication

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.GridLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class GameActivity : AppCompatActivity() {

    private lateinit var pitchPanel: GridLayout
    private lateinit var btnPitch: Button
    private lateinit var btnUndo: Button
    private lateinit var btnEndGame: Button
    private lateinit var field: BaseballFieldView

    private val state = GameStateManager()

    private var gameId: Int = -1
    private var homeTeam: String = ""
    private var awayTeam: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        pitchPanel = findViewById(R.id.pitchPanel)
        btnPitch = findViewById(R.id.btnPitch)
        btnUndo = findViewById(R.id.btnUndo)
        field = findViewById(R.id.baseballFieldView)
        btnEndGame = findViewById(R.id.btnEndGame)
        gameId = intent.getIntExtra("game_id", -1)
        Log.e("GameActivity", "Received gameId: $gameId")
        homeTeam = intent.getStringExtra("home_team_id") ?: ""
        awayTeam = intent.getStringExtra("away_team_id") ?: ""

        field.setTeams(homeTeam, awayTeam)

        // (Extra safety) Reload players in case Repository is empty
        ApiService.getPlayersByTeam(this, homeTeam, "home",
            onSuccess = {
                Log.i("Players", "Home loaded in GameActivity")
            },
            onError = { Log.e("Players", it) }
        )

        ApiService.getPlayersByTeam(this, awayTeam, "away",
            onSuccess = {
                Log.i("Players", "Away loaded in GameActivity")
            },
            onError = { Log.e("Players", it) }
        )
        field.onRunnerAction = { base, action ->

            var playRecorded = false

            when (action) {
                0 -> state.advanceRunner(base, 1)
                1 -> state.advanceRunner(base, 2)
                2 -> state.advanceRunner(base, 3)
                3 -> state.advanceRunner(base, -1)

                4 -> {
                    state.removeRunner(base)
                    state.addOut(1)

                    recordPlay(battedOut = 1)
                    playRecorded = true
                }
            }

            if (!playRecorded) {
                recordPlay()
            }

            updateUI()
        }
        btnUndo.setOnClickListener {
            undoLastPlay(gameId)
        }
        btnEndGame.setOnClickListener {

            ApiService.endGame(
                this,
                gameId,
                state.homeScore,
                state.awayScore,
                onSuccess = { success ->
                    if (success) {
                        Log.e("EndGame", "Game ended successfully")
                        finish()
                    } else {
                        Log.e("EndGame", "Failed to end game")
                        Toast.makeText(this, "Failed to end game", Toast.LENGTH_SHORT).show()
                    }
                },
                onError = { error ->
                    Log.e("EndGame", "Error ending game: $error")
                    Toast.makeText(this, "Failed to end game", Toast.LENGTH_SHORT).show()
                }
            )
        }

        btnPitch.setOnClickListener {
            val pitcherId = field.getPitcherId()
            val hitterId = field.getHitterId()

            if (pitcherId == -1 || hitterId == -1) {
                Toast.makeText(this, "Must choose hitter and pitcher", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            pitchPanel.visibility = View.VISIBLE
        }

        setupButtons()
        updateUI()
    }

    /* ---------------- UNDO ---------------- */

    private fun undoLastPlay(gameId: Int) {
        ApiService.undoLastPlay(
            this,
            gameId,
            onSuccess = {
                ApiService.getLatestPlay(
                    this,
                    gameId,
                    onSuccess = { response ->

                        if (!response.optBoolean("success")) {
                            Log.e("Undo", "Backend failed: ${response.optString("error")}")
                            Toast.makeText(this, "Undo failed", Toast.LENGTH_SHORT).show()
                            return@getLatestPlay
                        }

                        val playObj = response.getJSONObject("play")

                        state.loadFromPlay(playObj)

                        runOnUiThread {
                            updateUI()
                            Toast.makeText(this, "Undo successful", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onError = {
                        error->
                        Log.e("GameActivity", "Failed to fetch latest play after undo: $error")

                        Toast.makeText(this, "Failed to fetch latest play", Toast.LENGTH_SHORT).show()
                    }
                )
            },
            onError = {
                Toast.makeText(this, "Undo failed", Toast.LENGTH_SHORT).show()
            }
        )
    }

    /* ---------------- BUTTONS ---------------- */

    private fun setupButtons() {

        findViewById<Button>(R.id.btnBall).setOnClickListener {
            state.beginPlay()
            val isWalk = state.addBall()

            if (isWalk) {
                recordPlay(
                    walk = 1
                )
                state.walk(field.getHitterId())
                field.clearHitter()
                pitchPanel.visibility = View.GONE
            } else {
                recordPlay()
            }
            Log.e("RecordPlay", "BALL: Walk = $isWalk")


            updateUI()
        }

        findViewById<Button>(R.id.btnStrike).setOnClickListener {
            state.beginPlay()
            val isStrikeout = state.addStrike()

            if (isStrikeout) {
                recordPlay(
                    strikeout = 1
                )
                state.addOut(1)
                field.clearHitter()
                pitchPanel.visibility = View.GONE
            } else{
                recordPlay()
            }
            Log.e("RecordPlay", "STRIKE: Strikeout = $isStrikeout")


            updateUI()
        }

        /* -------- FOUL -------- */
        findViewById<Button>(R.id.btnFoul).setOnClickListener {
            state.beginPlay()
            state.addFoul()
            recordPlay()
            updateUI()
        }

        /* -------- OUT (ball in play) -------- */
        findViewById<Button>(R.id.btnOut).setOnClickListener {
            state.beginPlay()
            state.addOut(1)
            recordPlay(battedOut = 1)

            field.clearHitter()
            updateUI()
            pitchPanel.visibility = View.GONE
        }

        findViewById<Button>(R.id.btnSingle).setOnClickListener {
            state.beginPlay()
            state.hit(1, field.getHitterId())
            recordPlay(single = 1)

            field.clearHitter()
            updateUI()
            pitchPanel.visibility = View.GONE
        }

        findViewById<Button>(R.id.btnDouble).setOnClickListener {
            state.beginPlay()
            state.hit(2, field.getHitterId())
            recordPlay(doubleHit = 1)

            field.clearHitter()
            updateUI()
            pitchPanel.visibility = View.GONE
        }

        findViewById<Button>(R.id.btnTriple).setOnClickListener {
            state.beginPlay()
            state.hit(3, field.getHitterId())
            recordPlay(triple = 1)

            field.clearHitter()
            updateUI()
            pitchPanel.visibility = View.GONE
        }

        findViewById<Button>(R.id.btnHomerun).setOnClickListener {
            state.beginPlay()
            state.hit(4, field.getHitterId())
            recordPlay(homerun = 1)

            field.clearHitter()
            updateUI()
            pitchPanel.visibility = View.GONE
        }

        /* -------- FIELDERS CHOICE -------- */
        findViewById<Button>(R.id.btnFC).setOnClickListener {

            val activity = this

            val options = mutableListOf<String>()
            val baseMap = mutableListOf<Int>()

            if (state.firstBase != null) {
                options.add("Runner out at 1st")
                baseMap.add(1)
            }

            if (state.secondBase != null) {
                options.add("Runner out at 2nd")
                baseMap.add(2)
            }

            if (state.thirdBase != null) {
                options.add("Runner out at 3rd")
                baseMap.add(3)
            }

            if (options.isEmpty()) {
                Toast.makeText(activity, "No runners for fielder's choice", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            androidx.appcompat.app.AlertDialog.Builder(activity)
                .setTitle("Choose runner out")
                .setItems(options.toTypedArray()) { _, which ->

                    val outBase = baseMap[which]

                    val success = state.fieldersChoice(field.getHitterId(), outBase)

                    if (!success) {
                        Toast.makeText(activity, "Invalid fielder's choice", Toast.LENGTH_SHORT).show()
                        return@setItems
                    }

                    updateUI()

                    recordPlay(fieldersChoice = 1)

                    field.clearHitter()
                    pitchPanel.visibility = View.GONE
                }
                .show()
        }

        /* -------- DOUBLE PLAY -------- */
        findViewById<Button>(R.id.btnDoublePlay).setOnClickListener {
            state.addOut(2)

            recordPlay(doublePlay = 1)

            field.clearHitter()
            updateUI()
            pitchPanel.visibility = View.GONE
        }

        /* -------- TRIPLE PLAY -------- */
        findViewById<Button>(R.id.btnTriplePlay).setOnClickListener {
            state.addOut(3)

            recordPlay(triplePlay = 1)

            field.clearHitter()
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
        battedOut: Int = 0,
        doublePlay: Int = 0,
        triplePlay: Int = 0,
        fieldersChoice: Int = 0
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

        ApiService.insertPlay(
            this,
            play,
            onSuccess = { playId ->
                state.getScoredRunners().forEach { runnerId ->
                    Log.e("Scored runner and play", "Runner: $runnerId, Play: $playId")
                    ApiService.insertPlayRun(this, playId, runnerId)
                }

            },
            onError = { }
        )
    }
}