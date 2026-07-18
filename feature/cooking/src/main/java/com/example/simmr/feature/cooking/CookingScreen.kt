@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.simmr.feature.cooking

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.SwapCalls
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.simmr.core.designsystem.components.SimmrPrimaryButton
import com.example.simmr.core.designsystem.components.SimmrSecondaryButton
import com.example.simmr.core.designsystem.components.SimmrStickyFooter
import com.example.simmr.core.designsystem.theme.SimmrColors
import com.example.simmr.core.designsystem.theme.SimmrLayout
import com.example.simmr.core.designsystem.theme.SimmrRadius
import com.example.simmr.core.designsystem.theme.SimmrSpacing
import com.example.simmr.core.designsystem.theme.SimmrStroke
import com.example.simmr.core.designsystem.theme.SimmrTextStyles
import com.example.simmr.core.model.AppConstants
import com.example.simmr.core.model.ConversionCategory
import com.example.simmr.core.model.ConvertibleIngredient
import com.example.simmr.core.model.Ingredient
import com.example.simmr.core.model.IngredientDensity
import com.example.simmr.core.model.QuantityFormatter
import com.example.simmr.core.model.Recipe
import com.example.simmr.core.model.VolumeUnit
import com.example.simmr.core.model.WeightUnit
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun CookingScreen(recipe: Recipe, onBack: () -> Unit, onFinish: () -> Unit) {
    var stepIndex by remember(recipe) { mutableIntStateOf(0) }
    val step = recipe.steps[stepIndex]
    val timers = remember(recipe) { recipe.steps.associate { it.stepNumber to CookingTimer(it.timerSeconds ?: 0) } }
    val timer = timers.getValue(step.stepNumber)
    var remaining by remember(stepIndex) { mutableIntStateOf(timer.remaining()) }
    var timerRevision by remember(recipe) { mutableIntStateOf(0) }
    var converterOptions by remember { mutableStateOf<List<ConvertibleIngredient>?>(null) }
    val used = remember(step, recipe) { resolveIngredients(step.ingredientsUsed, recipe.ingredients) }
    val convertible = remember(used) { used.mapNotNull(::convertibleIngredient) }

    fun refreshTimer() {
        remaining = timer.remaining()
        timerRevision++
    }

    LaunchedEffect(timer, timerRevision) {
        while (true) {
            remaining = timer.remaining()
            if (!timer.isRunning || remaining == 0) break
            delay(AppConstants.Timer.TICK_INTERVAL_MS)
        }
    }

    Column(Modifier.fillMaxSize().background(SimmrColors.CreamBackground).safeDrawingPadding()) {
        CookingTopBar("Cooking", onBack) {
            IconButton(onClick = { converterOptions = convertible }) { Icon(Icons.Rounded.SwapCalls, "Unit converter") }
        }
        Column(
            Modifier.fillMaxWidth().widthIn(max = 720.dp).align(Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(SimmrSpacing.Xs),
        ) {
            LinearProgressIndicator(
                progress = { (stepIndex + 1).toFloat() / recipe.steps.size },
                Modifier.fillMaxWidth(), color = SimmrColors.Coral, trackColor = SimmrColors.Border,
                drawStopIndicator = {},
            )
            Text("Step ${stepIndex + 1} of ${recipe.steps.size}", Modifier.padding(horizontal = SimmrSpacing.Lg), style = SimmrTextStyles.Caption, color = SimmrColors.TextMuted)
        }
        Column(
            Modifier.weight(1f).fillMaxWidth().widthIn(max = 720.dp).align(Alignment.CenterHorizontally)
                .verticalScroll(rememberScrollState()).padding(SimmrSpacing.Lg),
            verticalArrangement = Arrangement.spacedBy(SimmrSpacing.Lg),
        ) {
            Text(step.title, style = SimmrTextStyles.CookingStepTitle, color = SimmrColors.TextDark)
            Text(step.instruction, style = SimmrTextStyles.CookingInstruction, color = SimmrColors.TextDark.copy(alpha = .85f))
            if (used.isNotEmpty()) FlowRow(horizontalArrangement = Arrangement.spacedBy(SimmrSpacing.Xs), verticalArrangement = Arrangement.spacedBy(SimmrSpacing.Xs)) {
                used.forEach { ingredient ->
                    val option = convertibleIngredient(ingredient)
                    Surface(
                        onClick = { if (option != null) converterOptions = listOf(option) },
                        enabled = option != null,
                        shape = CircleShape,
                        color = SimmrColors.Tint,
                    ) {
                        Row(
                            Modifier.padding(horizontal = SimmrSpacing.Sm, vertical = SimmrSpacing.Xs),
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(ingredient.name, style = SimmrTextStyles.Footnote)
                            ingredient.quantityLabel?.let { Text(it, style = SimmrTextStyles.Headline, color = SimmrColors.Coral) }
                            if (option != null) Icon(Icons.Rounded.SwapCalls, null, Modifier.size(10.dp), tint = SimmrColors.Coral.copy(alpha = .7f))
                        }
                    }
                }
            }
            step.tips?.let { Callout("💡", it) }
            listOfNotNull(step.cookware?.let { "Cookware: $it" }, step.heatLevel?.let { "Heat: $it" }, step.lid?.let { "Lid: $it" }, step.visualCue).forEach { Callout("•", it) }
            if (step.hasTimer) TimerControl(timer, remaining, ::refreshTimer)
        }
        SimmrStickyFooter(showDivider = false) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SimmrSpacing.Sm),
            ) {
                SimmrSecondaryButton(
                    onClick = { stepIndex--; remaining = timers.getValue(recipe.steps[stepIndex].stepNumber).remaining() },
                    enabled = stepIndex > 0,
                    modifier = Modifier.weight(1f),
                ) { Text("Previous") }
                SimmrPrimaryButton(
                    onClick = {
                        if (stepIndex == recipe.steps.lastIndex) onFinish()
                        else { stepIndex++; remaining = timers.getValue(recipe.steps[stepIndex].stepNumber).remaining() }
                    },
                    modifier = Modifier.weight(1f),
                ) { Text(if (stepIndex == recipe.steps.lastIndex) "Finish" else "Next") }
            }
        }
    }
    converterOptions?.let { UnitConverterSheet(it, onDismiss = { converterOptions = null }) }
}

private fun resolveIngredients(names: List<String>, ingredients: List<Ingredient>): List<Ingredient> = names.mapNotNull { name ->
    ingredients.firstOrNull { it.name.equals(name, ignoreCase = true) }
        ?: ingredients.firstOrNull { it.name.contains(name, ignoreCase = true) || name.contains(it.name, ignoreCase = true) }
}.distinctBy { it.name }

private fun convertibleIngredient(ingredient: Ingredient): ConvertibleIngredient? {
    val quantity = ingredient.quantity ?: return null
    val unit = ingredient.unit ?: return null
    VolumeUnit.matching(unit)?.let { return ConvertibleIngredient(ingredient.name, ConversionCategory.VOLUME, quantity, volumeUnit = it) }
    WeightUnit.matching(unit)?.let { return ConvertibleIngredient(ingredient.name, ConversionCategory.WEIGHT, quantity, weightUnit = it) }
    return null
}

@Composable
private fun Callout(icon: String, text: String) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(SimmrRadius.Sm)).background(SimmrColors.CreamCard).padding(SimmrSpacing.Sm)) {
        Text("$icon  ", color = SimmrColors.Amber)
        Text(text, style = SimmrTextStyles.Footnote, color = SimmrColors.TextMuted)
    }
}

@Composable
private fun TimerControl(timer: CookingTimer, remaining: Int, refresh: () -> Unit) {
    val minutes = remaining / AppConstants.Timer.SECONDS_PER_MINUTE
    val seconds = remaining % AppConstants.Timer.SECONDS_PER_MINUTE
    val isComplete = remaining == 0
    val progress = if (timer.totalSeconds == 0) 0f else {
        1f - remaining.toFloat() / timer.totalSeconds
    }.coerceIn(0f, 1f)
    val ringColor = if (isComplete) SimmrColors.Amber else SimmrColors.Coral

    Column(
        Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SimmrSpacing.Md),
    ) {
        Box(Modifier.size(160.dp), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize()) {
                val stroke = 8.dp.toPx()
                drawCircle(SimmrColors.Border, style = Stroke(width = stroke))
                if (progress > 0f) {
                    drawArc(
                        color = ringColor,
                        startAngle = -90f,
                        sweepAngle = progress * 360f,
                        useCenter = false,
                        style = Stroke(width = stroke, cap = StrokeCap.Round),
                    )
                }
            }
            if (isComplete) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(SimmrSpacing.Xxs)) {
                    Icon(Icons.Rounded.CheckCircle, null, Modifier.size(32.dp), tint = SimmrColors.Amber)
                    Text("Done", style = SimmrTextStyles.Headline, color = SimmrColors.TextDark)
                }
            } else {
                Text(
                    String.format(Locale.US, "%d:%02d", minutes, seconds),
                    style = SimmrTextStyles.TimerDisplay,
                    color = SimmrColors.TextDark,
                )
            }
        }

        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Timer", style = SimmrTextStyles.Headline, color = SimmrColors.TextDark)
            Spacer(Modifier.weight(1f))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(SimmrSpacing.Sm),
            ) {
                TimerMinuteButton(
                    icon = { Icon(Icons.Rounded.Remove, "Subtract minute", Modifier.size(18.dp)) },
                    enabled = timer.canSubtractMinute,
                    onClick = { timer.subtractMinute(); refresh() },
                )
                Text(
                    "${(timer.totalSeconds / AppConstants.Timer.SECONDS_PER_MINUTE.toDouble()).roundToInt()} min",
                    modifier = Modifier.widthIn(min = 64.dp),
                    style = SimmrTextStyles.Title3,
                    color = SimmrColors.TextDark,
                )
                TimerMinuteButton(
                    icon = { Icon(Icons.Rounded.Add, "Add minute", Modifier.size(18.dp)) },
                    onClick = { timer.addMinute(); refresh() },
                )
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(SimmrSpacing.Sm)) {
            SimmrPrimaryButton(
                onClick = { if (timer.isRunning) timer.pause() else timer.start(); refresh() },
                enabled = !isComplete,
                modifier = Modifier.weight(1f),
            ) { Text(if (timer.isRunning) "Pause" else "Start", style = SimmrTextStyles.Button) }
            SimmrSecondaryButton(
                onClick = { timer.reset(); refresh() },
                modifier = Modifier.weight(1f),
            ) { Text("Reset", style = SimmrTextStyles.Button) }
        }
    }
}

@Composable
private fun TimerMinuteButton(
    icon: @Composable () -> Unit,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.size(32.dp),
        shape = CircleShape,
        color = SimmrColors.CreamCard,
        contentColor = if (enabled) SimmrColors.TextDark else SimmrColors.TextMuted,
        border = BorderStroke(SimmrStroke.Hairline, SimmrColors.Border),
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { icon() }
    }
}

@Composable
private fun UnitConverterSheet(options: List<ConvertibleIngredient>, onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    fun dismissSheet() {
        scope.launch {
            sheetState.hide()
            onDismiss()
        }
    }
    var selected by remember(options) { mutableStateOf(options.firstOrNull()) }
    var category by remember(selected) { mutableStateOf(selected?.category ?: ConversionCategory.VOLUME) }
    var input by remember(selected) { mutableStateOf(selected?.quantity?.let(::formatDecimal).orEmpty()) }
    var volumeUnit by remember(selected) { mutableStateOf(selected?.volumeUnit ?: VolumeUnit.MILLILITER) }
    var weightUnit by remember(selected) { mutableStateOf(selected?.weightUnit ?: WeightUnit.GRAM) }
    val value = input.toDoubleOrNull() ?: 0.0
    val name = selected?.name
    val ml = when (category) {
        ConversionCategory.VOLUME -> volumeUnit.convert(value, VolumeUnit.MILLILITER)
        ConversionCategory.WEIGHT -> IngredientDensity.millilitersFromGrams(weightUnit.convert(value, WeightUnit.GRAM), name.orEmpty())
    }
    val grams = when (category) {
        ConversionCategory.WEIGHT -> weightUnit.convert(value, WeightUnit.GRAM)
        ConversionCategory.VOLUME -> IngredientDensity.gramsFromMilliliters(volumeUnit.convert(value, VolumeUnit.MILLILITER), name.orEmpty())
    }

    ModalBottomSheet(
        onDismissRequest = ::dismissSheet,
        sheetState = sheetState,
        containerColor = SimmrColors.CreamBackground,
    ) {
        Column(Modifier.fillMaxWidth()) {
            Box(
                Modifier.fillMaxWidth().padding(horizontal = SimmrSpacing.Sm),
                contentAlignment = Alignment.Center,
            ) {
                Text("Unit Converter", style = SimmrTextStyles.Headline, color = SimmrColors.TextDark)
                TextButton(onClick = ::dismissSheet, modifier = Modifier.align(Alignment.CenterEnd).testTag("converter_done")) {
                    Text("Done", style = SimmrTextStyles.Button, color = SimmrColors.Coral)
                }
            }
            HorizontalDivider(color = SimmrColors.Border)
            Column(
                Modifier.fillMaxWidth().widthIn(max = SimmrLayout.ContentMaxWidth)
                    .align(Alignment.CenterHorizontally).verticalScroll(rememberScrollState())
                    .padding(SimmrSpacing.Lg),
                verticalArrangement = Arrangement.spacedBy(SimmrSpacing.Lg),
            ) {
                if (options.isEmpty()) {
                    CategoryPicker(category, onSelect = { category = it })
                }
                if (name != null) {
                    Text(
                        "No scale? Here's the easiest way to measure $name.",
                        style = SimmrTextStyles.Footnote,
                        color = SimmrColors.TextMuted,
                    )
                }
                if (options.size > 1 && selected != null) {
                    ConverterDropdown(
                        items = options,
                        selected = selected!!,
                        label = { it.name },
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = SimmrColors.Tint,
                        fillWidth = true,
                        onSelect = { option ->
                            selected = option
                            category = option.category
                            input = formatDecimal(option.quantity)
                            option.volumeUnit?.let { volumeUnit = it }
                            option.weightUnit?.let { weightUnit = it }
                        },
                    )
                }
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(SimmrSpacing.Sm),
                ) {
                    ConverterValueField(
                        value = input,
                        onValueChange = { input = it },
                        modifier = Modifier.weight(1f),
                    )
                    when (category) {
                        ConversionCategory.VOLUME -> ConverterDropdown(
                            items = VolumeUnit.entries,
                            selected = volumeUnit,
                            label = { it.symbol },
                            onSelect = {
                                input = formatDecimal(volumeUnit.convert(value, it))
                                volumeUnit = it
                            },
                        )
                        ConversionCategory.WEIGHT -> ConverterDropdown(
                            items = WeightUnit.entries,
                            selected = weightUnit,
                            label = { it.symbol },
                            onSelect = {
                                input = formatDecimal(weightUnit.convert(value, it))
                                weightUnit = it
                            },
                        )
                    }
                }
                HorizontalDivider(color = SimmrColors.Border)
                if (name != null) {
                    Column(verticalArrangement = Arrangement.spacedBy(SimmrSpacing.Sm)) {
                        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(SimmrRadius.Lg)).background(SimmrColors.Tint).padding(SimmrSpacing.Md)) {
                            Text("≈ ${smartVolumeLabel(ml)}", style = SimmrTextStyles.LargeTitle, color = SimmrColors.Coral)
                            Text("using a standard cup, tablespoon, or teaspoon", style = SimmrTextStyles.Footnote, color = SimmrColors.TextMuted)
                        }
                        ResultRow("Weight", "${formatDecimal(grams)} g")
                        ResultRow("Volume", "${formatDecimal(ml)} ml")
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(SimmrSpacing.Xs)) {
                        when (category) {
                            ConversionCategory.VOLUME -> VolumeUnit.entries.filter { it != volumeUnit }.forEach { ResultRow(it.symbol, formatDecimal(volumeUnit.convert(value, it))) }
                            ConversionCategory.WEIGHT -> WeightUnit.entries.filter { it != weightUnit }.forEach { ResultRow(it.symbol, formatDecimal(weightUnit.convert(value, it))) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryPicker(selected: ConversionCategory, onSelect: (ConversionCategory) -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(SimmrRadius.Sm)).background(SimmrColors.Border).padding(2.dp),
    ) {
        ConversionCategory.entries.forEach { category ->
            Surface(
                onClick = { onSelect(category) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(6.dp),
                color = if (selected == category) SimmrColors.CreamCard else Color.Transparent,
            ) {
                Text(category.label, Modifier.padding(SimmrSpacing.Xs), style = SimmrTextStyles.Footnote, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
private fun ConverterValueField(value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
    var focused by remember { mutableStateOf(false) }
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.onFocusChanged { focused = it.isFocused }
            .clip(RoundedCornerShape(SimmrRadius.Md)).background(SimmrColors.CreamCard)
            .border(SimmrStroke.Regular, if (focused) SimmrColors.Coral else SimmrColors.Border, RoundedCornerShape(SimmrRadius.Md))
            .padding(SimmrSpacing.Sm),
        singleLine = true,
        textStyle = SimmrTextStyles.Title2.copy(color = SimmrColors.TextDark),
        cursorBrush = SolidColor(SimmrColors.Coral),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        decorationBox = { inner ->
            Box {
                if (value.isEmpty()) Text("Value", style = SimmrTextStyles.Title2, color = SimmrColors.TextMuted)
                inner()
            }
        },
    )
}

@Composable
private fun <T> ConverterDropdown(
    items: List<T>,
    selected: T,
    label: (T) -> String,
    modifier: Modifier = Modifier,
    containerColor: Color = Color.Transparent,
    fillWidth: Boolean = false,
    onSelect: (T) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier) {
        Surface(
            onClick = { expanded = true },
            modifier = if (fillWidth) Modifier.fillMaxWidth() else Modifier,
            shape = RoundedCornerShape(SimmrRadius.Md),
            color = containerColor,
        ) {
            Row(
                (if (fillWidth) Modifier.fillMaxWidth() else Modifier)
                    .padding(horizontal = SimmrSpacing.Sm, vertical = SimmrSpacing.Xs),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(SimmrSpacing.Xxs),
            ) {
                Text(label(selected), style = SimmrTextStyles.Headline, color = SimmrColors.TextDark)
                if (fillWidth) Spacer(Modifier.weight(1f))
                Icon(Icons.Rounded.KeyboardArrowDown, null, Modifier.size(18.dp), tint = SimmrColors.Coral)
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(label(item), style = SimmrTextStyles.Body) },
                    onClick = { expanded = false; onSelect(item) },
                )
            }
        }
    }
}

@Composable
private fun ResultRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(SimmrRadius.Sm)).background(SimmrColors.CreamCard).padding(SimmrSpacing.Sm)) {
        Text(label, Modifier.weight(1f), style = SimmrTextStyles.Body, color = SimmrColors.TextMuted)
        Text(value, style = SimmrTextStyles.Title3, color = SimmrColors.TextDark)
    }
}

private fun smartVolumeLabel(ml: Double): String {
    if (ml <= 0) return "0"
    val cups = VolumeUnit.MILLILITER.convert(ml, VolumeUnit.CUP)
    if (cups >= .25) return "${QuantityFormatter.format(cups)} ${if (cups > 1.001) "cups" else "cup"}"
    val tablespoons = VolumeUnit.MILLILITER.convert(ml, VolumeUnit.TABLESPOON)
    if (tablespoons >= 1) return "${QuantityFormatter.format(tablespoons)} tbsp"
    return "${QuantityFormatter.format(VolumeUnit.MILLILITER.convert(ml, VolumeUnit.TEASPOON))} tsp"
}

private fun formatDecimal(value: Double): String = if (value == value.toInt().toDouble()) value.toInt().toString() else String.format(Locale.US, "%.2f", value)

@Composable
private fun CookingTopBar(title: String, onBack: () -> Unit, action: @Composable () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = SimmrSpacing.Xs, vertical = SimmrSpacing.Xs), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBackIosNew, "Back", tint = SimmrColors.TextDark) }
        Text(title, Modifier.weight(1f), style = SimmrTextStyles.Headline, color = SimmrColors.TextDark)
        action()
    }
}
