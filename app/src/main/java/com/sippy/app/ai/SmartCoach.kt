package com.sippy.app.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection

data class CoachContext(
    val friendName: String,
    val totalMl: Int,
    val goalMl: Int,
    val streak: Int,
    val mood: String,
    val average7DaysMl: Int
)

data class CoachState(
    val busy: Boolean = false,
    val answer: String? = null,
    val error: String? = null
)

/** Local-first coaching: deterministic insights need no network. AI answers are opt-in. */
object SmartCoach {
    private const val ENDPOINT = "https://openrouter.ai/api/v1/chat/completions"
    private const val MODEL = "openai/gpt-4o-mini"

    fun localInsights(c: CoachContext): List<String> {
        val remaining = (c.goalMl - c.totalMl).coerceAtLeast(0)
        val result = mutableListOf<String>()
        when {
            remaining == 0 -> result.add("${c.friendName} is dancing — target reached. Protect the streak with a small evening sip.")
            c.totalMl == 0 -> result.add("No water yet today. A 250 ml sip is the easiest way to wake ${c.friendName} up.")
            else -> result.add("$remaining ml to go. Two 250 ml glasses would almost finish the day.")
        }
        if (c.average7DaysMl > 0) {
            val delta = c.totalMl - c.average7DaysMl
            result.add(if (delta >= 0) "You are $delta ml ahead of your 7-day average at this point." else
                "You are ${-delta} ml behind your 7-day average — same day, last week.")
        }
        if (c.streak > 0) result.add("Streak: $c.streak day(s). Consistency matters more than a huge single day.")
        result.add("Current mood: ${c.mood}. ${c.friendName} reacts to every entry you log.")
        return result
    }

    suspend fun ask(apiKey: String, question: String, context: CoachContext): String =
        withContext(Dispatchers.IO) {
            if (apiKey.isBlank()) error("Add your OpenRouter API key in Settings first")
            val system = """
                You are the hydration coach inside Sippy, a friendly water-tracking app.
                Be brief, warm and practical. Never diagnose medical conditions.
                Local app state: friend=${context.friendName}, today=${context.totalMl}ml of ${context.goalMl}ml,
                streak=${context.streak} days, mood=${context.mood}, 7-day average=${context.average7DaysMl}ml.
            """.trimIndent()
            val payload = JSONObject()
                .put("model", MODEL)
                .put("max_tokens", 180)
                .put("messages", JSONArray()
                    .put(JSONObject().put("role", "system").put("content", system))
                    .put(JSONObject().put("role", "user").put("content", question)))

            val connection = URL(ENDPOINT).openConnection() as HttpsURLConnection
            try {
                connection.requestMethod = "POST"
                connection.connectTimeout = 15_000
                connection.readTimeout = 30_000
                connection.doOutput = true
                connection.setRequestProperty("Authorization", "Bearer $apiKey")
                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("HTTP-Referer", "https://cooreo.github.io/Sippy/")
                connection.setRequestProperty("X-Title", "Sippy")
                connection.outputStream.use { it.write(payload.toString().toByteArray(Charsets.UTF_8)) }
                val code = connection.responseCode
                val stream = if (code in 200..299) connection.inputStream else connection.errorStream
                val response = stream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                if (code !in 200..299) error("OpenRouter HTTP $code: ${response.take(160)}")
                val choices = JSONObject(response).getJSONArray("choices")
                if (choices.length() == 0) error("OpenRouter returned no choices")
                choices.getJSONObject(0).getJSONObject("message").optString("content").ifBlank {
                    error("OpenRouter returned an empty reply")
                }
            } finally {
                connection.disconnect()
            }
        }
}
