package br.com.rossiny.movielist.utils

import android.content.Context
import android.preference.PreferenceManager

class PreferencesHelper(context: Context){
    companion object {
        val DEVELOP_MODE = false
        private const val RESULTS = "data.source.prefs.RESULTS"
    }

    private val preferences = PreferenceManager.getDefaultSharedPreferences(context)

    // save device token
    var results = preferences.getString(RESULTS, "")
        set(value) = preferences.edit().putString(RESULTS, value).apply()

}