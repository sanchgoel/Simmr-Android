package com.example.simmr.feature.onboarding.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.simmr.core.designsystem.components.SimmrPrimaryButton
import com.example.simmr.core.designsystem.components.SimmrStickyFooter
import com.example.simmr.core.designsystem.components.SimmrSelectionChip
import com.example.simmr.core.designsystem.theme.SimmrColors
import com.example.simmr.core.designsystem.theme.SimmrRadius
import com.example.simmr.core.designsystem.theme.SimmrSpacing
import com.example.simmr.core.designsystem.theme.SimmrStroke
import com.example.simmr.core.designsystem.theme.SimmrTextStyles
import com.example.simmr.core.designsystem.theme.SimmrTheme
import com.example.simmr.feature.onboarding.data.OnboardingQuestions
import com.example.simmr.feature.onboarding.data.SharedPreferencesKitchenProfileStore
import com.example.simmr.feature.onboarding.model.OnboardingOption
import com.example.simmr.feature.onboarding.model.OnboardingQuestion
import com.example.simmr.feature.onboarding.model.OnboardingQuestionKind

@Composable
fun OnboardingRoute(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
    isEditing: Boolean = false,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val factory = remember(context, isEditing) {
        OnboardingViewModelFactory(
            questions = OnboardingQuestions.all,
            store = SharedPreferencesKitchenProfileStore(context),
            isEditing = isEditing,
        )
    }
    val viewModel: OnboardingViewModel = viewModel(
        key = "simmr-onboarding-$isEditing",
        factory = factory,
    )

    OnboardingContent(
        viewModel = viewModel,
        onComplete = onComplete,
        modifier = modifier,
    )
}

@Composable
private fun OnboardingContent(
    viewModel: OnboardingViewModel,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SimmrColors.CreamBackground)
            .safeDrawingPadding(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
                .widthIn(max = 680.dp)
                .imePadding(),
        ) {
            OnboardingHeader(
                showsProgress = viewModel.showsProgressBar,
                progress = viewModel.progress,
                isEditing = viewModel.isEditing,
                onBack = {
                    val screen = viewModel.screen
                    if (viewModel.isEditing && screen is OnboardingScreen.Question && screen.index == 0) {
                        onComplete()
                    } else {
                        viewModel.goBack()
                    }
                },
                onClose = onComplete,
            )

            AnimatedContent(
                targetState = viewModel.screen,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                transitionSpec = {
                    fadeIn(tween(200)) togetherWith fadeOut(tween(200))
                },
                label = "onboarding-page",
            ) { screen ->
                Box(Modifier.fillMaxSize()) {
                    when (screen) {
                        OnboardingScreen.Welcome -> WelcomePage(onStart = viewModel::beginQuestions)
                        is OnboardingScreen.Question -> {
                            viewModel.visibleQuestions.getOrNull(screen.index)?.let { question ->
                                QuestionPage(
                                    question = question,
                                    viewModel = viewModel,
                                    onContinue = viewModel::goNext,
                                )
                            }
                        }
                        OnboardingScreen.Final -> FinalPage(onFinish = onComplete)
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingHeader(
    showsProgress: Boolean,
    progress: Float,
    isEditing: Boolean,
    onBack: () -> Unit,
    onClose: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SimmrSpacing.Lg, vertical = SimmrSpacing.Sm),
        verticalArrangement = Arrangement.spacedBy(SimmrSpacing.Sm),
    ) {
        Box(Modifier.fillMaxWidth().height(32.dp), contentAlignment = Alignment.Center) {
            Text("Simmr", style = SimmrTextStyles.Headline, color = SimmrColors.TextDark)

            if (showsProgress) {
                HeaderIconButton(
                    onClick = onBack,
                    modifier = Modifier.align(Alignment.CenterStart),
                ) {
                    Icon(
                        Icons.Rounded.ArrowBackIosNew,
                        contentDescription = "Back",
                        modifier = Modifier.size(18.dp),
                    )
                }
            }

            if (isEditing) {
                HeaderIconButton(
                    onClick = onClose,
                    modifier = Modifier.align(Alignment.CenterEnd),
                ) {
                    Icon(Icons.Rounded.Close, contentDescription = "Close", modifier = Modifier.size(18.dp))
                }
            }
        }

        if (showsProgress) {
            val animatedProgress by animateFloatAsState(
                targetValue = progress,
                animationSpec = tween(250),
                label = "onboarding-progress",
            )
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(SimmrColors.Border),
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(animatedProgress)
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(SimmrColors.Coral),
                )
            }
        }
    }
}

@Composable
private fun HeaderIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.size(32.dp),
        shape = CircleShape,
        color = SimmrColors.CreamCard,
        contentColor = SimmrColors.TextDark,
        content = content,
    )
}

@Composable
private fun WelcomePage(onStart: () -> Unit) {
    ResponsiveBookendPage(
        content = {
            Column(verticalArrangement = Arrangement.spacedBy(SimmrSpacing.Sm)) {
                Text(
                    "Let's build your Kitchen Profile",
                    style = SimmrTextStyles.LargeTitle,
                    color = SimmrColors.TextDark,
                )
                Text(
                    "Answer a few quick questions so we can personalize recipes, recommendations and your AI cooking companion. You can update these anytime.",
                    style = SimmrTextStyles.Body,
                    color = SimmrColors.TextMuted,
                )
            }
        },
        action = {
            SimmrPrimaryButton(onClick = onStart) {
                Text("Let's Go", style = SimmrTextStyles.Button)
            }
        },
    )
}

@Composable
private fun FinalPage(onFinish: () -> Unit) {
    ResponsiveBookendPage(
        content = {
            Column(verticalArrangement = Arrangement.spacedBy(SimmrSpacing.Sm)) {
                Text("🎉", fontSize = 48.sp)
                Text(
                    "Your Kitchen Profile is ready!",
                    style = SimmrTextStyles.LargeTitle,
                    color = SimmrColors.TextDark,
                )
                Text(
                    "We'll personalize recipes, cooking guidance and recommendations based on your preferences. You can update these anytime from Settings.",
                    style = SimmrTextStyles.Body,
                    color = SimmrColors.TextMuted,
                )
            }
        },
        action = {
            SimmrPrimaryButton(onClick = onFinish) {
                Text("Start Cooking", style = SimmrTextStyles.Button)
            }
        },
    )
}

@Composable
private fun ResponsiveBookendPage(
    content: @Composable () -> Unit,
    action: @Composable () -> Unit,
) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        if (maxHeight < 360.dp) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(SimmrSpacing.Lg),
                verticalArrangement = Arrangement.spacedBy(SimmrSpacing.Xl),
            ) {
                content()
                action()
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(SimmrSpacing.Lg),
                horizontalAlignment = Alignment.Start,
            ) {
                Spacer(Modifier.weight(1f))
                content()
                Spacer(Modifier.weight(2f))
                action()
            }
        }
    }
}

@Composable
private fun QuestionPage(
    question: OnboardingQuestion,
    viewModel: OnboardingViewModel,
    onContinue: () -> Unit,
) {
    val canContinue = viewModel.canContinue(question)
    Column(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(SimmrSpacing.Lg),
            verticalArrangement = Arrangement.spacedBy(SimmrSpacing.Lg),
        ) {
            item(key = "header-${question.id}") {
                QuestionHeader(question, viewModel.selectedCount(question))
            }

            item(key = "options-${question.id}") {
                if (question.kind is OnboardingQuestionKind.Ranking) {
                    Column(verticalArrangement = Arrangement.spacedBy(SimmrSpacing.Xs)) {
                        question.allOptions.forEach { option ->
                            val rank = viewModel.rank(option.id, question)
                            RankingOptionRow(
                                option = option,
                                rank = rank,
                                enabled = rank != null || viewModel.selectedCount(question) < question.kind.count,
                                onClick = { viewModel.toggle(option.id, question) },
                            )
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(SimmrSpacing.Md)) {
                        question.groups.forEach { group ->
                            Column(verticalArrangement = Arrangement.spacedBy(SimmrSpacing.Xs)) {
                                group.title?.let { title ->
                                    Text(title, style = SimmrTextStyles.Headline, color = SimmrColors.TextDark)
                                }
                                group.options.forEach { option ->
                                    val isSelected = viewModel.isSelected(option.id, question)
                                    val max = (question.kind as? OnboardingQuestionKind.MultiSelect)?.maxSelections
                                    OptionRow(
                                        option = option,
                                        selected = isSelected,
                                        enabled = isSelected || max == null || viewModel.selectedCount(question) < max,
                                        singleSelect = question.kind is OnboardingQuestionKind.SingleSelect,
                                        onClick = { viewModel.toggle(option.id, question) },
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (question.allowsOtherText) {
                item(key = "other-${question.id}") {
                    OtherTextField(
                        value = viewModel.otherText(question),
                        onValueChange = { viewModel.updateOtherText(it, question) },
                    )
                }
            }
        }

        SimmrStickyFooter(showDivider = false) {
            SimmrPrimaryButton(onClick = onContinue, enabled = canContinue) {
                Text("Continue", style = SimmrTextStyles.Button)
            }
        }
    }
}

@Composable
private fun QuestionHeader(question: OnboardingQuestion, selectedCount: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(SimmrSpacing.Xxs)) {
        Text(
            question.sectionTitle.uppercase(),
            style = SimmrTextStyles.Caption,
            color = SimmrColors.Coral,
        )
        Text(question.text, style = SimmrTextStyles.Title, color = SimmrColors.TextDark)
        question.subtitle?.let {
            Text(it, style = SimmrTextStyles.Subheadline, color = SimmrColors.TextMuted)
        }
        when (val kind = question.kind) {
            is OnboardingQuestionKind.Ranking -> HintText("$selectedCount of ${kind.count} ranked")
            is OnboardingQuestionKind.MultiSelect -> HintText(
                kind.maxSelections?.let { "Choose up to $it" } ?: "Select all that apply",
            )
            OnboardingQuestionKind.SingleSelect -> Unit
        }
    }
}

@Composable
private fun HintText(text: String) {
    Text(
        text,
        modifier = Modifier.padding(top = SimmrSpacing.Xxs),
        style = SimmrTextStyles.Footnote,
        color = SimmrColors.TextMuted,
    )
}

@Composable
private fun OptionRow(
    option: OnboardingOption,
    selected: Boolean,
    enabled: Boolean,
    singleSelect: Boolean,
    onClick: () -> Unit,
) {
    SimmrSelectionChip(
        selected = selected,
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled,
        role = if (singleSelect) Role.RadioButton else Role.Checkbox,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().heightIn(min = 32.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SimmrSpacing.Sm),
        ) {
            Text(
                option.label,
                modifier = Modifier.weight(1f),
                style = SimmrTextStyles.Body,
                color = SimmrColors.TextDark,
            )
            if (!singleSelect || selected) {
                Icon(
                    imageVector = if (selected) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = if (selected) SimmrColors.Coral else SimmrColors.Border,
                )
            }
        }
    }
}

@Composable
private fun RankingOptionRow(
    option: OnboardingOption,
    rank: Int?,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    SimmrSelectionChip(
        selected = rank != null,
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled,
        role = Role.Checkbox,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().heightIn(min = 32.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SimmrSpacing.Sm),
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .background(if (rank != null) SimmrColors.Coral else SimmrColors.CreamBackground, CircleShape)
                    .then(
                        if (rank == null) Modifier.border(SimmrStroke.Regular, SimmrColors.Border, CircleShape)
                        else Modifier,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                rank?.let {
                    Text(
                        it.toString(),
                        style = SimmrTextStyles.Footnote.copy(fontWeight = FontWeight.Bold),
                        color = androidx.compose.ui.graphics.Color.White,
                    )
                }
            }
            Text(option.label, style = SimmrTextStyles.Body, color = SimmrColors.TextDark)
        }
    }
}

@Composable
private fun OtherTextField(value: String, onValueChange: (String) -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .background(SimmrColors.CreamCard, RoundedCornerShape(SimmrRadius.Md))
            .border(
                SimmrStroke.Regular,
                if (focused) SimmrColors.Coral else SimmrColors.Border,
                RoundedCornerShape(SimmrRadius.Md),
            )
            .padding(SimmrSpacing.Sm),
        textStyle = SimmrTextStyles.Body.copy(color = SimmrColors.TextDark),
        singleLine = true,
        cursorBrush = SolidColor(SimmrColors.Coral),
        interactionSource = interactionSource,
        decorationBox = { innerTextField ->
            Box(contentAlignment = Alignment.CenterStart) {
                if (value.isEmpty()) {
                    Text("Other (optional)", style = SimmrTextStyles.Body, color = SimmrColors.TextMuted)
                }
                innerTextField()
            }
        },
    )
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun WelcomePreview() {
    SimmrTheme { WelcomePage {} }
}
