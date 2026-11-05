package com.skillswap.app.util

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

object LocaleHelper {

    fun applySavedLocale(context: Context) {
        val lang = getSavedLanguage(context)
        setLocale(lang)
    }

    fun setLocale(languageCode: String) {
        val appLocales = LocaleListCompat.forLanguageTags(languageCode)
        AppCompatDelegate.setApplicationLocales(appLocales)
    }

    fun getSavedLanguage(context: Context): String {
        val prefs = context.getSharedPreferences("skillswap_locale_prefs", Context.MODE_PRIVATE)
        return prefs.getString("language_pref", "en") ?: "en"
    }

    fun saveLanguage(context: Context, languageCode: String) {
        val prefs = context.getSharedPreferences("skillswap_locale_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("language_pref", languageCode).apply()
        setLocale(languageCode)
    }
}
