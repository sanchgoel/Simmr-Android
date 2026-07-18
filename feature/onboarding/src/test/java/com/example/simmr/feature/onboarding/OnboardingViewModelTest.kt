package com.example.simmr.feature.onboarding

import com.example.simmr.feature.onboarding.data.KitchenProfile
import com.example.simmr.feature.onboarding.data.KitchenProfileStore
import com.example.simmr.feature.onboarding.data.OnboardingQuestions
import com.example.simmr.feature.onboarding.model.OnboardingOption
import com.example.simmr.feature.onboarding.model.OnboardingQuestionKind.SingleSelect
import com.example.simmr.feature.onboarding.model.question
import com.example.simmr.feature.onboarding.ui.OnboardingScreen
import com.example.simmr.feature.onboarding.ui.OnboardingViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OnboardingViewModelTest {
    @Test
    fun questionSetMatchesIosAndConditionalQuestionsReactToAnswers() {
        val viewModel = viewModel()

        assertEquals(18, OnboardingQuestions.all.size)
        assertEquals(15, viewModel.visibleQuestions.size)

        val restrictions = OnboardingQuestions.all.first { it.id == "restrictions" }
        viewModel.toggle("has_allergies", restrictions)
        viewModel.toggle("has_medical", restrictions)
        viewModel.toggle("avoids_by_choice", restrictions)

        assertEquals(18, viewModel.visibleQuestions.size)
    }

    @Test
    fun exclusiveOptionClearsOtherSelectionsAndViceVersa() {
        val viewModel = viewModel()
        val diet = OnboardingQuestions.all.first { it.id == "diet" }

        viewModel.toggle("vegetarian", diet)
        viewModel.toggle("vegan", diet)
        viewModel.toggle("no_preference", diet)
        assertEquals(listOf("no_preference"), viewModel.answers.getValue("diet").selectedOptionIds)

        viewModel.toggle("chicken", diet)
        assertEquals(listOf("chicken"), viewModel.answers.getValue("diet").selectedOptionIds)
    }

    @Test
    fun cappedAndRankingQuestionsEnforceTheirLimits() {
        val viewModel = viewModel()
        val cuisines = OnboardingQuestions.all.first { it.id == "cuisines" }
        listOf("indian", "italian", "chinese", "thai", "japanese", "korean").forEach {
            viewModel.toggle(it, cuisines)
        }
        assertEquals(5, viewModel.selectedCount(cuisines))

        val aiHelp = OnboardingQuestions.all.first { it.id == "ai_help" }
        listOf(
            "decide_what_to_cook",
            "step_by_step_cooking",
            "grocery_lists",
            "meal_planning",
        ).forEach { viewModel.toggle(it, aiHelp) }
        assertEquals(3, viewModel.selectedCount(aiHelp))
        assertEquals(1, viewModel.rank("decide_what_to_cook", aiHelp))
        assertEquals(3, viewModel.rank("grocery_lists", aiHelp))
    }

    @Test
    fun continueRequiresTheSameSelectionRulesAsIos() {
        val viewModel = viewModel()
        val frequency = OnboardingQuestions.all.first { it.id == "cook_frequency" }
        val nutrition = OnboardingQuestions.all.first { it.id == "nutrition_goals" }
        val aiHelp = OnboardingQuestions.all.first { it.id == "ai_help" }

        assertFalse(viewModel.canContinue(frequency))
        viewModel.toggle("daily", frequency)
        assertTrue(viewModel.canContinue(frequency))

        assertFalse(viewModel.canContinue(nutrition))
        viewModel.updateOtherText("Low sodium", nutrition)
        assertTrue(viewModel.canContinue(nutrition))

        repeat(2) { viewModel.toggle(aiHelp.allOptions[it].id, aiHelp) }
        assertFalse(viewModel.canContinue(aiHelp))
        viewModel.toggle(aiHelp.allOptions[2].id, aiHelp)
        assertTrue(viewModel.canContinue(aiHelp))
    }

    @Test
    fun finishingLastQuestionPersistsCompletedProfileAndShowsFinalPage() {
        val question = question(
            id = "single",
            sectionTitle = "Test",
            text = "Pick one",
            kind = SingleSelect,
            options = listOf(OnboardingOption("one", "One")),
        )
        val store = FakeStore()
        val viewModel = OnboardingViewModel(listOf(question), store, isEditing = false)

        viewModel.beginQuestions()
        viewModel.toggle("one", question)
        viewModel.goNext()

        assertEquals(OnboardingScreen.Final, viewModel.screen)
        assertTrue(store.profile?.isComplete == true)
        assertEquals(listOf("one"), store.profile?.answers?.get("single")?.selectedOptionIds)
    }

    private fun viewModel(store: FakeStore = FakeStore()) =
        OnboardingViewModel(OnboardingQuestions.all, store, isEditing = false)

    private class FakeStore(var profile: KitchenProfile? = null) : KitchenProfileStore {
        override fun load(): KitchenProfile? = profile
        override fun save(profile: KitchenProfile) {
            this.profile = profile
        }
    }
}
