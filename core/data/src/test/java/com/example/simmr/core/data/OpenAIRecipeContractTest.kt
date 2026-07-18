package com.example.simmr.core.data

import com.example.simmr.core.model.AppConstants
import com.example.simmr.core.model.RecipeOptimization
import org.json.JSONArray
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OpenAIRecipeContractTest {
    @Test fun `request uses the iOS structured output envelope`() {
        val request = OpenAIRecipeRequest.create("system prompt", "user payload")

        assertEquals(AppConstants.Network.OPENAI_MODEL, request.getString("model"))
        assertEquals(AppConstants.Network.TEMPERATURE, request.getDouble("temperature"), 0.0)
        val messages = request.getJSONArray("messages")
        assertEquals("system", messages.getJSONObject(0).getString("role"))
        assertEquals("system prompt", messages.getJSONObject(0).getString("content"))
        assertEquals("user", messages.getJSONObject(1).getString("role"))
        assertEquals("user payload", messages.getJSONObject(1).getString("content"))

        val format = request.getJSONObject("response_format")
        assertEquals("json_schema", format.getString("type"))
        val structured = format.getJSONObject("json_schema")
        assertEquals("recipe", structured.getString("name"))
        assertTrue(structured.getBoolean("strict"))
        assertFalse(structured.getJSONObject("schema").getBoolean("additionalProperties"))
    }

    @Test fun `schema matches required iOS enums and nullability`() {
        val properties = RecipeSchema.json.getJSONObject("properties")
        val difficulty = properties.getJSONObject("difficulty")
        assertEquals("string", difficulty.getString("type"))
        assertEquals(listOf("easy", "medium", "hard"), difficulty.getJSONArray("enum").strings())
        assertEquals("string", properties.getJSONObject("cuisine").getString("type"))

        val step = properties.getJSONObject("steps").getJSONObject("items")
        val lid = step.getJSONObject("properties").getJSONObject("lid")
        assertEquals(listOf("string", "null"), lid.getJSONArray("type").strings())
        assertEquals("on", lid.getJSONArray("enum").getString(0))
        assertEquals("off", lid.getJSONArray("enum").getString(1))
        assertTrue(lid.getJSONArray("enum").isNull(2))
    }

    @Test fun `prompt and optimization instructions retain iOS guidance`() {
        assertTrue(RecipePrompt.DEFAULT.contains("You will receive a JSON object with two fields:"))
        assertTrue(RecipePrompt.DEFAULT.contains("spelled EXACTLY as they appear"))
        assertTrue(RecipePrompt.DEFAULT.contains("Never reuse the same duration across more than one step"))
        assertEquals(
            "Make this recipe more kid-friendly — mild flavors, less spice, familiar and approachable ingredients.",
            OptimizationInstructions.forOption(RecipeOptimization.KID_FRIENDLY),
        )
    }

    private fun JSONArray.strings(): List<String> = List(length()) { getString(it) }
}
