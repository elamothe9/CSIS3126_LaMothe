package com.example.myapplication

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
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

    private val runnerPaint = Paint().apply {
        color = Color.YELLOW
        style = Paint.Style.FILL
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
    private val hitterRadius = 35f   // ✅ smaller hitter

    /* ---------- GAME STATE ---------- */

    private var inning = 1
    private var outs = 0
    private var balls = 0
    private var strikes = 0
    private var homeScore = 0
    private var awayScore = 0

    // Base runners
    private var firstBase: Int? = null
    private var secondBase: Int? = null
    private var thirdBase: Int? = null

    private var selectedPitcher: Player? = null
    private var selectedHitter: Player? = null

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

        // Background
        canvas.drawRect(0f, 0f, width, height, fieldPaint)

        /* ---------- SCOREBOARD (COMPACT) ---------- */

        val inningText = if (isTopInning) "TOP $inning" else "BOT $inning"
        val line1 = "$inningText   Outs:$outs   Count:$balls-$strikes"
        val line2 = "HOME $homeScore   AWAY $awayScore"

        canvas.drawText(line1, 50f, 60f, scoreboardPaint)
        canvas.drawText(line2, 50f, 110f, scoreboardPaint)

        /* ---------- FIELD ---------- */

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
            canvas.drawRect(
                x - baseSize / 2,
                y - baseSize / 2,
                x + baseSize / 2,
                y + baseSize / 2,
                basePaint
            )
        }

        /* ---------- RUNNERS ---------- */

        val runnerRadius = 20f

        if (firstBase != null) {
            canvas.drawCircle(firstPos.first, firstPos.second, runnerRadius, runnerPaint)
        }
        if (secondBase != null) {
            canvas.drawCircle(secondPos.first, secondPos.second, runnerRadius, runnerPaint)
        }
        if (thirdBase != null) {
            canvas.drawCircle(thirdPos.first, thirdPos.second, runnerRadius, runnerPaint)
        }

        /* ---------- PITCHER ---------- */

        canvas.drawCircle(pitcherPos.first, pitcherPos.second, pitcherRadius, circlePaint)
        canvas.drawText(
            selectedPitcher?.last_name ?: "P",
            pitcherPos.first,
            pitcherPos.second + 15f,
            textPaint
        )

        /* ---------- HITTER ---------- */

        canvas.drawCircle(homePos.first, homePos.second, hitterRadius, circlePaint)
        canvas.drawText(
            selectedHitter?.last_name ?: "H",
            homePos.first,
            homePos.second + 10f,
            textPaint
        )
    }

    /* ---------- TOUCH ---------- */

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event ?: return false

        if (event.action == MotionEvent.ACTION_DOWN) {
            val x = event.x
            val y = event.y

            Log.e("Touch", "Tapped at $x, $y")

            if (::pitcherPos.isInitialized &&
                isInsideCircle(x, y, pitcherPos, pitcherRadius * 1.5f)
            ) {
                Log.e("Touch", "Pitcher tapped")
                showPlayerDropdown(true)
                return true
            }

            if (::homePos.isInitialized &&
                isInsideCircle(x, y, homePos, hitterRadius * 2f)
            ) {
                Log.e("Touch", "Hitter tapped")
                showPlayerDropdown(false)
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

    /* ---------- DROPDOWN ---------- */

    private fun showPlayerDropdown(isPitcher: Boolean) {
        val activity = context as? android.app.Activity ?: return

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

        Log.e("Dropdown", "Players size: ${players.size}")

        if (players.isEmpty()) return

        val names = players.map { "${it.first_name} ${it.last_name}" }.toTypedArray()

        activity.runOnUiThread {
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
    }

    /* ---------- PUBLIC METHODS ---------- */

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

    fun updateInning(newInning: Int, isTop: Boolean) {
        inning = newInning
        isTopInning = isTop
        invalidate()
    }

    fun setBaseRunners(first: Int?, second: Int?, third: Int?) {
        firstBase = first
        secondBase = second
        thirdBase = third
        invalidate()
    }

    fun getPitcherId(): Int = selectedPitcher?.player_id ?: -1
    fun getHitterId(): Int = selectedHitter?.player_id ?: -1
}