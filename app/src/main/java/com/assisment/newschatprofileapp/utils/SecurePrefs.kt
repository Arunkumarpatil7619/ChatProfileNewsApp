package com.assisment.newschatprofileapp.utils



import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys


object SecurePrefs {

    private const val FILE_NAME = "secure_prefs"
    private const val KEY_API = "api_key"

    private var prefs: EncryptedSharedPreferences? = null

    fun init(context: Context) {
        if (prefs == null) {
            val masterKey = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

            prefs = EncryptedSharedPreferences.create(
                FILE_NAME,
                masterKey,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            ) as EncryptedSharedPreferences
        }
    }

    fun saveApiKey(context: Context,apiKey: String) {
        prefs?.edit()?.putString(KEY_API, apiKey)?.apply()
    }

    fun getApiKey(): String {
        return prefs?.getString(KEY_API, "") ?: ""
    }
}
