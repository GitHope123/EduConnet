package com.example.educonnet.ui.profesor

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.educonnet.R
import com.google.firebase.firestore.FirebaseFirestore

class EditProfesor : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var editTextNombres: EditText
    private lateinit var editTextApellidos: EditText
    private lateinit var editTextCelular: EditText
    private lateinit var editTextCargo: EditText
    private lateinit var editTextDni: EditText
    private lateinit var editTextCorreo: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var buttonGuardar: Button

    private lateinit var idProfesor: String
    private lateinit var nombres: String
    private lateinit var apellidos: String
    private var celular: Long = 0
    private lateinit var cargo: String
    private lateinit var correo: String
    private lateinit var password: String
    private var dni: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profesor)

        // Obtener datos del intent
        idProfesor = intent.getStringExtra("idProfesor") ?: ""
        nombres = intent.getStringExtra("nombres") ?: ""
        apellidos = intent.getStringExtra("apellidos") ?: ""
        celular = intent.getLongExtra("celular", 0)
        cargo = intent.getStringExtra("cargo") ?: ""
        correo = intent.getStringExtra("correo") ?: ""
        password = intent.getStringExtra("password") ?: ""
        dni = intent.getLongExtra("dni", 0)

        initViews()
        loadData()
        setupListeners()
    }

    private fun initViews() {
        firestore = FirebaseFirestore.getInstance()
        editTextNombres = findViewById(R.id.editTextNombres)
        editTextApellidos = findViewById(R.id.editTextApellidos)
        editTextCelular = findViewById(R.id.editTextCelular)
        editTextCargo = findViewById(R.id.editTextCargo)
        editTextCorreo = findViewById(R.id.editTextCorreo)
        editTextPassword = findViewById(R.id.editTextPassword)
        editTextDni = findViewById(R.id.editTextDni)
        buttonGuardar = findViewById(R.id.buttonModificar)
    }

    private fun loadData() {
        editTextNombres.setText(nombres)
        editTextApellidos.setText(apellidos)
        editTextCelular.setText(celular.toString())
        editTextCargo.setText(cargo)
        editTextCorreo.setText(correo)
        editTextPassword.setText(password)
        editTextDni.setText(dni.toString())
    }

    private fun setupListeners() {
        buttonGuardar.setOnClickListener {
            if (validateFields()) {
                updateData()
            }
        }
    }

    private fun validateFields(): Boolean {
        val nombres = editTextNombres.text.toString().trim()
        val apellidos = editTextApellidos.text.toString().trim()
        val celular = editTextCelular.text.toString().trim()
        val cargo = editTextCargo.text.toString().trim()
        val correo = editTextCorreo.text.toString().trim()
        val password = editTextPassword.text.toString().trim()
        val dni = editTextDni.text.toString().trim()

        if (nombres.isEmpty() || apellidos.isEmpty() || celular.isEmpty() ||
            cargo.isEmpty() || correo.isEmpty() || password.isEmpty() || dni.isEmpty()) {
            Toast.makeText(this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show()
            return false
        }

        if (celular.length != 9) {
            Toast.makeText(this, "El celular debe tener 9 dígitos", Toast.LENGTH_SHORT).show()
            return false
        }

        if (dni.length != 8) {
            Toast.makeText(this, "El DNI debe tener 8 dígitos", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!correo.endsWith("@colegiosparroquiales.com")) {
            Toast.makeText(this, "El correo debe terminar con @colegiosparroquiales.com", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun updateData() {
        val updatedData = mapOf(
            "nombres" to editTextNombres.text.toString().trim(),
            "apellidos" to editTextApellidos.text.toString().trim(),
            "celular" to editTextCelular.text.toString().trim().toLong(),
            "cargo" to editTextCargo.text.toString().trim(),
            "correo" to editTextCorreo.text.toString().trim(),
            "password" to editTextPassword.text.toString().trim(),
            "dni" to editTextDni.text.toString().trim().toLong()
        )

        firestore.collection("Profesor").document(idProfesor)
            .update(updatedData)
            .addOnSuccessListener {
                Toast.makeText(this, "Profesor actualizado con éxito", Toast.LENGTH_SHORT).show()
                setResult(Activity.RESULT_OK)
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Error al actualizar: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }
}