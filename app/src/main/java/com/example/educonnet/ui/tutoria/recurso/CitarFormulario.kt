package com.example.educonnet.ui.tutoria.recurso

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import com.example.educonnet.R
import com.example.educonnet.ui.tutoria.TutoriaClass
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

/**
 * Clase para manejar el formulario de citación de tutorías
 * Implementa patrón Singleton para control de instancias
 */
class CitarFormulario private constructor() {

    companion object {
        private val idProfesor = com.example.educonnet.LoginActivity.GlobalData.idUsuario

        // Formatos de fecha y hora
        private val DATE_FORMAT = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        private val TIME_FORMAT_24H = SimpleDateFormat("HH:mm", Locale.getDefault())
        private val TIME_FORMAT_12H = SimpleDateFormat("hh:mm a", Locale.getDefault())
        private val TIMESTAMP_FORMAT = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

        /**
         * Muestra el formulario de citación
         */
        fun mostrar(
            context: Context,
            tutoria: TutoriaClass,
            callback: TutoriaUpdateCallback? = null
        ) {
            val view = LayoutInflater.from(context).inflate(R.layout.dialog_cita_form, null)
            val dialog = crearDialog(context, view)

            inicializarComponentes(context, view, dialog, tutoria, callback)
            dialog.show()
        }

        /**
         * Crea el dialog principal
         */
        private fun crearDialog(context: Context, view: View): AlertDialog {
            return MaterialAlertDialogBuilder(context)
                .setTitle(context.getString(R.string.dialog_citar_titulo))
                .setView(view)
                .setNegativeButton(context.getString(R.string.btn_cancelar)) { dialog, _ ->
                    dialog.dismiss()
                }
                .setCancelable(false)
                .create()
        }

        /**
         * Inicializa todos los componentes del formulario
         */
        private fun inicializarComponentes(
            context: Context,
            view: View,
            dialog: AlertDialog,
            tutoria: TutoriaClass,
            callback: TutoriaUpdateCallback?
        ) {
            // Referencias a los componentes
            val editTextApoderado = view.findViewById<EditText>(R.id.editTextApoderado)
            val editTextFecha = view.findViewById<EditText>(R.id.editTextFecha)
            val editTextHora = view.findViewById<EditText>(R.id.editTextHora)
            val spinnerParentesco = view.findViewById<Spinner>(R.id.SpinnerParentesco)
            val checkBox = view.findViewById<MaterialCheckBox>(R.id.confirm_checkbox_cita)
            val btnEnviar = view.findViewById<MaterialButton>(R.id.btn_enviar_cita)
            val btnCancelar = view.findViewById<MaterialButton>(R.id.btn_cancelar_cita)
            val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)

            // Configuraciones iniciales
            configurarSpinner(context, spinnerParentesco)
            configurarPickers(context, editTextFecha, editTextHora)
            configurarValidacion(checkBox, btnEnviar)
            configurarBotones(
                context, btnEnviar, btnCancelar, progressBar, dialog,
                callback, editTextApoderado, editTextFecha, editTextHora,
                spinnerParentesco, tutoria
            )
        }

        /**
         * Configura el spinner de parentesco
         */
        private fun configurarSpinner(context: Context, spinner: Spinner) {
            val parentescos = context.resources.getStringArray(R.array.parentesco_array)
            val adapter = ArrayAdapter(context, R.layout.spinner_item_general, parentescos)
            adapter.setDropDownViewResource(R.layout.simple_spinner_general)
            spinner.adapter = adapter
        }

        /**
         * Configura los date y time pickers
         */
        private fun configurarPickers(context: Context, editTextFecha: EditText, editTextHora: EditText) {
            val calendar = Calendar.getInstance()

            configurarDatePicker(context, editTextFecha, calendar)
            configurarTimePicker(context, editTextHora, calendar)
        }

        /**
         * Configura el selector de fecha
         */
        private fun configurarDatePicker(context: Context, editText: EditText, calendar: Calendar) {
            editText.setOnClickListener {
                val datePickerDialog = DatePickerDialog(
                    context,
                    { _, year, month, day ->
                        calendar.set(year, month, day)
                        editText.setText(DATE_FORMAT.format(calendar.time))
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                )

                // Establecer fecha mínima (hoy)
                datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000

                // Establecer fecha máxima (6 meses desde hoy)
                val maxCalendar = Calendar.getInstance()
                maxCalendar.add(Calendar.MONTH, 6)
                datePickerDialog.datePicker.maxDate = maxCalendar.timeInMillis

                datePickerDialog.show()
            }
        }

        /**
         * Configura el selector de hora con formato AM/PM
         */
        private fun configurarTimePicker(context: Context, editText: EditText, calendar: Calendar) {
            editText.setOnClickListener {
                // Determinar si usar formato 12 o 24 horas basado en la configuración del sistema
                val is24HourFormat = android.text.format.DateFormat.is24HourFormat(context)

                val timePicker = TimePickerDialog(
                    context,
                    { _, hour, minute ->
                        // Validar horario de atención (8:00 AM - 11:00 PM)
                        if (hour < 8 || hour >= 23) {
                            val horarioMensaje = if (is24HourFormat) {
                                "Horario de atención: 08:00 - 23:00"
                            } else {
                                "Horario de atención: 8:00 AM - 11:00 PM"
                            }
                            Toast.makeText(context, horarioMensaje, Toast.LENGTH_SHORT).show()
                            return@TimePickerDialog
                        }

                        calendar.set(Calendar.HOUR_OF_DAY, hour)
                        calendar.set(Calendar.MINUTE, minute)

                        // Formatear según la preferencia del sistema
                        val timeFormat = if (is24HourFormat) TIME_FORMAT_24H else TIME_FORMAT_12H
                        editText.setText(timeFormat.format(calendar.time))
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    is24HourFormat // Usar formato basado en configuración del sistema
                )

                timePicker.show()
            }
        }

        /**
         * Configura la validación del checkbox
         */
        private fun configurarValidacion(checkBox: MaterialCheckBox, btnEnviar: MaterialButton) {
            btnEnviar.isEnabled = false
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                btnEnviar.isEnabled = isChecked
            }
        }

        /**
         * Configura los botones de acción
         */
        private fun configurarBotones(
            context: Context,
            btnEnviar: MaterialButton,
            btnCancelar: MaterialButton,
            progressBar: ProgressBar,
            dialog: AlertDialog,
            callback: TutoriaUpdateCallback?,
            editTextApoderado: EditText,
            editTextFecha: EditText,
            editTextHora: EditText,
            spinnerParentesco: Spinner,
            tutoria: TutoriaClass
        ) {
            // Botón cancelar
            btnCancelar.setOnClickListener {
                dialog.dismiss()
            }

            // Botón enviar
            btnEnviar.setOnClickListener {
                procesarEnvio(
                    context, progressBar, btnEnviar, dialog, callback,
                    editTextApoderado, editTextFecha, editTextHora,
                    spinnerParentesco, tutoria
                )
            }
        }

        /**
         * Procesa el envío del formulario
         */
        private fun procesarEnvio(
            context: Context,
            progressBar: ProgressBar,
            btnEnviar: MaterialButton,
            dialog: AlertDialog,
            callback: TutoriaUpdateCallback?,
            editTextApoderado: EditText,
            editTextFecha: EditText,
            editTextHora: EditText,
            spinnerParentesco: Spinner,
            tutoria: TutoriaClass
        ) {
            val apoderado = editTextApoderado.text.toString().trim()
            val fecha = editTextFecha.text.toString().trim()
            val hora = editTextHora.text.toString().trim()
            val parentesco = spinnerParentesco.selectedItem.toString()

            if (!validarCampos(context, apoderado, fecha, hora, parentesco)) {
                return
            }

            mostrarCargando(progressBar, btnEnviar, true)

            val cita = crearCita(tutoria, apoderado, fecha, hora, parentesco)
            guardarCitaEnFirestore(
                context, cita, tutoria, progressBar, btnEnviar, dialog, callback
            )
        }

        /**
         * Valida todos los campos del formulario
         */
        private fun validarCampos(
            context: Context,
            apoderado: String,
            fecha: String,
            hora: String,
            parentesco: String
        ): Boolean {
            when {
                apoderado.isEmpty() -> {
                    mostrarError(context, "El nombre del apoderado es obligatorio")
                    return false
                }
                apoderado.length < 3 -> {
                    mostrarError(context, "El nombre debe tener al menos 3 caracteres")
                    return false
                }
                fecha.isEmpty() -> {
                    mostrarError(context, "La fecha es obligatoria")
                    return false
                }
                hora.isEmpty() -> {
                    mostrarError(context, "La hora es obligatoria")
                    return false
                }
                parentesco.isEmpty() -> {
                    mostrarError(context, "Debe seleccionar el parentesco")
                    return false
                }
            }
            return true
        }

        /**
         * Crea el objeto Cita con todos los datos
         */
        private fun crearCita(
            tutoria: TutoriaClass,
            apoderado: String,
            fecha: String,
            hora: String,
            parentesco: String
        ): Cita {
            return Cita(
                idCita = tutoria.id,
                idProfesor = idProfesor,
                detalle = tutoria.detalle,
                createFecha = obtenerFechaActual(),
                apoderado = apoderado,
                fechaCita = fecha,
                hora = hora,
                parentesco = parentesco
            )
        }

        /**
         * Guarda la cita en Firestore usando transacción
         */
        private fun guardarCitaEnFirestore(
            context: Context,
            cita: Cita,
            tutoria: TutoriaClass,
            progressBar: ProgressBar,
            btnEnviar: MaterialButton,
            dialog: AlertDialog,
            callback: TutoriaUpdateCallback?
        ) {
            val db = FirebaseFirestore.getInstance()

            db.runTransaction { transaction ->
                val citaRef = db.collection("Cita").document(cita.idCita)
                val tutoriaRef = db.collection("Incidencia").document(tutoria.id)

                // Verificar que la tutoría aún existe y no ha sido citada
                val tutoriaSnapshot = transaction.get(tutoriaRef)
                if (!tutoriaSnapshot.exists()) {
                    throw Exception("La tutoría ya no existe")
                }

                val estadoActual = tutoriaSnapshot.getString("estado")
                if (estadoActual == "Citado") {
                    throw Exception("Esta tutoría ya fue citada")
                }

                // Realizar las operaciones
                transaction.set(citaRef, cita)
                transaction.update(tutoriaRef, "estado", "Citado")

                null // Transacción exitosa
            }
                .addOnSuccessListener {
                    mostrarCargando(progressBar, btnEnviar, false)
                    dialog.dismiss()
                    mostrarExito(context, "Cita registrada exitosamente")
                    callback?.onTutoriaUpdated(tutoria.id, "Citado")
                }
                .addOnFailureListener { exception ->
                    mostrarCargando(progressBar, btnEnviar, false)
                    val mensaje = when {
                        exception.message?.contains("no existe") == true ->
                            "La tutoría ya no está disponible"
                        exception.message?.contains("ya fue citada") == true ->
                            "Esta tutoría ya fue citada"
                        else -> "Error al registrar la cita: ${exception.localizedMessage}"
                    }
                    mostrarError(context, mensaje)
                }
        }

        /**
         * Controla el estado de carga
         */
        private fun mostrarCargando(progressBar: ProgressBar, btnEnviar: MaterialButton, mostrar: Boolean) {
            progressBar.isVisible = mostrar
            btnEnviar.isEnabled = !mostrar
        }

        /**
         * Muestra mensaje de error
         */
        private fun mostrarError(context: Context, mensaje: String) {
            Toast.makeText(context, mensaje, Toast.LENGTH_LONG).show()
        }

        /**
         * Muestra mensaje de éxito
         */
        private fun mostrarExito(context: Context, mensaje: String) {
            Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
        }

        /**
         * Obtiene la fecha actual formateada
         */
        private fun obtenerFechaActual(): String {
            return TIMESTAMP_FORMAT.format(Date())
        }
    }
}