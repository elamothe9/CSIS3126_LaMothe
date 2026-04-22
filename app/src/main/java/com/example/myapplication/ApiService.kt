package com.example.myapplication

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
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
                if (error.networkResponse != null) {
                    val raw = String(error.networkResponse.data)
                    Log.e("LOGIN_RAW_ERROR", raw)
                }
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
        onSuccess: (Int) -> Unit,
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
        Log.e("insertPlay PARAMS", params.toString())
        val request = JsonObjectRequest(
            Request.Method.POST,
            BASE_URL + "insertPlay",
            params,
            { response ->
                onSuccess(response.getInt("play_id"))
                Log.e("insertPlay SUCCESS", "Play inserted successfully with play_id  = ${response.getInt("play_id")}")
            },
            { error ->
                Log.e("insertPlay ERROR", error.toString())
                onError(error.toString())
            }
        )

        request.setShouldCache(false)
        Volley.newRequestQueue(context).add(request)
    }

    /* ---------------- INSERT WHO SCORED ---------------- */
    fun insertPlayRun(
        context: Context,
        playId: Int,
        runnerId: Int
    ) {
        val params = JSONObject()
        params.put("play_id", playId)
        params.put("runner_id", runnerId)
        Log.e("insertPlayRun PARAMS", params.toString())
        val request = JsonObjectRequest(
            Request.Method.POST,
            BASE_URL + "insertPlayRun",
            params,
            { response->
                Log.e("InsertPlayRun SUCCESS", response.toString())
            },
            { error ->
                Log.e("PlayRun ERROR", error.toString())
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
            {response ->
                Log.e("undoLastPlay SUCCESS", response.toString())
                onSuccess()
            },
            { error ->
                onError(error.toString())
                Log.e("undoLastPlay ERROR", error.toString())
            }
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
        val params = JSONObject()
        params.put("game_id", gameId)
        val request = JsonObjectRequest(
            Request.Method.POST,
            BASE_URL + "getLatestPlay",
            params,
            { response ->
                Log.e("getLatestPlay SUCCESS", response.toString())
                onSuccess(response) },
            { error ->

                if (error.networkResponse != null && error.networkResponse.data != null) {

                    val raw = String(error.networkResponse.data)

                    Log.e("RAW_RESPONSE", raw)

                    try {
                        val json = JSONObject(raw)

                        val receivedGameId = json.getInt("received_game_id")

                        Log.e("Received_Game_ID", receivedGameId.toString())

                    } catch (e: Exception) {
                        Log.e("JSON_PARSE_ERROR", e.toString())
                    }

                } else {
                    Log.e("VolleyError", error.toString())
                }

                onError(error.toString())
            }
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

    /* ---------------- END GAME ---------------- */
    fun endGame(
        context: Context,
        gameId: Int,
        finalHomeScore: Int,
        finalAwayScore: Int,
        onSuccess: (Boolean) -> Unit,
        onError: (String) -> Unit
    ) {

        val params = JSONObject()
        params.put("game_id", gameId)
        params.put("final_home_score", finalHomeScore)
        params.put("final_away_score", finalAwayScore)

        val request = JsonObjectRequest(
            Request.Method.POST,
            BASE_URL + "endGame",
            params,
            { response ->
                val success = response.optBoolean("success", false)
                onSuccess(success)
            },
            { error ->
                onError(error.toString())
            }
        )

        request.setShouldCache(false)
        Volley.newRequestQueue(context).add(request)
    }

    fun getBattingStats(
        context: Context,
        onSuccess: (JSONArray) -> Unit,
        onError: (String) -> Unit
    ) {
        val url = BASE_URL + "getBattingStats"

        val request = JsonObjectRequest(
            Request.Method.POST,
            url,
            null,
            { response ->
                Log.e("getBattingStats SUCCESS", response.toString())
                try {
                    val players = response.getJSONArray("players")
                    onSuccess(players)
                } catch (e: Exception) {
                    onError(e.toString())
                }
            },
            { error ->
                onError(error.toString())
            }
        )

        request.setShouldCache(false)
        Volley.newRequestQueue(context).add(request)
    }
    fun getPitchingStats(
        context: Context,
        onSuccess: (JSONArray) -> Unit,
        onError: (String) -> Unit
    ) {
        Log.e("Function", "getPitchingStats function called")
        val request = JsonObjectRequest(
            Request.Method.POST,
            BASE_URL + "getPitchingStats",
            null,
            { response ->
                Log.e("getPitchingStats SUCCESS", response.toString())
                try {
                    val players = response.getJSONArray("players")
                    onSuccess(players)
                } catch (e: Exception) {
                    Log.e("getPitchingStats ERROR", "Failed to parse pitching stats: ${e.toString()}")
                    onError(e.toString())
                }
            },
            { error ->
                Log.e("getPitchingStats ERROR", "Volley error: ${error.message}")

                error.networkResponse?.let {
                    Log.e("getPitchingStats ERROR", "Status code: ${it.statusCode}")
                    Log.e("getPitchingStats ERROR", "Response: ${String(it.data)}")
                }

                onError(error.toString())
            }
        )

        request.setShouldCache(false)
        Log.e("Volley", "Adding getPitchingStats request to queue")
        Volley.newRequestQueue(context).add(request)
    }

}