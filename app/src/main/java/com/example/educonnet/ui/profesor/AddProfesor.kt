package com.example.educonnet.ui.profesor

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.educonnet.databinding.ActivityAddProfesorBinding
import com.google.firebase.firestore.FirebaseFirestore

class AddProfesor : AppCompatActivity() {
    private lateinit var binding: ActivityAddProfesorBinding
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private var isSaving = false // Flag to prevent duplicate saves

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddProfesorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
    }

    private fun setupListeners() {
        binding.buttonAddProfesor.setOnClickListener {
            if (!isSaving) {
                isSaving = true
                saveProfesorToFirebase()
            }
        }
    }

    private fun saveProfesorToFirebase() {
        // Extract data from inputs
        val nombres = binding.editTextNombres.text?.toString()?.trim().orEmpty()
        val apellidos = binding.editTextApellidos.text?.toString()?.trim().orEmpty()
        val celularStr = binding.editTextCelular.text?.toString()?.trim().orEmpty()
        val correo = binding.editTextCorreo.text?.toString()?.trim().orEmpty()
        val password = binding.editTextPassword.text?.toString()?.trim().orEmpty()
        val cargo = binding.editTextCargo.text?.toString()?.trim().orEmpty()

        // Validate inputs
        if (nombres.isEmpty() || apellidos.isEmpty() || celularStr.isEmpty() ||
            cargo.isEmpty() || correo.isEmpty() || password.isEmpty()) {
            showToast("Por favor, complete todos los campos")
            isSaving = false
            return
        }

        if (celularStr.length != 9) {
            showToast("El celular debe tener 9 dígitos y el DNI 8 dígitos")
            isSaving = false
            return
        }

        if (!correo.endsWith("@colegiosparroquiales.com")) {
            showToast("El correo debe pertenecer al dominio @colegiosparroquiales.com")
            isSaving = false
            return
        }

        // Convert numeric fields
        val celular = celularStr.toLongOrNull() ?: run {
            showToast("Número de celular inválido")
            isSaving = false
            return
        }


        // Prepare data for Firestore
        val profesor = mapOf(
            "nombres" to nombres,
            "apellidos" to apellidos,
            "celular" to celular,
            "cargo" to cargo,
            "correo" to correo,
            "tutor" to false,
            "grado" to 0,
            "seccion" to "",
            "password" to password
        )

        // Save to Firestore
        firestore.collection("Profesor")
            .add(profesor)
            .addOnSuccessListener { documentReference ->
                documentReference.update("idProfesor", documentReference.id)
                    .addOnSuccessListener {
                        showToast("Profesor agregado correctamente")
                        clearFields()
                        finish()
                    }
                    .addOnFailureListener { e ->
                        showToast("Error al actualizar el ID del profesor: ${e.message}")
                        isSaving = false
                    }
            }
            .addOnFailureListener { e ->
                showToast("Error al agregar profesor: ${e.message}")
                isSaving = false
            }
    }

    private fun clearFields() {
        binding.editTextNombres.text?.clear()
        binding.editTextApellidos.text?.clear()
        binding.editTextCelular.text?.clear()
        binding.editTextCorreo.text?.clear()
        binding.editTextPassword.text?.clear()
        binding.editTextCargo.text?.clear()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
