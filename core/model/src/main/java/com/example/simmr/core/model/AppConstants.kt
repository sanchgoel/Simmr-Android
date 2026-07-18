package com.example.simmr.core.model

object AppConstants {
    const val APP_NAME = "Simmr"

    object Network {
        const val OPENAI_ENDPOINT = "https://api.openai.com/v1/chat/completions"
        const val OPENAI_MODEL = "gpt-4.1-mini"
        const val RECIPE_SCHEMA_NAME = "recipe"
        const val TEMPERATURE = 0.2
        const val CONNECT_TIMEOUT_MS = 30_000
        const val READ_TIMEOUT_MS = 90_000
    }

    object Storage {
        const val KITCHEN_PROFILE_FILE = "simmr_kitchen_profile"
        const val KITCHEN_PROFILE_KEY = "profile"
        const val SETTINGS_FILE = "simmr_settings"
        const val ENCRYPTED_API_KEY = "encrypted_api_key"
        const val ENCRYPTED_API_KEY_IV = "encrypted_api_key_iv"
        const val API_KEY_ALIAS = "simmr_openai_api_key"
        const val PROMPT_OVERRIDE = "recipe_prompt_override"
    }

    object Timer {
        const val MILLIS_PER_SECOND = 1_000L
        const val SECONDS_PER_MINUTE = 60
        const val TICK_INTERVAL_MS = 250L
    }

    object Conversion {
        const val MILLILITERS_PER_CUP = 236.588
        const val DEFAULT_GRAMS_PER_CUP = 236.0
        const val QUARTER = 0.25
    }
}
