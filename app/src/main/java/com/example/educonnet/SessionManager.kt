package com.example.educonnet

import android.content.Context

object SessionManager {
    private const val PREF_NAME = "user_session"

    fun saveUserData(context: Context, data: LoginActivity.GlobalData) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString("datoTipoUsuario", data.datoTipoUsuario)
            putString("idUsuario", data.idUsuario)
            putString("nombresUsuario", data.nombresUsuario)
            putString("apellidosUsuario", data.apellidosUsuario)
            putLong("celularUsuario", data.celularUsuario)
            putString("correoUsuario", data.correoUsuario)
            putBoolean("tutor", data.tutor)
            putString("cargoUsuario", data.cargoUsuario)
            putLong("gradoUsuario", data.gradoUsuario)
            putString("seccionUsuario", data.seccionUsuario)
            putString("passwordUsuario", data.passwordUsuario)
            apply()
        }
    }

    fun loadUserData(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        LoginActivity.GlobalData.datoTipoUsuario = prefs.getString("datoTipoUsuario", "") ?: ""
        LoginActivity.GlobalData.idUsuario = prefs.getString("idUsuario", "") ?: ""
        LoginActivity.GlobalData.nombresUsuario = prefs.getString("nombresUsuario", "") ?: ""
        LoginActivity.GlobalData.apellidosUsuario = prefs.getString("apellidosUsuario", "") ?: ""
        LoginActivity.GlobalData.celularUsuario = prefs.getLong("celularUsuario", 0L)
        LoginActivity.GlobalData.correoUsuario = prefs.getString("correoUsuario", "") ?: ""
        LoginActivity.GlobalData.tutor = prefs.getBoolean("tutor", false)
        LoginActivity.GlobalData.cargoUsuario = prefs.getString("cargoUsuario", "") ?: ""
        LoginActivity.GlobalData.gradoUsuario = prefs.getLong("gradoUsuario", 0L)
        LoginActivity.GlobalData.seccionUsuario = prefs.getString("seccionUsuario", "") ?: ""
        LoginActivity.GlobalData.passwordUsuario = prefs.getString("passwordUsuario", "") ?: ""
    }

    fun clearUserData(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }
}