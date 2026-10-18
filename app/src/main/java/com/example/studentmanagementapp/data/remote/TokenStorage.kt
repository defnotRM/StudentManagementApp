package com.example.studentmanagementapp.data.remote

import android.content.Context

class TokenStorage(context: Context) {

    private val preferences =
        context.getSharedPreferences(
            "auth_preferences",
            Context.MODE_PRIVATE
        )

    fun saveToken(token: String) {
        preferences.edit()
            .putString("access_token", token)
            .apply()
    }

    fun getToken(): String? {
        return preferences.getString("access_token", null)
    }

    fun clearToken() {
        preferences.edit()
            .remove("access_token")
            .apply()
    }
}
