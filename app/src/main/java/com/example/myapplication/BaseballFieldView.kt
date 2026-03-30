package com.example.myapplication

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AlertDialog

class BaseballFieldView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
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
        style = Paint.Style.FILL
    }

    private val linePaint = Paint().apply {
        color = Color.WHITE
        strokeWidth = 5f
        style = Paint.Style.STROKE
    }

    private val basePaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }

    private val circlePaint = Paint().apply {
        color = Color.GRAY
        style = Paint.Style.FILL
    }

    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 40f
        textAlign = Paint.Align.CENTER
    }

    private val scoreboardPaint = Paint().apply {
        color = Color.WHITE
        textSize = 45f
    }

    private val circleRadius = 60f

    /* ---------- GAME STATE ---------- */

    private var inning = 1
    private var outs = 0
    private var balls = 0
    private var strikes = 0
    private var homeScore = 0
    private var awayScore = 0

    private var selectedPitcher: Player? = null
    private var selectedHitter: Player? = null

    /* ---------- POSITIONS ---------- */

    private lateinit var homePos: Pair<Float, Float>
    private lateinit var pitcherPos: Pair<Float, Float>

    /* ---------- DRAW ---------- */

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()

        // Background
        canvas.drawRect(0f, 0f, width, height, fieldPaint)

        // Scoreboard
        val scoreboardText =
            "Inning: $inning  Outs: $outs  Balls: $balls  Strikes: $strikes  Home: $homeScore  Away: $awayScore"
        canvas.drawText(scoreboardText, 50f, 80f, scoreboardPaint)

        val diamondSize = width.coerceAtMost(height) * 0.5f
        val centerX = width / 2
        val centerY = height / 2 + 50f
        val half = diamondSize / 2

        // Bases
        val home = Pair(centerX, centerY + half)
        val first = Pair(centerX + half, centerY)
        val second = Pair(centerX, centerY - half)
        val third = Pair(centerX - half, centerY)

        homePos = home
        pitcherPos = Pair(centerX, centerY)

        // Lines
        canvas.drawLine(home.first, home.second, first.first, first.second, linePaint)
        canvas.drawLine(first.first, first.second, second.first, second.second, linePaint)
        canvas.drawLine(second.first, second.second, third.first, third.second, linePaint)
        canvas.drawLine(third.first, third.second, home.first, home.second, linePaint)

        // Bases
        val baseSize = diamondSize * 0.1f
        listOf(home, first, second, third).forEach { (x, y) ->
            canvas.drawRect(
                x - baseSize / 2,
                y - baseSize / 2,
                x + baseSize / 2,
                y + baseSize / 2,
                basePaint
            )
        }

        // Pitcher circle
        canvas.drawCircle(pitcherPos.first, pitcherPos.second, circleRadius, circlePaint)
        canvas.drawText(
            selectedPitcher?.last_name ?: "P",
            pitcherPos.first,
            pitcherPos.second + 15f,
            textPaint
        )

        // Hitter circle (home plate)
        canvas.drawCircle(homePos.first, homePos.second, circleRadius, circlePaint)
        canvas.drawText(
            selectedHitter?.last_name ?: "H",
            homePos.first,
            homePos.second + 15f,
            textPaint
        )
    }

    /* ---------- TOUCH ---------- */

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event ?: return false

        if (event.action == MotionEvent.ACTION_DOWN) {
            val x = event.x
            val y = event.y

            if (isInsideCircle(x, y, pitcherPos)) {
                showPlayerDropdown(isPitcher = true)
                return true
            }

            if (isInsideCircle(x, y, homePos)) {
                showPlayerDropdown(isPitcher = false)
                return true
            }
        }

        return super.onTouchEvent(event)
    }

    private fun isInsideCircle(x: Float, y: Float, center: Pair<Float, Float>): Boolean {
        val dx = x - center.first
        val dy = y - center.second
        return dx * dx + dy * dy <= circleRadius * circleRadius
    }

    /* ---------- DROPDOWN ---------- */

    private fun showPlayerDropdown(isPitcher: Boolean) {

        // Get correct team players
        val players = if (isPitcher) {
            if (isTopInning)
                Repository.getHomeTeamPlayers(homeTeamId)   // home pitches
            else
                Repository.getAwayTeamPlayers(awayTeamId)
        } else {
            if (isTopInning)
                Repository.getAwayTeamPlayers(awayTeamId)   // away hits
            else
                Repository.getHomeTeamPlayers(homeTeamId)
        }

        if (players.isEmpty()) return

        val names = players.map { "${it.first_name} ${it.last_name}" }.toTypedArray()

        AlertDialog.Builder(context)
            .setTitle(if (isPitcher) "Select Pitcher" else "Select Hitter")
            .setItems(names) { dialog, which ->
                val selected = players[which]

                if (isPitcher) {
                    selectedPitcher = selected
                } else {
                    selectedHitter = selected
                }

                invalidate() // redraw
                dialog.dismiss()
            }
            .show()
    }

    /* ---------- PUBLIC GAME STATE METHODS ---------- */

    fun updateCount(newBalls: Int, newStrikes: Int, newOuts: Int) {
        balls = newBalls
        strikes = newStrikes
        outs = newOuts
        invalidate()
    }

    fun updateScore(home: Int, away: Int) {
        homeScore = home
        awayScore = away
        invalidate()
    }

    fun nextInning() {
        inning++
        outs = 0
        balls = 0
        strikes = 0
        invalidate()
    }
    fun getPitcherId(): Int {
        return selectedPitcher?.player_id ?: -1
    }

    fun getHitterId(): Int {
        return selectedHitter?.player_id ?: -1
    }
}

