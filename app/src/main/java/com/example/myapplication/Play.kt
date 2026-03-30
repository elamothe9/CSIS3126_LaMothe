package com.example.myapplication

data class Play(
    val game_id: Int,
    val inning: Int,
    val half: String, // "top" or "bottom"
    val home_score: Int,
    val away_score: Int,

    val pitcher_id: Int,
    val batter_id: Int,

    val first_base_runner_id: Int?,
    val second_base_runner_id: Int?,
    val third_base_runner_id: Int?,

    val balls: Int,
    val strikes: Int,
    val outs: Int,

    val single_hit: Int,
    val double_hit: Int,
    val triple_hit: Int,
    val homerun: Int,
    val strikeout: Int,
    val walk: Int,
    val fielders_choice: Int,
    val batted_out: Int,
    val double_play: Int,
    val triple_play: Int
)
