package com.example.simmr.core.data

import android.content.Context
import com.example.simmr.core.model.AppConstants
import com.example.simmr.core.model.Recipe
import com.example.simmr.core.model.RecipeOptimization
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

interface RecipeRepository {
    suspend fun generate(input: String, optimizations: Set<RecipeOptimization>): Recipe
}

internal object OpenAIRecipeRequest {
    fun create(systemPrompt: String, userPrompt: String): JSONObject = JSONObject()
        .put("model", AppConstants.Network.OPENAI_MODEL)
        .put("temperature", AppConstants.Network.TEMPERATURE)
        .put(
            "messages",
            JSONArray()
                .put(JSONObject().put("role", "system").put("content", systemPrompt))
                .put(JSONObject().put("role", "user").put("content", userPrompt)),
        )
        .put(
            "response_format",
            JSONObject().put("type", "json_schema").put(
                "json_schema",
                JSONObject()
                    .put("name", AppConstants.Network.RECIPE_SCHEMA_NAME)
                    .put("strict", true)
                    .put("schema", RecipeSchema.json),
            ),
        )
}

class OpenAIRecipeRepository(
    context: Context,
    private val settings: SettingsStore = SettingsStore(context),
) : RecipeRepository {
    private val appContext = context.applicationContext

    override suspend fun generate(input: String, optimizations: Set<RecipeOptimization>): Recipe =
        withContext(Dispatchers.IO) {
            val apiKey = settings.apiKey().orEmpty()
            if (apiKey.isBlank()) throw RecipeGenerationException.MissingApiKey

            val request = OpenAIRecipeRequest.create(
                systemPrompt = settings.promptOverride() ?: RecipePrompt.DEFAULT,
                userPrompt = userPrompt(input, optimizations),
            )

            val connection = (URL(AppConstants.Network.OPENAI_ENDPOINT).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = AppConstants.Network.CONNECT_TIMEOUT_MS
                readTimeout = AppConstants.Network.READ_TIMEOUT_MS
                doOutput = true
                setRequestProperty("Authorization", "Bearer $apiKey")
                setRequestProperty("Content-Type", "application/json")
            }
            try {
                connection.outputStream.bufferedWriter().use { it.write(request.toString()) }
                val status = connection.responseCode
                val body = (if (status in 200..299) connection.inputStream else connection.errorStream)
                    ?.bufferedReader()?.use { it.readText() }.orEmpty()
                if (status !in 200..299) {
                    val apiMessage = runCatching { JSONObject(body).getJSONObject("error").getString("message") }.getOrNull()
                    throw RecipeGenerationException.RequestFailed(status, apiMessage)
                }
                val content = JSONObject(body)
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")
                runCatching { RecipeJson.decode(content) }
                    .getOrElse { throw RecipeGenerationException.MalformedResponse(it) }
            } catch (error: RecipeGenerationException) {
                throw error
            } catch (error: Exception) {
                throw RecipeGenerationException.Network(error)
            } finally {
                connection.disconnect()
            }
        }

    private fun userPrompt(input: String, optimizations: Set<RecipeOptimization>): String {
        val instructions = optimizations.mapNotNull(OptimizationInstructions::forOption)
        val optimizedInput = if (instructions.isEmpty()) input else buildString {
            append(input)
            append("\n\nAdditional optimization instructions for this recipe:\n")
            instructions.forEach { append("- ").append(it).append('\n') }
        }
        return JSONObject()
            .put("input", optimizedInput)
            .put("userProfile", kitchenProfileJson() ?: JSONObject.NULL)
            .toString()
    }

    private fun kitchenProfileJson(): JSONObject? = runCatching {
        val raw = appContext.getSharedPreferences(
            AppConstants.Storage.KITCHEN_PROFILE_FILE,
            Context.MODE_PRIVATE,
        ).getString(AppConstants.Storage.KITCHEN_PROFILE_KEY, null) ?: return null
        val root = JSONObject(raw)
        if (!root.optBoolean("isComplete")) return null
        val answers = root.optJSONObject("answers") ?: JSONObject()
        fun values(questionId: String): JSONArray {
            val answer = answers.optJSONObject(questionId) ?: return JSONArray()
            val values = answer.optJSONArray("selectedOptionIds") ?: JSONArray()
            answer.optString("otherText").trim().takeIf(String::isNotEmpty)?.let(values::put)
            return values
        }
        fun single(questionId: String): Any = values(questionId).let { if (it.length() > 0) it.get(0) else JSONObject.NULL }
        JSONObject()
            .put("cookingFrequency", single("cook_frequency"))
            .put("cooksFor", values("cook_for"))
            .put("cookingSkill", single("confidence"))
            .put("cookingMotivations", values("why_cook"))
            .put("cookingFrustrations", values("frustration"))
            .put("availableAppliances", values("appliances"))
            .put("availableCookingTime", single("time_available"))
            .put("mealsCooked", values("meals_cooked"))
            .put("diet", values("diet"))
            .put("foodAllergies", values("allergies"))
            .put("medicalConditions", values("medical_conditions"))
            .put("foodsToAvoid", values("foods_avoided"))
            .put("nutritionGoals", values("nutrition_goals"))
            .put("preferredCuisines", values("cuisines"))
            .put("spicePreference", single("spice_level"))
            .put("assistantPreferences", values("ai_help"))
            .put("measurementSystem", single("measurement_units"))
    }.getOrNull()
}

sealed class RecipeGenerationException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    data object MissingApiKey : RecipeGenerationException("Add your OpenAI API key in Settings to generate recipes.")
    class Network(cause: Throwable) : RecipeGenerationException("Couldn't reach OpenAI. Check your connection and try again.", cause)
    class RequestFailed(val statusCode: Int, apiMessage: String?) : RecipeGenerationException(
        when (statusCode) {
            401 -> "Your OpenAI API key looks invalid. Check it in Settings."
            429 -> "Rate limit reached. Please wait a moment and try again."
            else -> apiMessage ?: "OpenAI request failed ($statusCode). Please try again."
        },
    )
    class MalformedResponse(cause: Throwable) : RecipeGenerationException("Couldn't read the response from OpenAI. Please try again.", cause)
}

internal object OptimizationInstructions {
    private val values = mapOf(
        RecipeOptimization.LOWER_CALORIES to "Reduce overall calories per serving where reasonable (lighter cooking methods, portion-conscious amounts, lower-calorie substitutions) without losing the dish's identity.",
        RecipeOptimization.HIGHER_PROTEIN to "Increase the protein content where reasonable (larger protein portions, protein-rich additions or substitutions) while keeping the dish coherent.",
        RecipeOptimization.LOWER_SUGAR to "Reduce the sugar content where reasonable (less added sugar, lower-sugar substitutions) while keeping the dish palatable.",
        RecipeOptimization.LOW_CARB to "Make this recipe lower-carb where reasonable (swap or reduce high-carb ingredients like rice, pasta, bread, or sugar) while keeping the dish coherent.",
        RecipeOptimization.DAIRY_FREE to "Make this recipe dairy-free by substituting any dairy ingredients (milk, butter, cheese, cream, yogurt) with suitable dairy-free alternatives.",
        RecipeOptimization.SPICIER to "Make this recipe spicier — increase existing chilies/spices or add appropriate heat, while keeping the dish's overall flavor profile.",
        RecipeOptimization.KID_FRIENDLY to "Make this recipe more kid-friendly — mild flavors, less spice, familiar and approachable ingredients.",
    )
    fun forOption(option: RecipeOptimization): String? = values[option]
}
