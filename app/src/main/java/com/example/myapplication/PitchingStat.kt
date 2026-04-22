package com.example.myapplication

data class PitchingStat(
    val name: String,
    val ip: Double,
    val hits: Int,
    val runs: Int,
    val strikeouts: Int,
    val walks: Int,
    val era: Double,
    val whip: Double
)