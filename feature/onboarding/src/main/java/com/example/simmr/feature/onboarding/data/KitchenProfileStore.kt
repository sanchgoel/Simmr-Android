package com.example.simmr.feature.onboarding.data

import android.content.Context
import com.example.simmr.core.model.AppConstants
import com.example.simmr.feature.onboarding.model.OnboardingAnswer
import com.example.simmr.feature.onboarding.model.OnboardingAnswers
import org.json.JSONArray
import org.json.JSONObject

internal data class KitchenProfile(
    val answers: OnboardingAnswers = emptyMap(),
    val isComplete: Boolean = false,
    val completedAt: Long? = null,
)

internal interface KitchenProfileStore {
    fun load(): KitchenProfile?
    fun save(profile: KitchenProfile)
}

internal class SharedPreferencesKitchenProfileStore(context: Context) : KitchenProfileStore {
    private val preferences = context.applicationContext.getSharedPreferences(
        AppConstants.Storage.KITCHEN_PROFILE_FILE,
        Context.MODE_PRIVATE,
    )

    override fun load(): KitchenProfile? {
        val encoded = preferences.getString(AppConstants.Storage.KITCHEN_PROFILE_KEY, null) ?: return null
        return runCatching {
            val root = JSONObject(encoded)
            val encodedAnswers = root.optJSONObject("answers") ?: JSONObject()
            val answers = buildMap {
                val keys = encodedAnswers.keys()
                while (keys.hasNext()) {
                    val questionId = keys.next()
                    val encodedAnswer = encodedAnswers.getJSONObject(questionId)
                    val selected = encodedAnswer.optJSONArray("selectedOptionIds") ?: JSONArray()
                    put(
                        questionId,
                        OnboardingAnswer(
                            selectedOptionIds = List(selected.length()) { selected.getString(it) },
                            otherText = encodedAnswer.optString("otherText"),
                        ),
                    )
                }
            }
            KitchenProfile(
                answers = answers,
                isComplete = root.optBoolean("isComplete"),
                completedAt = root.optLong("completedAt").takeIf { root.has("completedAt") },
            )
        }.getOrNull()
    }

    override fun save(profile: KitchenProfile) {
        val encodedAnswers = JSONObject()
        profile.answers.forEach { (questionId, answer) ->
            encodedAnswers.put(
                questionId,
                JSONObject()
                    .put("selectedOptionIds", JSONArray(answer.selectedOptionIds))
                    .put("otherText", answer.otherText),
            )
        }
        val root = JSONObject()
            .put("answers", encodedAnswers)
            .put("isComplete", profile.isComplete)
        profile.completedAt?.let { root.put("completedAt", it) }
        preferences.edit().putString(AppConstants.Storage.KITCHEN_PROFILE_KEY, root.toString()).apply()
    }
}

object OnboardingStatus {
    fun isComplete(context: Context): Boolean =
        SharedPreferencesKitchenProfileStore(context).load()?.isComplete == true
}
