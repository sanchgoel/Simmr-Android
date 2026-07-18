package com.example.simmr.feature.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.simmr.core.designsystem.components.SimmrCard
import com.example.simmr.core.designsystem.components.SimmrPrimaryButton
import com.example.simmr.core.designsystem.components.SimmrSecondaryButton
import com.example.simmr.core.designsystem.components.SimmrSelectionChip
import com.example.simmr.core.designsystem.theme.SimmrColors
import com.example.simmr.core.designsystem.theme.SimmrRadius
import com.example.simmr.core.designsystem.theme.SimmrSpacing
import com.example.simmr.core.designsystem.theme.SimmrStroke
import com.example.simmr.core.designsystem.theme.SimmrTextStyles
import com.example.simmr.core.designsystem.theme.SimmrTheme

@Composable
fun DesignSystemScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SimmrColors.CreamBackground)
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(SimmrSpacing.Lg),
        verticalArrangement = Arrangement.spacedBy(SimmrSpacing.Xl),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(SimmrSpacing.Xxs)) {
            Text(
                text = "Simmr Design Language",
                style = SimmrTextStyles.LargeTitle,
                color = SimmrColors.TextDark,
            )
            Text(
                text = "Rethink Sans · Color · Spacing",
                style = MaterialTheme.typography.bodyMedium,
                color = SimmrColors.TextMuted,
            )
        }

        Section("Colors") { ColorSection() }
        Section("Typography") { TypographySection() }
        Section("Spacing") { SpacingSection() }
        Section("Components") { ComponentSection() }
        Spacer(Modifier.height(SimmrSpacing.Lg))
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(SimmrSpacing.Sm)) {
        Text(title, style = MaterialTheme.typography.titleLarge, color = SimmrColors.TextDark)
        content()
    }
}

private data class Swatch(val name: String, val hex: String, val color: Color, val lightText: Boolean)

@Composable
private fun ColorSection() {
    val swatches = listOf(
        Swatch("Coral", "#FF5A36", SimmrColors.Coral, true),
        Swatch("Amber", "#FFB03B", SimmrColors.Amber, true),
        Swatch("Cream BG", "#FFFDF8", SimmrColors.CreamBackground, false),
        Swatch("Cream Card", "#FFF9F0", SimmrColors.CreamCard, false),
        Swatch("Text Dark", "#241B14", SimmrColors.TextDark, true),
        Swatch("Text Muted", "#9C8E7C", SimmrColors.TextMuted, true),
        Swatch("Border", "#EDE3CF", SimmrColors.Border, false),
        Swatch("Tint", "#FFF1EC", SimmrColors.Tint, false),
    )
    Column(verticalArrangement = Arrangement.spacedBy(SimmrSpacing.Sm)) {
        swatches.chunked(2).forEach { pair ->
            Row(horizontalArrangement = Arrangement.spacedBy(SimmrSpacing.Sm)) {
                pair.forEach { swatch -> ColorSwatch(swatch, Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun ColorSwatch(swatch: Swatch, modifier: Modifier = Modifier) {
    val foreground = if (swatch.lightText) Color.White else SimmrColors.TextDark
    Column(
        modifier = modifier
            .height(72.dp)
            .background(swatch.color, RoundedCornerShape(SimmrRadius.Sm))
            .border(SimmrStroke.Hairline, SimmrColors.Border, RoundedCornerShape(SimmrRadius.Sm))
            .padding(SimmrSpacing.Sm),
        verticalArrangement = Arrangement.spacedBy(SimmrSpacing.Xxs),
    ) {
        Text(swatch.name, style = MaterialTheme.typography.labelMedium, color = foreground)
        Text(swatch.hex, style = MaterialTheme.typography.labelSmall, color = foreground.copy(alpha = 0.8f))
    }
}

@Composable
private fun TypographySection() {
    val styles = listOf(
        "Large Title / ExtraBold 34" to SimmrTextStyles.LargeTitle,
        "Title / Bold 28" to MaterialTheme.typography.headlineLarge,
        "Title 2 / SemiBold 22" to MaterialTheme.typography.headlineSmall,
        "Title 3 / SemiBold 20" to MaterialTheme.typography.titleLarge,
        "Headline / SemiBold 17" to MaterialTheme.typography.titleMedium,
        "Body / Regular 17" to MaterialTheme.typography.bodyLarge,
        "Callout / Medium 16" to SimmrTextStyles.Callout,
        "Subheadline / Regular 15" to MaterialTheme.typography.bodyMedium,
        "Footnote / Regular 14" to MaterialTheme.typography.bodySmall,
        "Caption / Medium 12" to MaterialTheme.typography.labelMedium,
    )
    Column(verticalArrangement = Arrangement.spacedBy(SimmrSpacing.Sm)) {
        styles.forEach { (label, style) ->
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Simmr — Rethink Sans", style = style, color = SimmrColors.TextDark)
                Text(label, style = MaterialTheme.typography.labelSmall, color = SimmrColors.TextMuted)
            }
        }
    }
}

@Composable
private fun SpacingSection() {
    val spaces = listOf(
        "xxs" to SimmrSpacing.Xxs,
        "xs" to SimmrSpacing.Xs,
        "sm" to SimmrSpacing.Sm,
        "md" to SimmrSpacing.Md,
        "lg" to SimmrSpacing.Lg,
        "xl" to SimmrSpacing.Xl,
        "xxl" to SimmrSpacing.Xxl,
    )
    Column(verticalArrangement = Arrangement.spacedBy(SimmrSpacing.Xs)) {
        spaces.forEach { (name, value) -> SpacingBar(name, value) }
    }
}

@Composable
private fun SpacingBar(name: String, value: Dp) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(name, Modifier.width(36.dp), style = MaterialTheme.typography.labelMedium, color = SimmrColors.TextMuted)
        Box(
            Modifier
                .width(value * 3)
                .height(12.dp)
                .background(SimmrColors.Coral, RoundedCornerShape(SimmrRadius.Sm / 2)),
        )
        Spacer(Modifier.width(SimmrSpacing.Sm))
        Text("${value.value.toInt()}dp", style = MaterialTheme.typography.labelSmall, color = SimmrColors.TextMuted)
    }
}

@Composable
private fun ComponentSection() {
    var selectedChip by remember { mutableIntStateOf(0) }
    Column(verticalArrangement = Arrangement.spacedBy(SimmrSpacing.Md)) {
        SimmrPrimaryButton(onClick = {}) {
            Text("Primary CTA", style = SimmrTextStyles.Button)
        }
        SimmrSecondaryButton(onClick = {}) {
            Text("Secondary Action", style = SimmrTextStyles.Button)
        }
        SimmrCard(Modifier.fillMaxWidth()) {
            Column(
                Modifier.padding(SimmrSpacing.Md),
                verticalArrangement = Arrangement.spacedBy(SimmrSpacing.Xxs),
            ) {
                Text("Card Title", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Cards sit on creamCard with a hairline border and a large radius.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SimmrColors.TextMuted,
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(SimmrSpacing.Xs)) {
            repeat(3) { index ->
                SimmrSelectionChip(
                    selected = selectedChip == index,
                    onClick = { selectedChip = index },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Option ${index + 1}", style = SimmrTextStyles.Callout)
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 420, heightDp = 900)
@Composable
private fun DesignSystemScreenPreview() {
    SimmrTheme { DesignSystemScreen() }
}
