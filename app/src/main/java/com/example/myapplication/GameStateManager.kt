package com.example.myapplication

import android.util.Log
import org.json.JSONObject

class GameStateManager {

    var balls = 0
    var strikes = 0
    var outs = 0
    var inning = 1
    var isTop = true

    var homeScore = 0
    var awayScore = 0

    var firstBase: Int? = null
    var secondBase: Int? = null
    var thirdBase: Int? = null
    private val scoredRunnersThisPlay = mutableListOf<Int>()
    /* ---------- LOAD FROM DATABASE ---------- */

    fun loadFromPlay(play: JSONObject) {
        Log.e("Previous state", play.toString())

        inning = play.optInt("inning", inning)
        isTop = play.optString("half", "TOP") == "TOP"

        homeScore = play.optInt("home_score", 0)
        awayScore = play.optInt("away_score", 0)

        balls = play.optInt("balls", 0)
        strikes = play.optInt("strikes", 0)
        outs = play.optInt("outs", 0)

        firstBase = if (play.isNull("first_base_runner_id")) null else play.optInt("first_base_runner_id")
        secondBase = if (play.isNull("second_base_runner_id")) null else play.optInt("second_base_runner_id")
        thirdBase = if (play.isNull("third_base_runner_id")) null else play.optInt("third_base_runner_id")
    }

    /* ---------- COUNT ---------- */

    fun addBall(): Boolean {
        balls++
        Log.e("GameStateManager", "Adding ball. Current count: $balls-$strikes")
        return balls == 4
    }

    fun addStrike(): Boolean {
        strikes++
        Log.e("GameStateManager", "Adding strike. Current count: $balls-$strikes")
        return strikes == 3
    }

    fun addFoul() {
        if (strikes < 2) strikes++
        Log.e("GameStateManager", "Adding foul. Current count: $balls-$strikes")
    }

    fun resetCount() {
        Log.e("GameStateManager", "Resetting count")
        balls = 0
        strikes = 0
    }

    /* ---------- OUTS ---------- */

    fun addOut(num: Int): Boolean {
        outs += num
        Log.e("GameStateManager", "Adding $num out(s). Current outs: $outs")
        resetCount()

        if (outs >= 3) {
            switchSides()
            return true
        }
        return false
    }

    /* ---------- RUNNERS ---------- */

    fun advanceRunner(fromBase: Int, delta: Int) {
        val runner = when (fromBase) {
            1 -> firstBase
            2 -> secondBase
            3 -> thirdBase
            else -> null
        } ?: return

        val newBase = fromBase + delta

        // remove from current base
        when (fromBase) {
            1 -> firstBase = null
            2 -> secondBase = null
            3 -> thirdBase = null
        }

        if (newBase >= 4) {
            scoreRunner(runner)
            addRuns(1)
            return
        }

        if (newBase <= 0) return

        when (newBase) {
            1 -> firstBase = runner
            2 -> secondBase = runner
            3 -> thirdBase = runner
        }
    }

    fun removeRunner(base: Int) {
        when (base) {
            1 -> firstBase = null
            2 -> secondBase = null
            3 -> thirdBase = null
        }
    }
    /* ---------- PLAY SCORING MANAGEMENT ---------- */
    fun beginPlay() {
        scoredRunnersThisPlay.clear()
    }
    fun scoreRunner(runnerId: Int?) {
        if (runnerId != null) {
            Log.e("GameStateManager", "Scoring runner with ID: $runnerId")
            scoredRunnersThisPlay.add(runnerId)
        }
    }
    fun getScoredRunners(): List<Int> {
        Log.e("GameStateManager", "Scored runners this play: $scoredRunnersThisPlay")
        return scoredRunnersThisPlay
    }
    /* ---------- HITS ---------- */

    fun hit(bases: Int, batterId: Int?): Int {
        var runs = 0

        if (thirdBase != null) {
            scoreRunner(thirdBase)
            runs++
        }
        if (bases >= 2 && secondBase != null) {
            scoreRunner(secondBase)
            runs++
        }
        if (bases >= 3 && firstBase != null) {
            scoreRunner(firstBase)
            runs++
        }
        if (bases == 4) {
            scoreRunner(batterId)
            runs++
        }

        when (bases) {
            1 -> {
                thirdBase = secondBase
                secondBase = firstBase
                firstBase = batterId
            }
            2 -> {
                thirdBase = firstBase
                secondBase = batterId
                firstBase = null
            }
            3 -> {
                thirdBase = batterId
                secondBase = null
                firstBase = null
            }
            4 -> clearBases()
        }

        addRuns(runs)
        resetCount()
        return runs
    }

    fun walk(batterId: Int?): Int {
        Log.e("GameStateManager", "Processing walk for batter ID: $batterId")
        var runs = 0

        if (firstBase != null && secondBase != null && thirdBase != null) {
            scoreRunner(thirdBase)
            runs++
        }

        thirdBase = secondBase
        secondBase = firstBase
        firstBase = batterId

        addRuns(runs)
        resetCount()
        return runs
    }
    fun fieldersChoice(batterId: Int?, outBase: Int): Boolean {
        val runner = when (outBase) {
            1 -> firstBase
            2 -> secondBase
            3 -> thirdBase
            else -> null
        } ?: return false

        removeRunner(outBase)
        addOut(1)

        // Move the batter to the base if it's empty
        when (outBase) {
            1 -> if (firstBase == null) firstBase = batterId else return false
            2 -> if (secondBase == null) secondBase = batterId else return false
            3 -> if (thirdBase == null) thirdBase = batterId else return false
        }

        resetCount()
        return true
    }
    /* ---------- HELPERS ---------- */

    private fun addRuns(runs: Int) {
        if (isTop) awayScore += runs else homeScore += runs
    }

    private fun clearBases() {
        firstBase = null
        secondBase = null
        thirdBase = null
    }

    fun switchSides(): Boolean {
        outs = 0
        resetCount()
        clearBases()
        isTop = !isTop
        if (isTop) inning++
        return true
    }
}