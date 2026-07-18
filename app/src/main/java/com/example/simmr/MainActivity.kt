package com.example.simmr

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.sp
import com.example.simmr.core.designsystem.theme.SimmrColors
import com.example.simmr.core.designsystem.theme.SimmrTextStyles
import com.example.simmr.core.designsystem.theme.SimmrTheme
import com.example.simmr.core.model.AppConstants
import com.example.simmr.core.model.Recipe
import com.example.simmr.feature.cooking.CookingScreen
import com.example.simmr.feature.home.HomeRoute
import com.example.simmr.feature.onboarding.data.OnboardingStatus
import com.example.simmr.feature.onboarding.ui.OnboardingRoute
import com.example.simmr.feature.recipe.RecipeOverviewScreen
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
        )
        setContent { SimmrTheme { SimmrApp(OnboardingStatus.isComplete(this)) } }
    }
}

private sealed interface AppScreen {
    data object Home : AppScreen
    data class Overview(val recipe: Recipe) : AppScreen
    data class Cooking(val recipe: Recipe) : AppScreen
}

@Composable
private fun SimmrApp(initiallyOnboarded: Boolean) {
    var launchComplete by remember { mutableStateOf(false) }
    var onboardingComplete by remember { mutableStateOf(initiallyOnboarded) }
    var screen by remember { mutableStateOf<AppScreen>(AppScreen.Home) }
    AnimatedContent(
        targetState = launchComplete,
        modifier = Modifier.fillMaxSize(),
        transitionSpec = { fadeIn(tween(AppUiConstants.ROOT_FADE_MS)) togetherWith fadeOut(tween(AppUiConstants.ROOT_FADE_MS)) },
        label = "root",
    ) { launched ->
        if (!launched) LaunchScreen { launchComplete = true }
        else if (!onboardingComplete) OnboardingRoute(onComplete = { onboardingComplete = true })
        else AnimatedContent(
            targetState = screen,
            modifier = Modifier.fillMaxSize(),
            transitionSpec = { fadeIn(tween(AppUiConstants.NAV_FADE_MS)) togetherWith fadeOut(tween(AppUiConstants.NAV_FADE_MS)) },
            label = "navigation",
        ) { destination ->
            when (destination) {
                AppScreen.Home -> HomeRoute(onRecipeGenerated = { screen = AppScreen.Overview(it) })
                is AppScreen.Overview -> RecipeOverviewScreen(destination.recipe, { screen = AppScreen.Home }) {
                    screen = AppScreen.Cooking(it)
                }
                is AppScreen.Cooking -> CookingScreen(
                    recipe = destination.recipe,
                    onBack = { screen = AppScreen.Overview(destination.recipe) },
                    onFinish = { screen = AppScreen.Home },
                )
            }
        }
    }
    BackHandler(enabled = launchComplete && onboardingComplete && screen !is AppScreen.Home) {
        screen = when (val current = screen) {
            is AppScreen.Cooking -> AppScreen.Overview(current.recipe)
            is AppScreen.Overview -> AppScreen.Home
            AppScreen.Home -> AppScreen.Home
        }
    }
}

@Composable
private fun LaunchScreen(onComplete: () -> Unit) {
    var count by remember { mutableIntStateOf(1) }
    var revealed by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        revealed = true
        delay(AppUiConstants.LAUNCH_SETTLE_MS)
        for (next in 2..AppConstants.APP_NAME.length) {
            count = next
            delay(AppUiConstants.LETTER_STAGGER_MS)
        }
        delay(AppUiConstants.LAUNCH_HOLD_MS)
        onComplete()
    }
    Box(Modifier.fillMaxSize().background(SimmrColors.CreamBackground), contentAlignment = Alignment.Center) {
        Text(
            AppConstants.APP_NAME.take(count),
            style = SimmrTextStyles.LargeTitle.copy(fontSize = AppUiConstants.SPLASH_FONT_SIZE.sp),
            color = SimmrColors.Coral,
            modifier = Modifier.scale(if (revealed) 1f else .5f).alpha(if (revealed) 1f else 0f),
        )
    }
}

private object AppUiConstants {
    const val ROOT_FADE_MS = 300
    const val NAV_FADE_MS = 200
    const val LAUNCH_SETTLE_MS = 550L
    const val LETTER_STAGGER_MS = 130L
    const val LAUNCH_HOLD_MS = 300L
    const val SPLASH_FONT_SIZE = 64
}
