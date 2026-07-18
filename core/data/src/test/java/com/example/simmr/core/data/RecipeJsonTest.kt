package com.example.simmr.core.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RecipeJsonTest {
    @Test fun `decodes strict structured recipe response`() {
        val recipe = RecipeJson.decode(RECIPE_JSON)
        assertEquals("Tomato Pasta", recipe.title)
        assertEquals(2, recipe.servings)
        assertEquals("Tomato", recipe.ingredients.single().name)
        assertEquals(300, recipe.steps.single().timerSeconds)
        assertNull(recipe.optimizationSummary)
    }

    private companion object {
        const val RECIPE_JSON = """{
          "title":"Tomato Pasta","description":null,"servings":2,
          "prepTimeMinutes":5,"cookTimeMinutes":15,"caloriesPerServing":430,
          "optimizationSummary":null,"difficulty":"easy","cuisine":"Italian",
          "mealType":["Dinner"],"dietaryTags":["Vegetarian"],
          "ingredients":[{"name":"Tomato","quantity":2,"unit":"cup","section":null,"optional":false,"prep":"diced"}],
          "steps":[{"stepNumber":1,"title":"Simmer sauce","instruction":"Simmer until thick.","ingredientsUsed":["Tomato"],"timerSeconds":300,"tips":null,"cookware":"pan","heatLevel":"medium","lid":"off","visualCue":"thickened"}]
        }"""
    }
}
