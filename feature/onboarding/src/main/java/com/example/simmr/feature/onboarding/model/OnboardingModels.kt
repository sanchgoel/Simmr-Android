package com.example.simmr.feature.onboarding.model

data class OnboardingAnswer(
    val selectedOptionIds: List<String> = emptyList(),
    val otherText: String = "",
)

typealias OnboardingAnswers = Map<String, OnboardingAnswer>

data class OnboardingOption(
    val id: String,
    val label: String,
    val isExclusive: Boolean = false,
)

data class OnboardingOptionGroup(
    val id: String,
    val title: String? = null,
    val options: List<OnboardingOption>,
)

sealed interface OnboardingQuestionKind {
    data object SingleSelect : OnboardingQuestionKind
    data class MultiSelect(val maxSelections: Int? = null) : OnboardingQuestionKind
    data class Ranking(val count: Int) : OnboardingQuestionKind
}

data class OnboardingQuestion(
    val id: String,
    val sectionTitle: String,
    val text: String,
    val subtitle: String? = null,
    val kind: OnboardingQuestionKind,
    val groups: List<OnboardingOptionGroup>,
    val allowsOtherText: Boolean = false,
    val isVisible: (OnboardingAnswers) -> Boolean = { true },
) {
    val allOptions: List<OnboardingOption> get() = groups.flatMap { it.options }
}

fun question(
    id: String,
    sectionTitle: String,
    text: String,
    kind: OnboardingQuestionKind,
    options: List<OnboardingOption>,
    subtitle: String? = null,
    allowsOtherText: Boolean = false,
    isVisible: (OnboardingAnswers) -> Boolean = { true },
) = OnboardingQuestion(
    id = id,
    sectionTitle = sectionTitle,
    text = text,
    subtitle = subtitle,
    kind = kind,
    groups = listOf(OnboardingOptionGroup(id = id, options = options)),
    allowsOtherText = allowsOtherText,
    isVisible = isVisible,
)
