package com.example.simmr.core.data

import com.example.simmr.core.model.Ingredient
import com.example.simmr.core.model.Recipe
import com.example.simmr.core.model.RecipeStep
import org.json.JSONArray
import org.json.JSONObject

object RecipeJson {
    fun decode(json: String): Recipe {
        val root = JSONObject(json)
        return Recipe(
            title = root.getString("title"),
            description = root.nullableString("description"),
            servings = root.getInt("servings"),
            prepTimeMinutes = root.nullableInt("prepTimeMinutes"),
            cookTimeMinutes = root.nullableInt("cookTimeMinutes"),
            caloriesPerServing = root.nullableInt("caloriesPerServing"),
            optimizationSummary = root.nullableString("optimizationSummary"),
            difficulty = root.nullableString("difficulty"),
            cuisine = root.nullableString("cuisine"),
            mealType = root.stringList("mealType"),
            dietaryTags = root.stringList("dietaryTags"),
            ingredients = root.getJSONArray("ingredients").objects().map { ingredient ->
                Ingredient(
                    name = ingredient.getString("name"),
                    quantity = ingredient.nullableDouble("quantity"),
                    unit = ingredient.nullableString("unit"),
                    section = ingredient.nullableString("section"),
                    optional = ingredient.optBoolean("optional"),
                    prep = ingredient.nullableString("prep"),
                )
            },
            steps = root.getJSONArray("steps").objects().map { step ->
                RecipeStep(
                    stepNumber = step.getInt("stepNumber"),
                    title = step.getString("title"),
                    instruction = step.getString("instruction"),
                    ingredientsUsed = step.stringList("ingredientsUsed"),
                    timerSeconds = step.nullableInt("timerSeconds"),
                    tips = step.nullableString("tips"),
                    cookware = step.nullableString("cookware"),
                    heatLevel = step.nullableString("heatLevel"),
                    lid = step.nullableString("lid"),
                    visualCue = step.nullableString("visualCue"),
                )
            },
        )
    }

    private fun JSONObject.nullableString(key: String): String? =
        if (!has(key) || isNull(key)) null else getString(key)
    private fun JSONObject.nullableInt(key: String): Int? =
        if (!has(key) || isNull(key)) null else getInt(key)
    private fun JSONObject.nullableDouble(key: String): Double? =
        if (!has(key) || isNull(key)) null else getDouble(key)
    private fun JSONObject.stringList(key: String): List<String> =
        optJSONArray(key)?.let { array -> List(array.length()) { array.getString(it) } }.orEmpty()
    private fun JSONArray.objects(): List<JSONObject> = List(length()) { getJSONObject(it) }
}

internal object RecipeSchema {
    val json: JSONObject by lazy {
        fun nullable(type: String) = JSONArray(listOf(type, "null"))
        fun property(type: Any, description: String? = null, values: List<Any>? = null) =
            JSONObject().put("type", type).apply {
                description?.let { put("description", it) }
                values?.let { put("enum", JSONArray(it)) }
            }
        fun objectSchema(properties: JSONObject, required: List<String>) = JSONObject()
            .put("type", "object")
            .put("properties", properties)
            .put("required", JSONArray(required))
            .put("additionalProperties", false)

        val ingredientFields = listOf("name", "quantity", "unit", "section", "optional", "prep")
        val ingredient = objectSchema(
            JSONObject()
                .put("name", property("string"))
                .put("quantity", property(nullable("number")))
                .put("unit", property(nullable("string")))
                .put("section", property(nullable("string"), "Ingredient group such as Marinade, Curry, Garnish."))
                .put("optional", property("boolean"))
                .put("prep", property(nullable("string"), "Short prep note such as 'finely minced' or 'diced'.")),
            ingredientFields,
        )
        val stepFields = listOf("stepNumber", "title", "instruction", "ingredientsUsed", "timerSeconds", "tips", "cookware", "heatLevel", "lid", "visualCue")
        val step = objectSchema(
            JSONObject()
                .put("stepNumber", property("integer"))
                .put("title", property("string", "A short 3-6 word step title."))
                .put("instruction", property("string"))
                .put("ingredientsUsed", JSONObject().put("type", "array").put("items", property("string")))
                .put("timerSeconds", property(nullable("integer")))
                .put("tips", property(nullable("string")))
                .put("cookware", property(nullable("string"), "Cookware used in this step, if relevant."))
                .put("heatLevel", property(nullable("string"), "Stove/oven heat level for this step, if relevant, e.g. low, medium, high."))
                .put("lid", property(nullable("string"), "Whether the lid should be on or off, if relevant.", listOf("on", "off", JSONObject.NULL)))
                .put("visualCue", property(nullable("string"), "How to visually judge doneness for this step, if relevant.")),
            stepFields,
        )
        val fields = listOf("title", "description", "servings", "prepTimeMinutes", "cookTimeMinutes", "caloriesPerServing", "optimizationSummary", "difficulty", "cuisine", "mealType", "dietaryTags", "ingredients", "steps")
        objectSchema(
            JSONObject()
                .put("title", property("string", "The recipe's title."))
                .put("description", property(nullable("string"), "A one to two sentence description of the dish."))
                .put("servings", property("integer", "Number of servings. Estimate a reasonable default if not stated."))
                .put("prepTimeMinutes", property(nullable("integer"), "Prep time in minutes, if stated or inferable."))
                .put("cookTimeMinutes", property(nullable("integer"), "Cook time in minutes, if stated or inferable."))
                .put("caloriesPerServing", property(nullable("integer"), "Estimated calories per serving."))
                .put("optimizationSummary", property(nullable("string"), "Short note on what was changed to satisfy requested optimizations, or null if none were requested."))
                .put("difficulty", property("string", "Difficulty for a home cook.", listOf("easy", "medium", "hard")))
                .put("cuisine", property("string", "The single most fitting cuisine, e.g. Italian, Indian, Fusion."))
                .put(
                    "mealType",
                    JSONObject()
                        .put("type", "array")
                        .put("description", "Meal occasions this dish fits, e.g. [\"Dinner\"] or [\"Breakfast\", \"Snack\"].")
                        .put("items", property("string")),
                )
                .put(
                    "dietaryTags",
                    JSONObject()
                        .put("type", "array")
                        .put("description", "Dietary labels the recipe genuinely satisfies, e.g. Vegetarian, Vegan, Gluten-Free, High Protein.")
                        .put("items", property("string")),
                )
                .put("ingredients", JSONObject().put("type", "array").put("items", ingredient))
                .put("steps", JSONObject().put("type", "array").put("items", step)),
            fields,
        )
    }
}
