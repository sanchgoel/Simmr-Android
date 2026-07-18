package com.example.simmr.feature.recipe

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.simmr.core.designsystem.components.SimmrCard
import com.example.simmr.core.designsystem.components.SimmrPrimaryButton
import com.example.simmr.core.designsystem.components.SimmrStickyFooter
import com.example.simmr.core.designsystem.theme.SimmrColors
import com.example.simmr.core.designsystem.theme.SimmrRadius
import com.example.simmr.core.designsystem.theme.SimmrSpacing
import com.example.simmr.core.designsystem.theme.SimmrTextStyles
import com.example.simmr.core.model.Ingredient
import com.example.simmr.core.model.Recipe

@Composable
fun RecipeOverviewScreen(
    recipe: Recipe,
    onBack: () -> Unit,
    onStartCooking: (Recipe) -> Unit,
) {
    var servings by remember(recipe) { mutableIntStateOf(recipe.servings) }
    var checkedNames by remember(recipe) { mutableStateOf(emptySet<String>()) }
    val scaled = remember(recipe, servings) { recipe.scaledTo(servings) }
    val sections = remember(scaled) { scaled.ingredients.groupBy { it.section } }

    Column(Modifier.fillMaxSize().background(SimmrColors.CreamBackground).safeDrawingPadding()) {
        TopBar("Recipe", onBack)
        LazyColumn(
            Modifier.weight(1f).fillMaxWidth().widthIn(max = 720.dp).align(Alignment.CenterHorizontally),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(SimmrSpacing.Lg),
            verticalArrangement = Arrangement.spacedBy(SimmrSpacing.Lg),
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(SimmrSpacing.Xs)) {
                    Text(recipe.title, style = SimmrTextStyles.Title, color = SimmrColors.TextDark)
                    recipe.description?.let { Text(it, style = SimmrTextStyles.Subheadline, color = SimmrColors.TextMuted) }
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(SimmrSpacing.Xs), verticalArrangement = Arrangement.spacedBy(SimmrSpacing.Xs)) {
                        MetaPill("♟ ${recipe.servings} servings")
                        recipe.prepTimeMinutes?.let { MetaPill("◷ $it min prep") }
                        recipe.cookTimeMinutes?.let { MetaPill("♨ $it min cook") }
                        recipe.caloriesPerServing?.let { MetaPill("ϟ $it kcal") }
                    }
                }
            }
            recipe.optimizationSummary?.let { summary ->
                item {
                    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(SimmrRadius.Sm)).background(SimmrColors.Tint).padding(SimmrSpacing.Sm)) {
                        Text("✦  ", color = SimmrColors.Coral)
                        Text(summary, style = SimmrTextStyles.Footnote, color = SimmrColors.TextMuted)
                    }
                }
            }
            item {
                SimmrCard(Modifier.fillMaxWidth()) {
                    Row(Modifier.fillMaxWidth().padding(SimmrSpacing.Md), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Servings", style = SimmrTextStyles.Headline)
                            Text("Adjust ingredient quantities", style = SimmrTextStyles.Footnote, color = SimmrColors.TextMuted)
                        }
                        IconButton(onClick = { servings = (servings - 1).coerceAtLeast(1) }) { Icon(Icons.Rounded.Remove, "Decrease servings") }
                        Text(servings.toString(), style = SimmrTextStyles.Title2)
                        IconButton(onClick = { servings += 1 }) { Icon(Icons.Rounded.Add, "Increase servings") }
                    }
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(SimmrSpacing.Xs)) {
                    Text("${checkedNames.size} of ${scaled.ingredients.size} ingredients available", style = SimmrTextStyles.Subheadline, color = SimmrColors.TextMuted)
                    LinearProgressIndicator(
                        progress = { if (scaled.ingredients.isEmpty()) 0f else checkedNames.size.toFloat() / scaled.ingredients.size },
                        modifier = Modifier.fillMaxWidth().clip(CircleShape),
                        color = SimmrColors.Coral,
                        trackColor = SimmrColors.Border,
                        drawStopIndicator = {},
                    )
                }
            }
            sections.forEach { (section, ingredients) ->
                item { Text(section ?: "Ingredients", style = SimmrTextStyles.Title3, color = SimmrColors.TextDark) }
                items(ingredients, key = { "${section}-${it.name}" }) { ingredient ->
                    IngredientRow(ingredient, ingredient.name in checkedNames) {
                        checkedNames = checkedNames.toMutableSet().apply { if (!add(ingredient.name)) remove(ingredient.name) }
                    }
                }
            }
        }
        SimmrStickyFooter {
            SimmrPrimaryButton(onClick = { onStartCooking(scaled) }) { Text("Start Cooking", style = SimmrTextStyles.Headline) }
        }
    }
}

@Composable
private fun MetaPill(text: String) {
    Text(
        text,
        Modifier.clip(CircleShape).background(SimmrColors.Tint).padding(horizontal = SimmrSpacing.Sm, vertical = 6.dp),
        style = SimmrTextStyles.Caption,
        color = SimmrColors.TextDark,
    )
}

@Composable
private fun IngredientRow(ingredient: Ingredient, checked: Boolean, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(SimmrRadius.Md)).background(SimmrColors.CreamCard)
            .clickable(onClick = onClick).padding(SimmrSpacing.Sm),
        horizontalArrangement = Arrangement.spacedBy(SimmrSpacing.Sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier.size(24.dp),
            shape = CircleShape,
            color = if (checked) SimmrColors.Coral else Color.Transparent,
            border = if (checked) null else androidx.compose.foundation.BorderStroke(1.5.dp, SimmrColors.Border),
        ) { if (checked) Icon(Icons.Rounded.Check, null, tint = Color.White, modifier = Modifier.padding(3.dp)) }
        Column(Modifier.weight(1f)) {
            Text(
                ingredient.name + if (ingredient.optional) " (optional)" else "",
                style = SimmrTextStyles.Body,
                color = if (checked) SimmrColors.TextMuted else SimmrColors.TextDark,
                textDecoration = if (checked) TextDecoration.LineThrough else null,
            )
            ingredient.prep?.let { Text(it, style = SimmrTextStyles.Caption, color = SimmrColors.TextMuted) }
        }
        ingredient.quantityLabel?.let { Text(it, style = SimmrTextStyles.Headline, color = SimmrColors.Coral) }
    }
}

@Composable
fun TopBar(title: String, onBack: () -> Unit, action: (@Composable () -> Unit)? = null) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = SimmrSpacing.Xs, vertical = SimmrSpacing.Xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBackIosNew, "Back", tint = SimmrColors.TextDark) }
        Text(title, Modifier.weight(1f), style = SimmrTextStyles.Headline, color = SimmrColors.TextDark)
        if (action != null) action() else Box(Modifier.size(48.dp))
    }
}
