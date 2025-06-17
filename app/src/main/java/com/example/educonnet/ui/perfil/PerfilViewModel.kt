package com.example.educonnet.ui.perfil

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.educonnet.LoginActivity

class PerfilViewModel : ViewModel() {
    private val _userData = MutableLiveData<UserData>()
    val userData: LiveData<UserData> = _userData

    init {
        loadUserData()
    }

    fun loadUserData() {
        val globalData = LoginActivity.GlobalData
        _userData.value = UserData(
            nombre = globalData.nombresUsuario,
            apellido = globalData.apellidosUsuario,
            celular = globalData.celularUsuario.toString(),
            correo = globalData.correoUsuario,
            password = globalData.passwordUsuario,
            id = globalData.idUsuario,
            isTutor = globalData.tutor
        )
    }

    data class UserData(
        val nombre: String,
        val apellido: String,
        val celular: String,
        val correo: String,
        val password: String,
        val id: String,
        val isTutor: Boolean
    )
} 