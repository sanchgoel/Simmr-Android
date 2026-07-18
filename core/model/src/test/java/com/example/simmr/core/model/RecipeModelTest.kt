package com.example.simmr.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class RecipeModelTest {
    @Test fun `quantity formatting matches kitchen fractions`() {
        assertEquals("¼", QuantityFormatter.format(.24))
        assertEquals("1½", QuantityFormatter.format(1.49))
        assertEquals("2", QuantityFormatter.format(2.01))
    }

    @Test fun `serving scaling rounds to nearest quarter and preserves source`() {
        val original = recipe(quantity = 1.5, servings = 4)
        val scaled = original.scaledTo(3)
        assertEquals(3, scaled.servings)
        assertEquals(1.0, scaled.ingredients.single().quantity!!, 0.0)
        assertEquals(1.5, original.ingredients.single().quantity!!, 0.0)
    }

    @Test fun `units round trip without display drift`() {
        val pounds = WeightUnit.GRAM.convert(300.0, WeightUnit.POUND)
        assertEquals(300.0, WeightUnit.POUND.convert(pounds, WeightUnit.GRAM), .0001)
        assertEquals(VolumeUnit.CUP, VolumeUnit.matching("cups"))
        assertEquals(WeightUnit.GRAM, WeightUnit.matching("g"))
    }

    @Test fun `ingredient density is specific with safe fallback`() {
        assertEquals(120.0, IngredientDensity.gramsPerCup("all-purpose flour"), 0.0)
        assertNotEquals(
            IngredientDensity.gramsFromMilliliters(236.588, "flour"),
            IngredientDensity.gramsFromMilliliters(236.588, "water"),
        )
    }

    private fun recipe(quantity: Double, servings: Int) = Recipe(
        title = "Test", servings = servings,
        ingredients = listOf(Ingredient("Rice", quantity, "cup")),
        steps = listOf(RecipeStep(1, "Cook", "Cook it")),
    )
}
