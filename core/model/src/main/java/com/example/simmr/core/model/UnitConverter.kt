package com.example.simmr.core.model

enum class ConversionCategory(val label: String) { VOLUME("Volume"), WEIGHT("Weight") }

enum class VolumeUnit(val symbol: String, private val milliliters: Double, vararg val aliases: String) {
    MILLILITER("ml", 1.0, "ml", "milliliter", "milliliters"),
    LITER("L", 1000.0, "l", "liter", "liters", "litre", "litres"),
    TEASPOON("tsp", 4.92892, "tsp", "teaspoon", "teaspoons"),
    TABLESPOON("tbsp", 14.7868, "tbsp", "tablespoon", "tablespoons"),
    FLUID_OUNCE("fl oz", 29.5735, "fl oz", "floz", "fluid ounce", "fluid ounces"),
    CUP("cup", 236.588, "cup", "cups"),
    PINT("pt", 473.176, "pt", "pint", "pints"),
    QUART("qt", 946.353, "qt", "quart", "quarts"),
    GALLON("gal", 3785.41, "gal", "gallon", "gallons");

    fun convert(value: Double, to: VolumeUnit): Double = value * milliliters / to.milliliters

    companion object {
        fun matching(text: String): VolumeUnit? {
            val normalized = text.trim().lowercase()
            return entries.firstOrNull { normalized == it.symbol.lowercase() || normalized in it.aliases }
        }
    }
}

enum class WeightUnit(val symbol: String, private val grams: Double, vararg val aliases: String) {
    GRAM("g", 1.0, "g", "gram", "grams"),
    KILOGRAM("kg", 1000.0, "kg", "kilogram", "kilograms"),
    OUNCE("oz", 28.3495, "oz", "ounce", "ounces"),
    POUND("lb", 453.592, "lb", "lbs", "pound", "pounds");

    fun convert(value: Double, to: WeightUnit): Double = value * grams / to.grams

    companion object {
        fun matching(text: String): WeightUnit? {
            val normalized = text.trim().lowercase()
            return entries.firstOrNull { normalized == it.symbol.lowercase() || normalized in it.aliases }
        }
    }
}

object IngredientDensity {
    private val densityByKeyword = linkedMapOf(
        "powdered sugar" to 120.0, "icing sugar" to 120.0, "confectioners" to 120.0,
        "brown sugar" to 220.0, "sugar" to 200.0, "flour" to 120.0, "maida" to 120.0,
        "cornstarch" to 128.0, "corn flour" to 128.0, "cornflour" to 128.0,
        "rice" to 185.0, "lentil" to 200.0, "dal" to 200.0, "bean" to 200.0,
        "chickpea" to 200.0, "oats" to 90.0, "butter" to 227.0, "ghee" to 227.0,
        "oil" to 218.0, "honey" to 340.0, "syrup" to 340.0, "molasses" to 340.0,
        "milk" to 245.0, "buttermilk" to 245.0, "cream" to 245.0, "yogurt" to 245.0,
        "curd" to 245.0, "water" to 236.0, "stock" to 236.0, "broth" to 236.0,
        "juice" to 236.0, "salt" to 273.0, "cocoa" to 100.0, "cacao" to 100.0,
        "breadcrumbs" to 108.0, "cheese" to 100.0, "parmesan" to 100.0,
        "cheddar" to 100.0, "nuts" to 120.0, "almond" to 120.0, "cashew" to 120.0,
        "walnut" to 120.0, "peanut" to 120.0,
    )

    fun gramsPerCup(ingredientName: String): Double {
        val normalized = ingredientName.lowercase()
        return densityByKeyword.entries.firstOrNull { normalized.contains(it.key) }?.value
            ?: AppConstants.Conversion.DEFAULT_GRAMS_PER_CUP
    }

    fun gramsFromMilliliters(milliliters: Double, ingredientName: String): Double =
        milliliters / AppConstants.Conversion.MILLILITERS_PER_CUP * gramsPerCup(ingredientName)

    fun millilitersFromGrams(grams: Double, ingredientName: String): Double =
        grams / gramsPerCup(ingredientName) * AppConstants.Conversion.MILLILITERS_PER_CUP
}

data class ConvertibleIngredient(
    val name: String,
    val category: ConversionCategory,
    val quantity: Double,
    val volumeUnit: VolumeUnit? = null,
    val weightUnit: WeightUnit? = null,
)
