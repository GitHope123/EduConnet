package com.example.educonnet.ui.estudiante

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.educonnet.R
import com.google.firebase.firestore.FirebaseFirestore

class EditEstudiante : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var editTextNombres: EditText
    private lateinit var editTextApellidos: EditText
    private lateinit var editTextCelularApoderado: EditText
    private lateinit var spinnerGrado: Spinner
    private lateinit var spinnerSeccion: Spinner
    private lateinit var buttonModificar: Button
    private lateinit var updatedNombres: String
    private lateinit var updatedApellidos: String
    private  var updatedCelularApoderado: Int =0
    private var updatedGrado: Int = 0
    private lateinit var updatedSeccion: String
    private lateinit var idEstudiante: String
    private lateinit var nombres: String
    private lateinit var apellidos: String
    private var grado: Int = 0
    private lateinit var nivel: String
    private var celular: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_estudiante)
        findButtons()
        listener()
    }

    private fun listener() {
        buttonModificar.setOnClickListener {
            modifyStudent(updatedGrado, updatedSeccion)
        }
    }

    private fun getData() {
        idEstudiante = intent.getStringExtra("idEstudiante") ?: ""
        nombres = intent.getStringExtra("nombres") ?: ""
        apellidos = intent.getStringExtra("apellidos") ?: ""
        grado = intent.getIntExtra("grado", 0)
        nivel = intent.getStringExtra("nivel") ?: ""
        celular = intent.getIntExtra("celularApoderado", 0)

        editTextNombres.setText(nombres)
        editTextApellidos.setText(apellidos)
        editTextCelularApoderado.setText(celular.toString())  // Asegúrate de que sea una cadena

        updatedGrado()
    }

    private fun findButtons() {
        firestore = FirebaseFirestore.getInstance()

        // Inicializa cada vista usando findViewById y verifica que los IDs estén correctos
        editTextNombres = findViewById(R.id.editTextNombres)
        editTextApellidos = findViewById(R.id.editTextApellidos)
        editTextCelularApoderado = findViewById(R.id.editTextCelularApoderadoEdit)
        spinnerGrado = findViewById(R.id.spinnerGrado)
        spinnerSeccion = findViewById(R.id.spinnerSection)
        buttonModificar = findViewById(R.id.buttonModificar)

        getData()
    }

    private fun updatedGrado() {
        val grados = arrayOf("1", "2", "3", "4", "5", "6")
        val adapterGrados = ArrayAdapter(this, R.layout.spinner_item_general, grados)
        adapterGrados.setDropDownViewResource(R.layout.simple_spinner_general)
        spinnerGrado.adapter = adapterGrados
        setSpinnerValue(spinnerGrado, grado.toString())

        spinnerGrado.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                updatedGrado = spinnerGrado.selectedItem.toString().trim().toIntOrNull() ?: 0
                updatedSeccion(updatedGrado)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun updatedSeccion(gradoSeleccionado: Int) {
        val secciones = when (gradoSeleccionado.toString()) {
            "Seleccione" -> arrayOf("Seleccione")
            "1", "2", "3", "4" -> arrayOf("Secundaria")
            "5" -> arrayOf("Primaria", "Secundaria")
            "6" -> arrayOf("Primaria")
            else -> arrayOf("Seleccione")
        }

        val adapterSecciones = ArrayAdapter(this, R.layout.spinner_item_general, secciones)
        adapterSecciones.setDropDownViewResource(R.layout.simple_spinner_general)
        spinnerSeccion.adapter = adapterSecciones
        setSpinnerValue(spinnerSeccion, nivel)

        spinnerSeccion.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                updatedSeccion = spinnerSeccion.selectedItem.toString().trim()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun modifyStudent(updatedGrado: Int, updatedSeccion: String) {
        updatedNombres = editTextNombres.text.toString().trim()
        updatedApellidos = editTextApellidos.text.toString().trim()
        updatedCelularApoderado = editTextCelularApoderado.text.toString().toIntOrNull() ?: 0

        if (updatedNombres.isNotEmpty() && updatedApellidos.isNotEmpty() && updatedSeccion.isNotEmpty()) {
            val updatedEstudiante = mapOf(
                "nombres" to updatedNombres,
                "apellidos" to updatedApellidos,
                "grado" to updatedGrado,
                "nivel" to updatedSeccion,
                "celularApoderado" to updatedCelularApoderado
            )

            firestore.collection("Estudiante").document(idEstudiante)
                .update(updatedEstudiante)
                .addOnSuccessListener {
                    Toast.makeText(this, "Estudiante actualizado con éxito", Toast.LENGTH_SHORT).show()
                    setResult(Activity.RESULT_OK)
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al actualizar: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Complete todos los campos", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setSpinnerValue(spinner: Spinner, value: String) {
        val adapter = spinner.adapter as? ArrayAdapter<String>
        val position = adapter?.getPosition(value) ?: 0
        spinner.setSelection(position)
    }
}
