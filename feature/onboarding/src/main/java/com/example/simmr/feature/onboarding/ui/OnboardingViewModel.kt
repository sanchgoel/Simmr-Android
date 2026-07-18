package com.example.simmr.feature.onboarding.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.simmr.feature.onboarding.data.KitchenProfile
import com.example.simmr.feature.onboarding.data.KitchenProfileStore
import com.example.simmr.feature.onboarding.model.OnboardingAnswer
import com.example.simmr.feature.onboarding.model.OnboardingAnswers
import com.example.simmr.feature.onboarding.model.OnboardingQuestion
import com.example.simmr.feature.onboarding.model.OnboardingQuestionKind

internal sealed interface OnboardingScreen {
    data object Welcome : OnboardingScreen
    data class Question(val index: Int) : OnboardingScreen
    data object Final : OnboardingScreen
}

internal class OnboardingViewModel(
    val allQuestions: List<OnboardingQuestion>,
    private val store: KitchenProfileStore,
    val isEditing: Boolean,
) : ViewModel() {
    var answers: OnboardingAnswers by mutableStateOf(store.load()?.answers.orEmpty())
        private set

    var screen: OnboardingScreen by mutableStateOf(
        if (isEditing) {
            if (allQuestions.any { it.isVisible(answers) }) OnboardingScreen.Question(0)
            else OnboardingScreen.Final
        } else {
            OnboardingScreen.Welcome
        },
    )
        private set

    val visibleQuestions: List<OnboardingQuestion>
        get() = allQuestions.filter { it.isVisible(answers) }

    val showsProgressBar: Boolean get() = screen is OnboardingScreen.Question

    val progress: Float
        get() {
            val current = screen as? OnboardingScreen.Question ?: return 0f
            return (current.index + 1f) / visibleQuestions.size.coerceAtLeast(1)
        }

    fun beginQuestions() {
        screen = if (visibleQuestions.isEmpty()) OnboardingScreen.Final else OnboardingScreen.Question(0)
    }

    fun goNext() {
        val current = screen as? OnboardingScreen.Question ?: return
        val next = current.index + 1
        if (next < visibleQuestions.size) {
            screen = OnboardingScreen.Question(next)
        } else {
            persist(isComplete = true)
            screen = OnboardingScreen.Final
        }
    }

    fun goBack() {
        screen = when (val current = screen) {
            is OnboardingScreen.Question -> {
                if (current.index == 0) {
                    if (isEditing) current else OnboardingScreen.Welcome
                } else {
                    OnboardingScreen.Question(current.index - 1)
                }
            }
            OnboardingScreen.Final -> OnboardingScreen.Question((visibleQuestions.size - 1).coerceAtLeast(0))
            OnboardingScreen.Welcome -> OnboardingScreen.Welcome
        }
    }

    fun toggle(optionId: String, question: OnboardingQuestion) {
        val current = answers[question.id] ?: OnboardingAnswer()
        val selected = current.selectedOptionIds.toMutableList()

        when (val kind = question.kind) {
            OnboardingQuestionKind.SingleSelect -> {
                selected.clear()
                selected += optionId
            }
            is OnboardingQuestionKind.MultiSelect -> {
                if (optionId in selected) {
                    selected -= optionId
                } else {
                    val tappedIsExclusive = question.allOptions.find { it.id == optionId }?.isExclusive == true
                    if (tappedIsExclusive) {
                        selected.clear()
                        selected += optionId
                    } else {
                        selected.removeAll { selectedId ->
                            question.allOptions.find { it.id == selectedId }?.isExclusive == true
                        }
                        if (kind.maxSelections != null && selected.size >= kind.maxSelections) return
                        selected += optionId
                    }
                }
            }
            is OnboardingQuestionKind.Ranking -> {
                if (optionId in selected) selected -= optionId
                else if (selected.size < kind.count) selected += optionId
            }
        }

        updateAnswer(question.id, current.copy(selectedOptionIds = selected))
    }

    fun updateOtherText(text: String, question: OnboardingQuestion) {
        val current = answers[question.id] ?: OnboardingAnswer()
        updateAnswer(question.id, current.copy(otherText = text))
    }

    fun isSelected(optionId: String, question: OnboardingQuestion): Boolean =
        answers[question.id]?.selectedOptionIds?.contains(optionId) == true

    fun rank(optionId: String, question: OnboardingQuestion): Int? =
        answers[question.id]?.selectedOptionIds?.indexOf(optionId)
            ?.takeIf { it >= 0 }
            ?.plus(1)

    fun selectedCount(question: OnboardingQuestion): Int =
        answers[question.id]?.selectedOptionIds?.size ?: 0

    fun otherText(question: OnboardingQuestion): String = answers[question.id]?.otherText.orEmpty()

    fun canContinue(question: OnboardingQuestion): Boolean {
        val selectedCount = selectedCount(question)
        val hasOtherText = otherText(question).isNotBlank()
        return when (val kind = question.kind) {
            OnboardingQuestionKind.SingleSelect -> selectedCount == 1
            is OnboardingQuestionKind.MultiSelect -> selectedCount >= 1 || question.allowsOtherText && hasOtherText
            is OnboardingQuestionKind.Ranking -> selectedCount == kind.count
        }
    }

    private fun updateAnswer(questionId: String, answer: OnboardingAnswer) {
        answers = answers + (questionId to answer)
        persist(isComplete = isEditing)
    }

    private fun persist(isComplete: Boolean) {
        store.save(
            KitchenProfile(
                answers = answers,
                isComplete = isComplete,
                completedAt = if (isComplete) System.currentTimeMillis() else null,
            ),
        )
    }
}

internal class OnboardingViewModelFactory(
    private val questions: List<OnboardingQuestion>,
    private val store: KitchenProfileStore,
    private val isEditing: Boolean,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        OnboardingViewModel(questions, store, isEditing) as T
}
