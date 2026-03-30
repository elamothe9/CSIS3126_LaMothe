package com.example.myapplication

data class Game(
    val game_id: Int,
    val home_team_id: String,
    val away_team_id: String,
    val final_home_score: Int,
    val final_away_score: Int
)
