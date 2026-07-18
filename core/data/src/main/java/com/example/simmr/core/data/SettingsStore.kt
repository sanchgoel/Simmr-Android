package com.example.simmr.core.data

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import com.example.simmr.core.model.AppConstants
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class SettingsStore(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(
        AppConstants.Storage.SETTINGS_FILE,
        Context.MODE_PRIVATE,
    )

    fun hasApiKey(): Boolean = apiKey().isNullOrBlank().not()

    fun apiKey(): String? = runCatching {
        val encoded = preferences.getString(AppConstants.Storage.ENCRYPTED_API_KEY, null) ?: return null
        val encodedIv = preferences.getString(AppConstants.Storage.ENCRYPTED_API_KEY_IV, null) ?: return null
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(
            Cipher.DECRYPT_MODE,
            secretKey(),
            GCMParameterSpec(TAG_LENGTH_BITS, Base64.decode(encodedIv, Base64.NO_WRAP)),
        )
        String(cipher.doFinal(Base64.decode(encoded, Base64.NO_WRAP)), Charsets.UTF_8)
    }.getOrNull()

    fun saveApiKey(value: String) {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey())
        val encrypted = cipher.doFinal(value.trim().toByteArray(Charsets.UTF_8))
        preferences.edit()
            .putString(AppConstants.Storage.ENCRYPTED_API_KEY, Base64.encodeToString(encrypted, Base64.NO_WRAP))
            .putString(AppConstants.Storage.ENCRYPTED_API_KEY_IV, Base64.encodeToString(cipher.iv, Base64.NO_WRAP))
            .apply()
    }

    fun clearApiKey() {
        preferences.edit()
            .remove(AppConstants.Storage.ENCRYPTED_API_KEY)
            .remove(AppConstants.Storage.ENCRYPTED_API_KEY_IV)
            .apply()
    }

    fun promptOverride(): String? = preferences.getString(AppConstants.Storage.PROMPT_OVERRIDE, null)

    fun savePromptOverride(value: String?) {
        val trimmed = value?.trim().orEmpty()
        preferences.edit().apply {
            if (trimmed.isEmpty()) remove(AppConstants.Storage.PROMPT_OVERRIDE)
            else putString(AppConstants.Storage.PROMPT_OVERRIDE, trimmed)
        }.apply()
    }

    private fun secretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (keyStore.getKey(AppConstants.Storage.API_KEY_ALIAS, null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        generator.init(
            KeyGenParameterSpec.Builder(
                AppConstants.Storage.API_KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
            ).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build(),
        )
        return generator.generateKey()
    }

    private companion object {
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val TAG_LENGTH_BITS = 128
    }
}
