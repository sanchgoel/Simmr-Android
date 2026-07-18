package com.example.simmr

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import com.example.simmr.core.data.RecipeRepository
import com.example.simmr.core.data.SettingsStore
import com.example.simmr.core.designsystem.theme.SimmrTheme
import com.example.simmr.core.model.Ingredient
import com.example.simmr.core.model.Recipe
import com.example.simmr.core.model.RecipeStep
import com.example.simmr.feature.cooking.CookingScreen
import com.example.simmr.feature.home.HomeRoute
import com.example.simmr.feature.recipe.RecipeOverviewScreen
import org.junit.Rule
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlinx.coroutines.delay

class JourneyE2ETest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test fun overviewToCookingToFinish() {
        var stage by mutableStateOf("home")
        compose.setContent {
            SimmrTheme {
                when (stage) {
                    "home" -> HomeRoute(onRecipeGenerated = { stage = "overview" }, repository = FakeRepository(sample))
                    "overview" -> RecipeOverviewScreen(sample, {}, { stage = "cooking" })
                    else -> CookingScreen(sample, {}, { stage = "finished" })
                }
            }
        }
        compose.onNode(hasSetTextAction()).performTextInput("tomato pasta")
        compose.onNodeWithText("tomato pasta").assertIsDisplayed()
        compose.onNodeWithTag("generate_recipe").assertIsDisplayed().assertIsEnabled().performClick()
        compose.onNodeWithText("Analysing your recipe…").assertIsDisplayed()
        compose.waitUntil(5_000) { stage == "overview" }
        compose.onNodeWithText("Tomato Pasta").assertIsDisplayed()
        compose.onNodeWithText("Tomato").performClick()
        compose.onNodeWithText("1 of 1 ingredients available").assertIsDisplayed()
        compose.onNodeWithText("Start Cooking").performClick()
        compose.onNodeWithText("Simmer sauce").assertIsDisplayed()
        compose.onNodeWithContentDescription("Unit converter").performClick()
        compose.onNodeWithText("Unit Converter").assertIsDisplayed()
        compose.onNodeWithTag("converter_done").assertIsDisplayed().performClick()
        compose.onNodeWithText("Unit Converter").assertDoesNotExist()
        compose.onNodeWithText("Timer").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("1 min").assertIsDisplayed()
        compose.onNodeWithText("Start").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Finish").performClick()
        compose.runOnIdle { assertEquals("finished", stage) }
    }

    @Test fun savedApiKeyPromptDisappearsImmediately() {
        val settings = SettingsStore(compose.activity)
        settings.clearApiKey()
        compose.setContent {
            SimmrTheme { HomeRoute(onRecipeGenerated = {}, repository = FakeRepository(sample)) }
        }
        compose.onNodeWithText("Add your OpenAI API key to generate recipes").performScrollTo().assertIsDisplayed()
        compose.onNodeWithContentDescription("Settings").performScrollTo().performClick()
        compose.onNodeWithTag("api_key_input").performTextInput("sk-test-key")
        compose.onNodeWithTag("save_api_key").performScrollTo().performClick()
        compose.onNodeWithContentDescription("Done").performClick()
        compose.onNodeWithText("Add your OpenAI API key to generate recipes").assertDoesNotExist()
        settings.clearApiKey()
    }

    private val sample = Recipe(
        title = "Tomato Pasta", servings = 2,
        ingredients = listOf(Ingredient("Tomato", 2.0, "cup")),
        steps = listOf(RecipeStep(1, "Simmer sauce", "Simmer until thick.", listOf("Tomato"), 60)),
    )

    private class FakeRepository(private val recipe: Recipe) : RecipeRepository {
        override suspend fun generate(input: String, optimizations: Set<com.example.simmr.core.model.RecipeOptimization>): Recipe {
            delay(500)
            return recipe
        }
    }
}
