@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.simmr.feature.home

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.simmr.core.data.OpenAIRecipeRepository
import com.example.simmr.core.data.RecipeRepository
import com.example.simmr.core.data.RecipePrompt
import com.example.simmr.core.data.SettingsStore
import com.example.simmr.core.designsystem.components.SimmrPrimaryButton
import com.example.simmr.core.designsystem.components.SimmrSecondaryButton
import com.example.simmr.core.designsystem.components.SimmrStickyFooter
import com.example.simmr.core.designsystem.theme.SimmrColors
import com.example.simmr.core.designsystem.theme.SimmrLayout
import com.example.simmr.core.designsystem.theme.SimmrRadius
import com.example.simmr.core.designsystem.theme.SimmrSpacing
import com.example.simmr.core.designsystem.theme.SimmrStroke
import com.example.simmr.core.designsystem.theme.SimmrTextStyles
import com.example.simmr.core.model.Recipe
import com.example.simmr.core.model.RecipeOptimization
import com.example.simmr.feature.onboarding.ui.OnboardingRoute

@Composable
fun HomeRoute(
    onRecipeGenerated: (Recipe) -> Unit,
    repository: RecipeRepository? = null,
) {
    val context = LocalContext.current
    val settings = remember(context) { SettingsStore(context) }
    val factory = remember(context, repository) {
        HomeViewModelFactory(repository ?: OpenAIRecipeRepository(context), settings)
    }
    val viewModel: HomeViewModel = viewModel(
        key = "simmr-home-${repository?.javaClass?.name ?: "default"}",
        factory = factory,
    )
    var showSettings by remember { mutableStateOf(false) }

    HomeScreen(
        state = viewModel.state,
        onInputChange = viewModel::updateInput,
        onToggleOptimization = viewModel::toggle,
        onGenerate = { viewModel.generate(onRecipeGenerated) },
        onSettings = { showSettings = true },
    )
    if (showSettings) {
        SettingsSheet(
            settings = settings,
            onApiKeyChanged = viewModel::refreshApiKey,
            onDismiss = {
                showSettings = false
                viewModel.refreshApiKey()
            },
        )
    }
}

@Composable
private fun HomeScreen(
    state: HomeUiState,
    onInputChange: (String) -> Unit,
    onToggleOptimization: (RecipeOptimization) -> Unit,
    onGenerate: () -> Unit,
    onSettings: () -> Unit,
) {
    Box(
        Modifier.fillMaxSize().background(SimmrColors.CreamBackground).safeDrawingPadding(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(Modifier.fillMaxSize()) {
            Column(
                Modifier.weight(1f).fillMaxWidth().widthIn(max = SimmrLayout.ContentMaxWidth)
                    .align(Alignment.CenterHorizontally).verticalScroll(rememberScrollState())
                    .padding(SimmrSpacing.Lg),
                verticalArrangement = Arrangement.spacedBy(SimmrSpacing.Lg),
            ) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(SimmrSpacing.Xxs)) {
                    Text(HomeCopy.TITLE, style = SimmrTextStyles.LargeTitle, color = SimmrColors.TextDark)
                    Text(HomeCopy.SUBTITLE, style = SimmrTextStyles.Subheadline, color = SimmrColors.TextMuted)
                }
                IconButton(onClick = onSettings) { Icon(Icons.Rounded.Settings, "Settings", tint = SimmrColors.TextDark) }
                }
                SimmrTextArea(value = state.input, onValueChange = onInputChange, placeholder = HomeCopy.PLACEHOLDER)
                Column(verticalArrangement = Arrangement.spacedBy(SimmrSpacing.Xs)) {
                    Text("Optimize this recipe", style = SimmrTextStyles.Headline, color = SimmrColors.TextDark)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(SimmrSpacing.Xs), verticalArrangement = Arrangement.spacedBy(SimmrSpacing.Xs)) {
                        RecipeOptimization.entries.forEach { option ->
                            val selected = option in state.optimizations
                            Surface(
                                onClick = { onToggleOptimization(option) },
                                shape = RoundedCornerShape(SimmrRadius.Pill),
                                color = if (selected) SimmrColors.Coral else SimmrColors.CreamCard,
                                contentColor = if (selected) androidx.compose.ui.graphics.Color.White else SimmrColors.TextDark,
                                border = if (selected) null else BorderStroke(SimmrStroke.Regular, SimmrColors.Border),
                            ) { Text(option.label, Modifier.padding(horizontal = SimmrSpacing.Sm, vertical = SimmrSpacing.Xs), style = SimmrTextStyles.Footnote) }
                        }
                    }
                }
                if (!state.hasApiKey) {
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(SimmrRadius.Md)).background(SimmrColors.Tint)
                            .clickable(role = Role.Button, onClick = onSettings).padding(SimmrSpacing.Sm),
                        horizontalArrangement = Arrangement.spacedBy(SimmrSpacing.Xs),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Rounded.Key, null, tint = SimmrColors.TextDark)
                        Text("Add your OpenAI API key to generate recipes", Modifier.weight(1f), style = SimmrTextStyles.Footnote)
                        Text("›", style = SimmrTextStyles.Title3)
                    }
                }
                state.error?.let { Text(it, style = SimmrTextStyles.Footnote, color = SimmrColors.Coral) }
            }
            SimmrStickyFooter {
                SimmrPrimaryButton(
                    onClick = onGenerate,
                    enabled = state.canGenerate,
                    modifier = Modifier.testTag("generate_recipe"),
                ) {
                    Text(if (state.isGenerating) "Generating…" else "Generate Recipe", style = SimmrTextStyles.Headline)
                }
            }
        }
        if (state.isGenerating) {
            RecipeGeneratingOverlay()
        }
    }
}

@Composable
internal fun SimmrTextArea(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    height: Int = 260,
    password: Boolean = false,
) {
    var focused by remember { mutableStateOf(false) }
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth().height(height.dp).onFocusChanged { focused = it.isFocused }
            .clip(RoundedCornerShape(SimmrRadius.Lg)).background(SimmrColors.CreamCard)
            .border(SimmrStroke.Regular, if (focused) SimmrColors.Coral else SimmrColors.Border, RoundedCornerShape(SimmrRadius.Lg))
            .padding(SimmrSpacing.Md),
        textStyle = SimmrTextStyles.Body.copy(color = SimmrColors.TextDark),
        cursorBrush = SolidColor(SimmrColors.Coral),
        visualTransformation = if (password) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        decorationBox = { inner -> Box { if (value.isEmpty()) Text(placeholder, style = SimmrTextStyles.Body, color = SimmrColors.TextMuted); inner() } },
    )
}

@Composable
private fun SettingsSheet(
    settings: SettingsStore,
    onApiKeyChanged: () -> Unit,
    onDismiss: () -> Unit,
) {
    var apiKey by remember { mutableStateOf(settings.apiKey().orEmpty()) }
    var isSaved by remember { mutableStateOf(settings.hasApiKey()) }
    var prompt by remember { mutableStateOf(settings.promptOverride() ?: RecipePrompt.DEFAULT) }
    var promptSaved by remember { mutableStateOf(false) }
    var editProfile by remember { mutableStateOf(false) }

    if (editProfile) {
        Box(Modifier.fillMaxSize().background(SimmrColors.CreamBackground)) {
            OnboardingRoute(onComplete = { editProfile = false }, isEditing = true)
        }
        return
    }
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = SimmrColors.CreamBackground) {
        Column(
            Modifier.fillMaxWidth().widthIn(max = 720.dp).align(Alignment.CenterHorizontally)
                .verticalScroll(rememberScrollState()).padding(SimmrSpacing.Lg),
            verticalArrangement = Arrangement.spacedBy(SimmrSpacing.Lg),
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Settings", Modifier.weight(1f), style = SimmrTextStyles.Title, color = SimmrColors.TextDark)
                IconButton(onClick = onDismiss) { Icon(Icons.Rounded.Close, "Done") }
            }
            SettingsHeader("OpenAI API Key", "Simmr uses your own OpenAI key to parse recipes. It's encrypted and stored only on this device.")
            SimmrTextArea(
                apiKey,
                { apiKey = it },
                "sk-...",
                modifier = Modifier.testTag("api_key_input"),
                height = 56,
                password = true,
            )
            if (isSaved) Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Rounded.CheckCircle, null, tint = SimmrColors.Amber); Text(" Key saved", style = SimmrTextStyles.Footnote) }
            SimmrPrimaryButton(
                onClick = {
                    settings.saveApiKey(apiKey)
                    isSaved = settings.hasApiKey()
                    onApiKeyChanged()
                },
                enabled = apiKey.isNotBlank(),
                modifier = Modifier.testTag("save_api_key"),
            ) { Text("Save") }
            if (isSaved) SimmrSecondaryButton(onClick = {
                settings.clearApiKey()
                apiKey = ""
                isSaved = false
                onApiKeyChanged()
            }) { Text("Remove Key", color = SimmrColors.Coral) }
            val context = LocalContext.current
            Text(
                "Get an API key from platform.openai.com",
                Modifier.clickable { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://platform.openai.com/api-keys"))) },
                style = SimmrTextStyles.Footnote,
                color = SimmrColors.Coral,
            )
            HorizontalDivider(color = SimmrColors.Border)
            SettingsHeader("Kitchen Profile", "Update your cooking habits, dietary needs, and taste preferences.")
            SimmrSecondaryButton(onClick = { editProfile = true }) { Text("Edit Kitchen Profile") }
            HorizontalDivider(color = SimmrColors.Border)
            SettingsHeader("Recipe Prompt", "For testing only — edit the system prompt sent to OpenAI.")
            SimmrTextArea(prompt, { prompt = it; promptSaved = false }, "System prompt", height = 260)
            if (promptSaved) Text("Prompt saved", style = SimmrTextStyles.Footnote, color = SimmrColors.Amber)
            SimmrPrimaryButton(onClick = { settings.savePromptOverride(prompt); promptSaved = true }) { Text("Save Prompt") }
            SimmrSecondaryButton(onClick = { settings.savePromptOverride(null); prompt = RecipePrompt.DEFAULT; promptSaved = true }) { Text("Reset to Default") }
            Spacer(Modifier.height(SimmrSpacing.Xl))
        }
    }
}

@Composable
private fun SettingsHeader(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(SimmrSpacing.Xxs)) {
        Text(title, style = SimmrTextStyles.Title3, color = SimmrColors.TextDark)
        Text(subtitle, style = SimmrTextStyles.Subheadline, color = SimmrColors.TextMuted)
    }
}
