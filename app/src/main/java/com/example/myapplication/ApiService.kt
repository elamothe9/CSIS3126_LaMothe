package com.example.myapplication

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

object ApiService {
    private const val BASE_URL = "https://elamothe.jwuclasses.com/"
    /* ---------------- LOGIN ---------------- */

    fun login(
        context: Context,
        email: String,
        password: String,
        onSuccess: (Boolean, Int) -> Unit,
        onError: (String) -> Unit
    ) {
        Log.e("Function", "Login function called")
        val queue = Volley.newRequestQueue(context)
        val params = JSONObject()
        params.put("email", email)
        params.put("password", password)

        val request = JsonObjectRequest(
            Request.Method.POST,
            BASE_URL + "login",
            params,
            { response ->
                Log.e("Login success", response.toString())
                val success = response.getBoolean("success")
                val isAdmin = response.getInt("is_admin")
                onSuccess(success, isAdmin)

            },
            { error ->
                Log.e("Login error", error.toString())
                onError(error.toString())
            }
        )

        request.setShouldCache(false)
        queue.add(request)
    }


    /* ---------------- SIGNUP ---------------- */

    fun signup(
        context: Context,
        email: String,
        password: String,
        onSuccess: (Boolean) -> Unit,
        onError: (String) -> Unit
    ) {
        Log.e("Function", "Signup function called")
        val params = JSONObject()
        params.put("email", email)
        params.put("password", password)

        val request = JsonObjectRequest(
            Request.Method.POST,
            BASE_URL + "signup",
            params,
            { response ->
                val success = response.getBoolean("success")
                onSuccess(success)
            },
            { error ->
                onError(error.toString())
            }
        )

        request.setShouldCache(false)
        Volley.newRequestQueue(context).add(request)
    }


    /* ---------------- CREATE GAME ---------------- */

    fun createGame(
        context: Context,
        homeTeam: String,
        awayTeam: String,
        onSuccess: (Int) -> Unit,
        onError: (String) -> Unit
    ) {
        Log.e("Function", "createGame function called with homeTeam: $homeTeam, awayTeam: $awayTeam")
        val params = JSONObject()
        params.put("home_team_id", homeTeam)
        params.put("away_team_id", awayTeam)

        val request = JsonObjectRequest(
            Request.Method.POST,
            BASE_URL + "createGame",
            params,
            { response ->
                Log.e("createGame success", response.toString())

                val success = response.getBoolean("success")
                if (success) {
                    val gameId = response.getInt("game_id")
                    Repository.setGameId(gameId)
                    onSuccess(gameId)
                } else {
                    onSuccess(-1)
                }
            },
            { error ->

                Log.e("createGame ERROR", error.toString())

                error.networkResponse?.data?.let {
                    Log.e("createGame BODY", String(it))
                }

                onError(error.toString())
                onSuccess(-1)
            }
        )

        request.setShouldCache(false)
        Volley.newRequestQueue(context).add(request)
    }

    /* ---------------- INSERT PLAY ---------------- */

    fun insertPlay(
        context: Context,
        play: Play,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        Log.e("Function", "insertPlay function called")
        val params = JSONObject()

        params.put("game_id", play.game_id)
        params.put("inning", play.inning)
        params.put("half", play.half)

        params.put("home_score", play.home_score)
        params.put("away_score", play.away_score)

        params.put("pitcher_id", play.pitcher_id)
        params.put("batter_id", play.batter_id)

        params.put("first_base_runner_id", play.first_base_runner_id)
        params.put("second_base_runner_id", play.second_base_runner_id)
        params.put("third_base_runner_id", play.third_base_runner_id)

        params.put("balls", play.balls)
        params.put("strikes", play.strikes)
        params.put("outs", play.outs)

        params.put("single_hit", play.single_hit)
        params.put("double_hit", play.double_hit)
        params.put("triple_hit", play.triple_hit)
        params.put("homerun", play.homerun)
        params.put("strikeout", play.strikeout)
        params.put("walk", play.walk)
        params.put("fielders_choice", play.fielders_choice)
        params.put("batted_out", play.batted_out)
        params.put("double_play", play.double_play)
        params.put("triple_play", play.triple_play)

        val request = JsonObjectRequest(
            Request.Method.POST,
            BASE_URL + "insertPlay",
            params,
            {
                onSuccess()
                Log.e("insertPlay SUCCESS", "Play inserted successfully")
            },
            { error ->
                Log.e("insertPlay ERROR", error.toString())
                onError(error.toString())
            }
        )

        request.setShouldCache(false)
        Volley.newRequestQueue(context).add(request)
    }


    /* ---------------- UNDO LAST PLAY ---------------- */

    fun undoLastPlay(
        context: Context,
        gameId: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        Log.e("Function", "undoLastPlay function called for game ID: $gameId")
        val params = JSONObject()
        params.put("game_id", gameId)

        val request = JsonObjectRequest(
            Request.Method.POST,
            BASE_URL + "undoLastPlay",
            params,
            { onSuccess() },
            { error -> onError(error.toString()) }
        )

        request.setShouldCache(false)
        Volley.newRequestQueue(context).add(request)
    }


    /* ---------------- GET LATEST PLAY ---------------- */

    fun getLatestPlay(
        context: Context,
        gameId: Int,
        onSuccess: (JSONObject) -> Unit,
        onError: (String) -> Unit
    ) {

        val request = JsonObjectRequest(
            Request.Method.GET,
            BASE_URL + "getLatestPlay?game_id=$gameId",
            null,
            { response -> onSuccess(response) },
            { error -> onError(error.toString()) }
        )

        request.setShouldCache(false)
        Volley.newRequestQueue(context).add(request)
    }


    /* ---------------- GET PLAYERS ---------------- */

    fun getPlayersByTeam(
        context: Context,
        teamId: String,
        homeAway: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val url = "$BASE_URL/getPlayers/$teamId"

        val request = JsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            { response ->
                try {
                    val success = response.getBoolean("success")
                    if (success) {
                        val playersJson = response.getJSONArray("players")
                        val players = mutableListOf<Player>()

                        for (i in 0 until playersJson.length()) {
                            val p = playersJson.getJSONObject(i)
                            val player = Player(
                                player_id = p.getInt("player_id"),
                                team_id = p.getString("team_id"),
                                first_name = p.getString("first_name"),
                                last_name = p.getString("last_name")
                            )
                            players.add(player)
                        }
                        Log.e("Players", "$homeAway team: $players")

                        // Store in Repository
                        if(homeAway == "home") {
                            Repository.setHomeTeamPlayers(teamId, players)
                        } else if(homeAway == "away") {
                            Repository.setAwayTeamPlayers(teamId, players)
                        }
                        onSuccess()
                    } else {
                        onError("Failed to fetch players for team $teamId")
                    }
                } catch (e: Exception) {
                    onError(e.message ?: "Unknown error parsing players")
                }
            },
            { error ->
                onError(error.toString())
            }
        )

        request.setShouldCache(false)
        Volley.newRequestQueue(context).add(request)
    }
}