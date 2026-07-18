package com.example.simmr

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.simmr.core.designsystem.theme.SimmrColors
import com.example.simmr.core.designsystem.theme.SimmrSpacing
import com.example.simmr.core.designsystem.theme.SimmrTextStyles
import com.example.simmr.core.designsystem.theme.SimmrTheme
import com.example.simmr.feature.onboarding.data.OnboardingStatus
import com.example.simmr.feature.onboarding.ui.OnboardingRoute

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT,
            ),
            navigationBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT,
            ),
        )
        setContent {
            SimmrTheme {
                var onboardingComplete by remember {
                    mutableStateOf(OnboardingStatus.isComplete(this@MainActivity))
                }
                if (onboardingComplete) {
                    PostOnboardingPlaceholder()
                } else {
                    OnboardingRoute(onComplete = { onboardingComplete = true })
                }
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun PostOnboardingPlaceholder() {
    Box(
        modifier = Modifier.fillMaxSize().background(SimmrColors.CreamBackground),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(SimmrSpacing.Xs),
        ) {
            Text("Simmr", style = SimmrTextStyles.LargeTitle, color = SimmrColors.TextDark)
            Text(
                "Your Kitchen Profile is ready.",
                style = SimmrTextStyles.Body,
                color = SimmrColors.TextMuted,
            )
        }
    }
}
