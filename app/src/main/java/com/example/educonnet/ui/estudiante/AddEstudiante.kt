package com.example.educonnet.ui.estudiante

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.educonnet.R
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.firestore.FirebaseFirestore

class AddEstudiante : AppCompatActivity() {
    // Contenedores TextInputLayout
    private lateinit var txtInputLayoutName: TextInputLayout
    private lateinit var txtInputLayoutLastName: TextInputLayout
    private lateinit var txtInputLayoutCelular: TextInputLayout

    // Campos de texto editables
    private lateinit var edTxtAddName: TextInputEditText
    private lateinit var edTxtAddLastName: TextInputEditText
    private lateinit var edTxtAddCelularApoderado: TextInputEditText

    private lateinit var spinnerAddGrado: Spinner
    private lateinit var spinnerAddSection: Spinner
    private lateinit var addName: String
    private lateinit var addLastName: String
    private var addGrado: Int = 0
    private lateinit var addSection: String
    private var addCelularApoderado: Int = 0
    private lateinit var db: FirebaseFirestore
    private lateinit var btnAdd: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_estudiantes)
        db = FirebaseFirestore.getInstance()
        initComponents()
        listener()
    }

    private fun initComponents() {
        // Obtener los TextInputLayout
        txtInputLayoutName = findViewById(R.id.nameLayout)
        txtInputLayoutLastName = findViewById(R.id.lastNameLayout)
        txtInputLayoutCelular = findViewById(R.id.phoneLayout)

        // Obtener los EditText desde los TextInputLayout
        edTxtAddName = txtInputLayoutName.findViewById(R.id.addNameStudent)
        edTxtAddLastName = txtInputLayoutLastName.findViewById(R.id.addLastNameStudent)
        edTxtAddCelularApoderado = txtInputLayoutCelular.findViewById(R.id.addCelularApoderado)

        spinnerAddGrado = findViewById(R.id.addSpinnerGradoStudent)
        spinnerAddSection = findViewById(R.id.addSpinnerSectionStudent)
        btnAdd = findViewById(R.id.buttonAddStudent)
        updateGrado()
    }

    private fun updateGrado() {
        val grados = arrayOf("1", "2", "3", "4", "5","6")
        val adapterGrados = ArrayAdapter(this, R.layout.spinner_item_selected, grados)
        adapterGrados.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerAddGrado.adapter = adapterGrados
        spinnerAddGrado.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val gradoSeleccionado = spinnerAddGrado.selectedItem.toString()
                updateSecciones(gradoSeleccionado)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun updateSecciones(gradoSeleccionado: String) {
        val niveles = when (gradoSeleccionado) {
            "1" -> arrayOf("Secundaria")
            "2" -> arrayOf("Secundaria")
            "3" -> arrayOf("Secundaria")
            "4" -> arrayOf("Secundaria")
            "5" -> arrayOf("Primaria", "Secundaria")
            "6" -> arrayOf("Primaria")
            else -> arrayOf("Seleccione")
        }

        val adapterSecciones = ArrayAdapter(this, R.layout.spinner_item_selected, niveles)
        adapterSecciones.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerAddSection.adapter = adapterSecciones
        spinnerAddSection.isEnabled = gradoSeleccionado != "Todas"
    }

    private fun listener() {
        btnAdd.setOnClickListener {
            saveStudentToFirebase()
        }
    }

    private fun saveStudentToFirebase() {
        addName = edTxtAddName.text.toString().trim()
        addLastName = edTxtAddLastName.text.toString().trim()
        addGrado = spinnerAddGrado.selectedItem.toString().trim().toIntOrNull() ?: 0
        addSection = spinnerAddSection.selectedItem.toString().trim()
        addCelularApoderado = edTxtAddCelularApoderado.text.toString().toIntOrNull() ?: 0

        if (addName.isNotEmpty() && addLastName.isNotEmpty() && addSection.isNotEmpty()) {
            val student = Estudiante(
                idEstudiante = "",
                nombres = addName,
                apellidos = addLastName,
                grado = addGrado,
                nivel = addSection,
                cantidadIncidencias = 0,
                celularApoderado = addCelularApoderado
            )

            db.collection("Estudiante")
                .add(student)
                .addOnSuccessListener { documentReference ->
                    val updatedStudent = student.copy(idEstudiante = documentReference.id)
                    documentReference.update("idEstudiante", updatedStudent.idEstudiante)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Estudiante agregado con éxito", Toast.LENGTH_SHORT).show()
                            clearFields()
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Error al actualizar ID del estudiante", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al agregar al estudiante", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Complete todos los campos", Toast.LENGTH_SHORT).show()
        }
    }

    private fun clearFields() {
        edTxtAddName.text?.clear()
        edTxtAddLastName.text?.clear()
        edTxtAddCelularApoderado.text?.clear()
    }
}