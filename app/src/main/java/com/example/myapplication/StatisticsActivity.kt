package com.example.myapplication

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class StatisticsActivity : AppCompatActivity() {

    private lateinit var btnBack: Button
    private lateinit var battingRecycler: RecyclerView
    private lateinit var pitchingRecycler: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)

        // 🔹 Bind views
        btnBack = findViewById(R.id.btnBack)
        battingRecycler = findViewById(R.id.rvBatting)
        pitchingRecycler = findViewById(R.id.rvPitching)

        // 🔹 Back button
        btnBack.setOnClickListener {
            finish()
        }

        // 🔹 RecyclerView setup
        battingRecycler.layoutManager = LinearLayoutManager(this)
        pitchingRecycler.layoutManager = LinearLayoutManager(this)

        battingRecycler.setHasFixedSize(true)
        pitchingRecycler.setHasFixedSize(true)

        // 🔹 Load data
        loadStats()
    }

    /* ---------------- LOAD STATS ---------------- */

    private fun loadStats() {

        // 🔹 Batting stats
        ApiService.getBattingStats(
            this,
            onSuccess = { battingJSONarray ->
                Log.e("RAW_BATTING_JSON", battingJSONarray.toString())
                val battingList = mutableListOf<BattingStat>()
                for (i in 0 until battingJSONarray.length()) {
                    val obj = battingJSONarray.getJSONObject(i)

                    val stat = BattingStat(
                        name = obj.getString("name"),
                        pa = obj.getInt("pa"),
                        ab = obj.getInt("ab"),
                        hits = obj.getInt("hits"),
                        runs = obj.getInt("runs"),
                        walks = obj.getInt("bb"),
                        strikeouts = obj.getInt("so")
                    )

                    battingList.add(stat)
                }
                Log.e("Stats", "Batting stats loaded: ${battingList.size}")

                runOnUiThread {
                    battingRecycler.adapter = BattingAdapter(battingList)
                }
            },
            onError = { error ->
                Log.e("Stats", "Batting error: $error")

                runOnUiThread {
                    Toast.makeText(this, "Failed to load batting stats", Toast.LENGTH_SHORT).show()
                }
            }
        )

        // 🔹 Pitching stats
        ApiService.getPitchingStats(
            this,
            onSuccess = { pitchingJSONArray ->
                Log.e("RAW_PITCHING_JSON", pitchingJSONArray.toString())
                val pitchingList = mutableListOf<PitchingStat>()

                for (i in 0 until pitchingJSONArray.length()) {
                    val obj = pitchingJSONArray.getJSONObject(i)
                    Log.e("Stats", "Processing pitching stat: ${obj.getString("name")}")
                    val stat = PitchingStat(
                        name = obj.optString("name"),
                        ip = obj.optDouble("ip", 0.0),
                        hits = obj.optInt("hits", 0),
                        runs = obj.optInt("runs", 0),
                        strikeouts = obj.optInt("strikeouts", 0),
                        walks = obj.optInt("walks", 0),
                        era = obj.optDouble("ra", 0.0),
                        whip = obj.optDouble("whip", 0.0)
                    )

                    pitchingList.add(stat)
                }

                Log.e("Stats", "Pitching stats loaded: ${pitchingList.size}")

                runOnUiThread {
                    pitchingRecycler.adapter = PitchingAdapter(pitchingList)
                }
            },
            onError = { error ->
                Log.e("Stats", "Pitching error: $error")

                runOnUiThread {
                    Toast.makeText(this, "Failed to load pitching stats", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
}