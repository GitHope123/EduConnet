package com.example.educonnet.ui.tutoria.recurso


import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.view.isVisible
import com.example.educonnet.R
import com.example.educonnet.ui.tutoria.TutoriaClass
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class CitarFormulario private constructor() {

    companion object {
        fun mostrar(
            context: Context,
            tutoria: TutoriaClass,
            callback: TutoriaUpdateCallback? = null
        ) {
            val view = LayoutInflater.from(context).inflate(R.layout.dialog_cita_form, null)
            val dialog = MaterialAlertDialogBuilder(context)
                .setTitle(context.getString(R.string.dialog_citar_titulo))
                .setView(view)
                .setNegativeButton(context.getString(R.string.btn_cancelar)) { dialog, _ -> dialog.dismiss() }
                .create()

            // Configurar vista
            val editTextApoderado = view.findViewById<EditText>(R.id.editTextApoderado)
            val editTextFecha = view.findViewById<EditText>(R.id.editTextFecha)
            val editTextHora = view.findViewById<EditText>(R.id.editTextHora)
            val spinnerParentesco = view.findViewById<Spinner>(R.id.SpinnerParentesco)
            val checkBox = view.findViewById<MaterialCheckBox>(R.id.confirm_checkbox_cita)
            val btnEnviar = view.findViewById<MaterialButton>(R.id.btn_enviar_cita)
            val progressBar = view.findViewById<View>(R.id.progressBar)

            // Configurar Spinner de Parentesco
            val parentescos = context.resources.getStringArray(R.array.parentesco_array)
            val adapter = ArrayAdapter(context, R.layout.spinner_item_general, parentescos)
            adapter.setDropDownViewResource(R.layout.simple_spinner_general)
            spinnerParentesco.adapter = adapter

            val calendar = Calendar.getInstance()
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

            // Configurar listeners
            setupDatePicker(context, editTextFecha, calendar, dateFormat)
            setupTimePicker(context, editTextHora, calendar, timeFormat)
            setupCheckboxValidation(checkBox, btnEnviar)

            setupSubmitButton(
                context = context,
                button = btnEnviar,
                progressBar = progressBar,
                dialog = dialog,
                callback = callback,
                editTextApoderado = editTextApoderado,
                editTextFecha = editTextFecha,
                editTextHora = editTextHora,
                spinnerParentesco = spinnerParentesco,
                tutoria = tutoria
            )

            dialog.show()
        }

        private fun setupDatePicker(
            context: Context,
            editText: EditText,
            calendar: Calendar,
            dateFormat: SimpleDateFormat
        ) {
            editText.setOnClickListener {
                // Forzar tema claro clásico (funciona en todos los Android)

                val datePickerDialog = DatePickerDialog(
                    context,
                    { _, year, month, day ->
                        calendar.set(year, month, day)
                        editText.setText(dateFormat.format(calendar.time))
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                )

                datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
                datePickerDialog.show()
            }
        }



        private fun setupTimePicker(
            context: Context,
            editText: EditText,
            calendar: Calendar,
            timeFormat: SimpleDateFormat
        ) {
            editText.setOnClickListener {
                TimePickerDialog(
                    context,
                    { _, hour, minute ->
                        calendar.set(Calendar.HOUR_OF_DAY, hour)
                        calendar.set(Calendar.MINUTE, minute)
                        editText.setText(timeFormat.format(calendar.time))
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
                ).show()
            }
        }

        private fun setupCheckboxValidation(
            checkBox: MaterialCheckBox,
            button: MaterialButton
        ) {
            button.isEnabled = false
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                button.isEnabled = isChecked
            }
        }

        private fun setupSubmitButton(
            context: Context,
            button: MaterialButton,
            progressBar: View,
            dialog: AlertDialog,
            callback: TutoriaUpdateCallback?,
            editTextApoderado: EditText,
            editTextFecha: EditText,
            editTextHora: EditText,
            spinnerParentesco: Spinner,
            tutoria: TutoriaClass
        ) {
            button.setOnClickListener {
                val apoderado = editTextApoderado.text.toString().trim()
                val fecha = editTextFecha.text.toString().trim()
                val hora = editTextHora.text.toString().trim()
                val parentesco = spinnerParentesco.selectedItem.toString()

                if (!validarCampos(context, apoderado, fecha, hora)) return@setOnClickListener

                progressBar.isVisible = true
                button.isEnabled = false

                val cita = crearCita(tutoria, apoderado, fecha, hora, parentesco)
                guardarCitaEnFirestore(
                    context = context,
                    cita = cita,
                    tutoria = tutoria,
                    progressBar = progressBar,
                    button = button,
                    dialog = dialog,
                    callback = callback
                )
            }
        }

        private fun validarCampos(
            context: Context,
            apoderado: String,
            fecha: String,
            hora: String
        ): Boolean {
            if (apoderado.isEmpty() || fecha.isEmpty() || hora.isEmpty()) {
                Toast.makeText(
                    context,
                    context.getString(R.string.error_campos_vacios),
                    Toast.LENGTH_SHORT
                ).show()
                return false
            }
            return true
        }

        private fun crearCita(
            tutoria: TutoriaClass,
            apoderado: String,
            fecha: String,
            hora: String,
            parentesco: String
        ): Cita {
            return Cita(
                idCita = tutoria.id,
                createFecha = obtenerFechaActual(),
                apoderado = apoderado,
                fechaCita = fecha,
                hora = hora,
                parentesco = parentesco
            )
        }

        private fun guardarCitaEnFirestore(
            context: Context,
            cita: Cita,
            tutoria: TutoriaClass,
            progressBar: View,
            button: MaterialButton,
            dialog: AlertDialog,
            callback: TutoriaUpdateCallback?
        ) {
            val db = FirebaseFirestore.getInstance()
            val batch = db.batch()

            val citaRef = db.collection("Cita").document(cita.idCita)
            val tutoriaRef = db.collection("Incidencia").document(tutoria.id)

            batch.set(citaRef, cita)
            batch.update(tutoriaRef, "estado", "Citado")

            batch.commit()
                .addOnSuccessListener {
                    progressBar.isVisible = false
                    dialog.dismiss()
                    Toast.makeText(
                        context,
                        context.getString(R.string.cita_registrada_exito),
                        Toast.LENGTH_SHORT
                    ).show()
                    callback?.onTutoriaUpdated(tutoria.id, "Citado")
                }
                .addOnFailureListener { e ->
                    progressBar.isVisible = false
                    button.isEnabled = true
                    Toast.makeText(
                        context,
                        context.getString(R.string.error_registro_cita, e.localizedMessage),
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }

        private fun obtenerFechaActual(): String {
            return SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                .format(Date())
        }
    }
}