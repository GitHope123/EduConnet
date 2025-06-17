package com.example.educonnet

import android.content.Intent
import android.os.Bundle
import kotlinx.coroutines.*
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.educonnet.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import android.content.Context

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var binding: ActivityLoginBinding
    private var userType: String = "" // Cambiado a var para permitir reassign
    private var id: String = "" // Cambiado a var para permitir reassign

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        window.statusBarColor = ContextCompat.getColor(this, R.color.md_theme_primary)
        setContentView(binding.root)

        // Cargar datos persistentes de usuario
        SessionManager.loadUserData(this)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()

            when {
                email.isEmpty() -> {
                    binding.emailTextInputLayout.error = "Ingrese su correo electrónico"
                    binding.passwordTextInputLayout.error = null
                }
                password.isEmpty() -> {
                    binding.passwordTextInputLayout.error = "Ingrese su contraseña"
                    binding.emailTextInputLayout.error = null
                }
                else -> {
                    binding.emailTextInputLayout.error = null
                    binding.passwordTextInputLayout.error = null
                    loginWithFirebase(email, password)
                }
            }
        }
    }

    private fun loginWithFirebase(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                handleAdminLogin()
            }
            .addOnFailureListener {
                checkTeacherCredentials(email, password)
            }
    }

    private fun handleAdminLogin() {
        userType = "Administrador"
        GlobalData.apply {
            datoTipoUsuario = userType
            idUsuario = "Administrador"
        }
        // Guardar datos persistentes
        SessionManager.saveUserData(this, GlobalData)
        navigateToMainActivity()
    }

    private fun checkTeacherCredentials(email: String, password: String) {
        firestore.collection("Profesor")
            .whereEqualTo("correo", email)
            .whereEqualTo("password", password)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val documents = task.result
                    if (!documents.isEmpty) {
                        handleTeacherLogin(documents.first())
                    } else {
                        showError("Credenciales incorrectas")
                    }
                } else {
                    showError("Error al consultar la base de datos")
                }
            }
    }

    private fun handleTeacherLogin(document: DocumentSnapshot) {
        CoroutineScope(Dispatchers.Default).launch {
            val isTutor = document.getBoolean("tutor") == true
            userType = if (isTutor) "Tutor" else "Profesor"
            id = document.getString("idProfesor") ?: ""

            GlobalData.apply {
                datoTipoUsuario = userType
                idUsuario = id
                nombresUsuario = document.getString("nombres") ?: ""
                apellidosUsuario = document.getString("apellidos") ?: ""
                celularUsuario = document.getLong("celular") ?: 0L
                correoUsuario = document.getString("correo") ?: ""
                tutor = isTutor
                cargoUsuario = document.getString("cargo") ?:""
                gradoUsuario = document.getLong("grado") ?: 0L
                seccionUsuario = document.getString("seccion") ?: ""
                passwordUsuario = document.getString("password") ?: ""
            }

            // Guardar datos persistentes
            withContext(Dispatchers.Main) {
                SessionManager.saveUserData(this@LoginActivity, GlobalData)
                navigateToMainActivity()
            }
        }
    }


    private fun navigateToMainActivity() {
        val intent = Intent(this, BarraNavegacionActivity::class.java).apply {
            putExtra("USER_TYPE", userType)
            if (userType == "Profesor" || userType == "Tutor") {
                putExtra("ID", id)
            }
        }
        startActivity(intent)
        finish() // Corregido error de unresolved reference
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    companion object GlobalData {
        var datoTipoUsuario: String = ""
        var idUsuario: String = ""
        var nombresUsuario: String = ""
        var apellidosUsuario: String = ""
        var celularUsuario: Long = 0L
        var correoUsuario: String = ""
        var tutor: Boolean = false// Mantenido aunque no se use directamente
        var cargoUsuario : String = ""
        var gradoUsuario: Long = 0L
        var seccionUsuario: String = ""
        var passwordUsuario: String =""
    }
}
