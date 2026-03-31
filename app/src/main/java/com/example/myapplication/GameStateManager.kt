package com.example.myapplication

class GameStateManager {

    var balls = 0
    var strikes = 0
    var outs = 0
    var inning = 1
    var isTop = true

    var homeScore = 0
    var awayScore = 0

    // Base runners (store player IDs)
    var firstBase: Int? = null
    var secondBase: Int? = null
    var thirdBase: Int? = null

    /* ---------- COUNT ---------- */

    fun addBall(): Boolean {
        balls++
        return balls == 4
    }

    fun addStrike(): Boolean {
        strikes++
        return strikes == 3
    }

    fun addFoul() {
        if (strikes < 2) strikes++
    }

    fun resetCount() {
        balls = 0
        strikes = 0
    }

    /* ---------- OUTS ---------- */

    fun addOut(num: Int) {
        outs += num
        resetCount()

        if (outs >= 3) {
            switchSides()
        }
    }

    /* ---------- HITS ---------- */

    fun hit(bases: Int, batterId: Int?): Int {
        var runsScored = 0

        when (bases) {
            1 -> { // SINGLE
                if (thirdBase != null) runsScored++
                thirdBase = secondBase
                secondBase = firstBase
                firstBase = batterId
            }

            2 -> { // DOUBLE
                if (thirdBase != null) runsScored++
                if (secondBase != null) runsScored++
                thirdBase = firstBase
                secondBase = batterId
                firstBase = null
            }

            3 -> { // TRIPLE
                if (thirdBase != null) runsScored++
                if (secondBase != null) runsScored++
                if (firstBase != null) runsScored++

                thirdBase = batterId
                secondBase = null
                firstBase = null
            }

            4 -> { // HOMERUN
                if (thirdBase != null) runsScored++
                if (secondBase != null) runsScored++
                if (firstBase != null) runsScored++

                runsScored++ // batter scores

                clearBases()
            }
        }

        addRuns(runsScored)
        resetCount()
        return runsScored
    }

    /* ---------- WALK ---------- */

    fun walk(batterId: Int?): Int {
        var runs = 0

        if (firstBase != null && secondBase != null && thirdBase != null) {
            runs++
        }

        thirdBase = secondBase
        secondBase = firstBase
        firstBase = batterId

        addRuns(runs)
        resetCount()
        return runs
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

    private fun switchSides() {
        outs = 0
        resetCount()
        clearBases()

        isTop = !isTop
        if (isTop) inning++
    }
}