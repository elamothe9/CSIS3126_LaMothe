package com.example.myapplication

import android.app.Activity
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AlertDialog

class BaseballFieldView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    init {
        isClickable = true
        isFocusable = true
    }

    var homeTeamId: String = ""
    var awayTeamId: String = ""

    fun setTeams(home: String, away: String) {
        homeTeamId = home
        awayTeamId = away
    }

    private var isTopInning = true

    /* ---------- PAINTS ---------- */

    private val fieldPaint = Paint().apply {
        color = Color.parseColor("#4CAF50")
    }

    private val linePaint = Paint().apply {
        color = Color.WHITE
        strokeWidth = 5f
        style = Paint.Style.STROKE
    }

    private val basePaint = Paint().apply {
        color = Color.WHITE
    }

    private val circlePaint = Paint().apply {
        color = Color.GRAY
    }

    private val runnerPaint = Paint().apply {
        color = Color.YELLOW
    }

    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 36f
        textAlign = Paint.Align.CENTER
    }

    private val scoreboardPaint = Paint().apply {
        color = Color.WHITE
        textSize = 40f
    }

    private val pitcherRadius = 50f
    private val hitterRadius = 35f

    /* ---------- GAME STATE ---------- */

    private var inning = 1
    private var outs = 0
    private var balls = 0
    private var strikes = 0
    private var homeScore = 0
    private var awayScore = 0

    private var firstBase: Int? = null
    private var secondBase: Int? = null
    private var thirdBase: Int? = null

    private var selectedPitcher: Player? = null
    private var selectedHitter: Player? = null

    var onRunnerAction: ((base: Int, action: Int) -> Unit)? = null
    /* ---------- POSITIONS ---------- */

    private lateinit var homePos: Pair<Float, Float>
    private lateinit var pitcherPos: Pair<Float, Float>
    private lateinit var firstPos: Pair<Float, Float>
    private lateinit var secondPos: Pair<Float, Float>
    private lateinit var thirdPos: Pair<Float, Float>

    /* ---------- DRAW ---------- */

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()

        canvas.drawRect(0f, 0f, width, height, fieldPaint)

        val inningText = if (isTopInning) "TOP $inning" else "BOT $inning"
        canvas.drawText("$inningText Outs:$outs Count:$balls-$strikes", 50f, 60f, scoreboardPaint)
        canvas.drawText("HOME $homeScore AWAY $awayScore", 50f, 110f, scoreboardPaint)

        val diamondSize = width.coerceAtMost(height) * 0.5f
        val centerX = width / 2
        val centerY = height / 2 + 50f
        val half = diamondSize / 2

        val home = Pair(centerX, centerY + half)
        val first = Pair(centerX + half, centerY)
        val second = Pair(centerX, centerY - half)
        val third = Pair(centerX - half, centerY)

        homePos = home
        pitcherPos = Pair(centerX, centerY)

        firstPos = first
        secondPos = second
        thirdPos = third

        // Lines
        canvas.drawLine(home.first, home.second, first.first, first.second, linePaint)
        canvas.drawLine(first.first, first.second, second.first, second.second, linePaint)
        canvas.drawLine(second.first, second.second, third.first, third.second, linePaint)
        canvas.drawLine(third.first, third.second, home.first, home.second, linePaint)

        // Bases
        val baseSize = diamondSize * 0.1f
        listOf(home, first, second, third).forEach { (x, y) ->
            canvas.drawRect(x - baseSize / 2, y - baseSize / 2, x + baseSize / 2, y + baseSize / 2, basePaint)
        }

        // Runners
        val r = 20f
        if (firstBase != null) canvas.drawCircle(firstPos.first, firstPos.second, r, runnerPaint)
        if (secondBase != null) canvas.drawCircle(secondPos.first, secondPos.second, r, runnerPaint)
        if (thirdBase != null) canvas.drawCircle(thirdPos.first, thirdPos.second, r, runnerPaint)

        // Pitcher
        canvas.drawCircle(pitcherPos.first, pitcherPos.second, pitcherRadius, circlePaint)
        canvas.drawText(selectedPitcher?.last_name ?: "P", pitcherPos.first, pitcherPos.second + 15f, textPaint)

        // Hitter
        canvas.drawCircle(homePos.first, homePos.second, hitterRadius, circlePaint)
        canvas.drawText(selectedHitter?.last_name ?: "H", homePos.first, homePos.second + 10f, textPaint)
    }

    /* ---------- TOUCH ---------- */

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event ?: return false

        if (event.action == MotionEvent.ACTION_DOWN) {
            val x = event.x
            val y = event.y

            // Pitcher
            if (::pitcherPos.isInitialized &&
                isInsideCircle(x, y, pitcherPos, pitcherRadius * 1.5f)
            ) {
                showPlayerDropdown(true)
                return true
            }

            // Hitter
            if (::homePos.isInitialized &&
                isInsideCircle(x, y, homePos, hitterRadius * 2f)
            ) {
                showPlayerDropdown(false)
                return true
            }

            // Runner taps
            if (firstBase != null && isInsideCircle(x, y, firstPos, 50f)) {
                showRunnerMenu(1)
                return true
            }

            if (secondBase != null && isInsideCircle(x, y, secondPos, 50f)) {
                showRunnerMenu(2)
                return true
            }

            if (thirdBase != null && isInsideCircle(x, y, thirdPos, 50f)) {
                showRunnerMenu(3)
                return true
            }
        }

        return true
    }

    private fun isInsideCircle(x: Float, y: Float, center: Pair<Float, Float>, radius: Float): Boolean {
        val dx = x - center.first
        val dy = y - center.second
        return dx * dx + dy * dy <= radius * radius
    }

    /* ---------- RUNNER MENU ---------- */

    private fun showRunnerMenu(base: Int) {
        val activity = context as? Activity ?: return

        val options = arrayOf(
            "Advance 1 base",
            "Advance 2 bases",
            "Advance 3 bases",
            "Go back 1 base",
            "Runner out"
        )

        AlertDialog.Builder(activity)
            .setTitle("Runner Action")
            .setItems(options) { _, which ->
                onRunnerAction?.invoke(base, which)
            }
            .show()
    }

    private fun moveRunner(fromBase: Int, delta: Int) {
        val runner = when (fromBase) {
            1 -> firstBase
            2 -> secondBase
            3 -> thirdBase
            else -> null
        } ?: return

        // Snapshot current state
        var f = firstBase
        var s = secondBase
        var t = thirdBase

        // Remove runner from original base
        when (fromBase) {
            1 -> f = null
            2 -> s = null
            3 -> t = null
        }

        val newBase = fromBase + delta

        when {
            newBase <= 0 -> {
                // runner removed (back past home)
            }

            newBase == 1 -> {
                if (f == null) f = runner else return
            }

            newBase == 2 -> {
                if (s == null) s = runner else return
            }

            newBase == 3 -> {
                if (t == null) t = runner else return
            }

            newBase >= 4 -> {
                // runner scores
                //if (isTopInning) awayScore++ else homeScore++
            }
        }

        // ✅ Apply clean state
        firstBase = f
        secondBase = s
        thirdBase = t
    }

    private fun removeRunner(base: Int) {
        when (base) {
            1 -> firstBase = null
            2 -> secondBase = null
            3 -> thirdBase = null
        }
    }

    /* ---------- PLAYER DROPDOWN ---------- */

    private fun showPlayerDropdown(isPitcher: Boolean) {
        val activity = context as? Activity ?: return

        val players = if (isPitcher) {
            if (isTopInning)
                Repository.getHomeTeamPlayers(homeTeamId)
            else
                Repository.getAwayTeamPlayers(awayTeamId)
        } else {
            if (isTopInning)
                Repository.getAwayTeamPlayers(awayTeamId)
            else
                Repository.getHomeTeamPlayers(homeTeamId)
        }

        if (players.isEmpty()) return

        val names = players.map { "${it.first_name} ${it.last_name}" }.toTypedArray()

        AlertDialog.Builder(activity)
            .setTitle(if (isPitcher) "Select Pitcher" else "Select Hitter")
            .setItems(names) { dialog, which ->
                val selected = players[which]
                if (isPitcher) selectedPitcher = selected
                else selectedHitter = selected
                invalidate()
                dialog.dismiss()
            }
            .show()
    }

    /* ---------- PUBLIC ---------- */

    fun updateCount(b: Int, s: Int, o: Int) {
        balls = b
        strikes = s
        outs = o
        invalidate()
    }

    fun updateScore(h: Int, a: Int) {
        homeScore = h
        awayScore = a
        invalidate()
    }

    fun updateInning(i: Int, top: Boolean) {
        inning = i
        isTopInning = top
        invalidate()
    }

    fun setBaseRunners(f: Int?, s: Int?, t: Int?) {
        firstBase = f
        secondBase = s
        thirdBase = t
        invalidate()
    }

    fun getPitcherId(): Int = selectedPitcher?.player_id ?: -1
    fun getHitterId(): Int = selectedHitter?.player_id ?: -1

    fun clearSelections() {
        selectedPitcher = null
        selectedHitter = null
        invalidate()
    }
}