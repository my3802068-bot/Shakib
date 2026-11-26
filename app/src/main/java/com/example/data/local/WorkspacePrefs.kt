package com.example.data.local

import android.content.Context

class WorkspacePrefs(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(
        "cloud_phone_workspace_prefs",
        Context.MODE_PRIVATE
    )

    var hasSeededInitial: Boolean
        get() = prefs.getBoolean(KEY_SEEDED_INITIAL, false)
        set(value) = prefs.edit().putBoolean(KEY_SEEDED_INITIAL, value).apply()

    var lastTab: String
        get() = prefs.getString(KEY_LAST_TAB, "FLEET") ?: "FLEET"
        set(value) = prefs.edit().putString(KEY_LAST_TAB, value).apply()

    var lastFullScreenPhoneId: Long?
        get() {
            val v = prefs.getLong(KEY_LAST_FULLSCREEN_ID, -1L)
            return if (v > 0L) v else null
        }
        set(value) {
            prefs.edit().putLong(KEY_LAST_FULLSCREEN_ID, value ?: -1L).apply()
        }

    var lastFocusedMiniPhoneId: Long?
        get() {
            val v = prefs.getLong(KEY_LAST_FOCUSED_MINI_ID, -1L)
            return if (v > 0L) v else null
        }
        set(value) {
            prefs.edit().putLong(KEY_LAST_FOCUSED_MINI_ID, value ?: -1L).apply()
        }

    companion object {
        private const val KEY_SEEDED_INITIAL = "seeded_initial_v1"
        private const val KEY_LAST_TAB = "last_tab"
        private const val KEY_LAST_FULLSCREEN_ID = "last_fullscreen_phone_id"
        private const val KEY_LAST_FOCUSED_MINI_ID = "last_focused_mini_phone_id"
    }
}
