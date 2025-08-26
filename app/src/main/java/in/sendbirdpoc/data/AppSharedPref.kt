package `in`.sendbirdpoc.data

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppSharedPref @Inject constructor(@ApplicationContext private val context: Context) {

    companion object {
        private const val USER_PREF = "userPreference"
        private const val IS_LOGGED_IN = "isLoggedIn"
        private const val LOGIN_USER_SEND_BIRD_ID = "LOGIN_USER_SEND_BIRD_ID"
        private const val LOGIN_USER_SEND_ACCESS_TOKEN = "LOGIN_USER_SEND_ACCESS_TOKEN"
    }

    private fun getSharedPreference(preferenceFile: String): SharedPreferences {
        return context.getSharedPreferences(preferenceFile, Context.MODE_PRIVATE)
    }

    private fun getSharedPreferenceEditor(
        preferenceFile: String
    ): SharedPreferences.Editor {
        return context.getSharedPreferences(preferenceFile, Context.MODE_PRIVATE).edit()
    }

    var isLoggedIn: Boolean
        get() = getSharedPreference(USER_PREF).getBoolean(IS_LOGGED_IN, false)
        set(value) {
            getSharedPreferenceEditor(USER_PREF).putBoolean(IS_LOGGED_IN, value).apply()
        }


    var loginUserSendbirdId: String?
        get() = getSharedPreference(USER_PREF).getString(LOGIN_USER_SEND_BIRD_ID, "")
        set(value) {
            getSharedPreferenceEditor(USER_PREF).putString(LOGIN_USER_SEND_BIRD_ID, value).apply()
        }

    var loginUserSendbirdAccessToken: String?
        get() = getSharedPreference(USER_PREF).getString(LOGIN_USER_SEND_ACCESS_TOKEN, "")
        set(value) {
            getSharedPreferenceEditor(USER_PREF).putString(LOGIN_USER_SEND_ACCESS_TOKEN, value).apply()
        }
}