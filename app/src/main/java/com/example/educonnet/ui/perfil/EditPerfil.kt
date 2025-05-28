package com.example.educonnet.ui.perfil

import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.educonnet.LoginActivity
import com.example.educonnet.R
import com.example.educonnet.databinding.ActivityEditPerfilBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class EditPerfil : AppCompatActivity() {

    private lateinit var binding: ActivityEditPerfilBinding
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Cargar datos del intent
        binding.etNombres.setText(intent.getStringExtra("nombre"))
        binding.etApellidos.setText(intent.getStringExtra("apellido"))
        binding.etCelular.setText(intent.getStringExtra("celular"))
        binding.etCorreo.setText(intent.getStringExtra("correo"))
        binding.etPassword.setText(intent.getStringExtra("password"))
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Habilita el botón de retroceso
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Maneja el clic en la flecha de retroceso
        toolbar.setNavigationOnClickListener {
            finish() // Cierra la actividad actual y vuelve a la anterior
        }
        // Configurar campos no editables
        binding.etNombres.isEnabled = false
        binding.etApellidos.isEnabled = false
        binding.etCorreo.isEnabled = false

        binding.etCelular.filters = arrayOf(
            InputFilter.LengthFilter(9),
            InputFilter { source, _, _, _, _, _ ->
                if (source.matches(Regex("[0-9]*"))) {
                    null // Acepta números
                } else {
                    Toast.makeText(this, "Solo se permiten números", Toast.LENGTH_SHORT).show()
                    "" // Bloquea cualquier carácter no numérico
                }
            }
        )

        binding.btnGuardar.setOnClickListener {
            guardarCambios(intent.getStringExtra("id") ?: return@setOnClickListener)
        }
    }

    private fun guardarCambios(userId: String) {
        val nuevoCelular = binding.etCelular.text.toString().trim()
        val nuevaPassword = binding.etPassword.text.toString().trim()

        // Validaciones mejoradas
        if (nuevoCelular.length != 9) {
            binding.etCelular.error = "Debe tener 9 dígitos"
            return
        }

        if (nuevaPassword.length < 8) {
            binding.etPassword.error = "Mínimo 8 caracteres"
            return
        }

        db.collection("Profesor").document(userId)
            .update(mapOf(
                "celular" to nuevoCelular.toLong(),
                "password" to nuevaPassword
            ))
            .addOnSuccessListener {
                // Actualizar GlobalData
                LoginActivity.GlobalData.apply {
                    celularUsuario = nuevoCelular.toLong()
                    passwordUsuario = nuevaPassword
                }

                // Preparar datos para devolver al fragmento
                val resultData = Intent().apply {
                    putExtra("nuevoCelular", nuevoCelular)
                    putExtra("nuevaPassword", nuevaPassword)
                }

                setResult(RESULT_OK, resultData)
                Toast.makeText(this, "Perfil actualizado", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}