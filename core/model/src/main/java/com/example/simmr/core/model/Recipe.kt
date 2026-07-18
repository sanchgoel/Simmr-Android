package com.example.simmr.core.model

data class Recipe(
    val title: String,
    val description: String? = null,
    val servings: Int,
    val prepTimeMinutes: Int? = null,
    val cookTimeMinutes: Int? = null,
    val caloriesPerServing: Int? = null,
    val optimizationSummary: String? = null,
    val difficulty: String? = null,
    val cuisine: String? = null,
    val mealType: List<String> = emptyList(),
    val dietaryTags: List<String> = emptyList(),
    val ingredients: List<Ingredient>,
    val steps: List<RecipeStep>,
) {
    fun scaledTo(targetServings: Int): Recipe {
        val safeTarget = targetServings.coerceAtLeast(1)
        val factor = safeTarget.toDouble() / servings.coerceAtLeast(1)
        return copy(
            servings = safeTarget,
            ingredients = ingredients.map { ingredient ->
                ingredient.copy(quantity = ingredient.quantity?.let { QuantityFormatter.roundToQuarter(it * factor) })
            },
        )
    }
}

data class Ingredient(
    val name: String,
    val quantity: Double? = null,
    val unit: String? = null,
    val section: String? = null,
    val optional: Boolean = false,
    val prep: String? = null,
) {
    val quantityLabel: String?
        get() = quantity?.let { value ->
            listOfNotNull(QuantityFormatter.format(value), unit).joinToString(" ")
        }
}

data class RecipeStep(
    val stepNumber: Int,
    val title: String,
    val instruction: String,
    val ingredientsUsed: List<String> = emptyList(),
    val timerSeconds: Int? = null,
    val tips: String? = null,
    val cookware: String? = null,
    val heatLevel: String? = null,
    val lid: String? = null,
    val visualCue: String? = null,
) {
    val hasTimer: Boolean get() = (timerSeconds ?: 0) > 0
}

enum class RecipeOptimization(val label: String) {
    LOWER_CALORIES("Lower calories"),
    HIGHER_PROTEIN("More protein"),
    LOWER_SUGAR("Less sugar"),
    LOW_CARB("Low carb"),
    DAIRY_FREE("Dairy free"),
    SPICIER("More spicy"),
    KID_FRIENDLY("Kid friendly"),
}

object QuantityFormatter {
    private val fractions = mapOf(1 to "¼", 2 to "½", 3 to "¾")

    fun roundToQuarter(value: Double): Double =
        kotlin.math.round(value / AppConstants.Conversion.QUARTER) * AppConstants.Conversion.QUARTER

    fun format(value: Double): String {
        val rounded = roundToQuarter(value)
        val whole = kotlin.math.floor(rounded).toInt()
        val quarters = kotlin.math.round((rounded - whole) * 4).toInt()
        return when {
            quarters == 0 -> whole.toString()
            whole == 0 -> fractions.getValue(quarters)
            else -> "$whole${fractions.getValue(quarters)}"
        }
    }
}
