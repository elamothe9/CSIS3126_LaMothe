package com.example.myapplication

import android.content.Context
import android.util.Log

object Repository {

    /* -------- USER STATE -------- */
    private var isAdmin: Int = 0

    fun setAdmin(admin: Int) {
        isAdmin = admin
    }

    fun isAdmin(): Boolean {
        return isAdmin == 1
    }


    /* -------- GAME STATE -------- */

    private var gameId: Int = -1

    fun setGameId(id: Int) {
        Log.e("Function", "setGameId called with id: $id")
        gameId = id
    }
    fun getGameId(): Int = gameId

    private val homeTeamPlayers = mutableMapOf<String, List<Player>>()
    private val awayTeamPlayers = mutableMapOf<String, List<Player>>()

    fun setHomeTeamPlayers(teamId: String, players: List<Player>) {
        homeTeamPlayers[teamId] = players
    }

    fun getHomeTeamPlayers(teamId: String): List<Player> {
        return homeTeamPlayers[teamId] ?: emptyList()
    }

    fun setAwayTeamPlayers(teamId: String, players: List<Player>) {
        awayTeamPlayers[teamId] = players
    }

    fun getAwayTeamPlayers(teamId: String): List<Player> {
        return awayTeamPlayers[teamId] ?: emptyList()
    }

    data class GameState(
        var inning: Int = 1,
        var outs: Int = 0,
        var balls: Int = 0,
        var strikes: Int = 0,
        var homeScore: Int = 0,
        var awayScore: Int = 0,
        var currentPitcherId: Int? = null
    )












}