
package com.bid.ai.data

import android.content.Context

class SessionManager(context: Context) {

    private val preferences = context.getSharedPreferences(
        "bidai_session",
        Context.MODE_PRIVATE
    )

    fun saveToken(token: String) {
        preferences.edit()
            .putString("auth_token", token)
            .apply()
    }

    fun getToken(): String? {
        return preferences.getString("auth_token", null)
    }

    fun clearSession() {
        preferences.edit()
            .remove("auth_token")
            .apply()
    }

    fun isLoggedIn(): Boolean {
        return !getToken().isNullOrBlank()
    }
}
