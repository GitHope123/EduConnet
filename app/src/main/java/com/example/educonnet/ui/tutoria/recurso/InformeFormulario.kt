package com.example.educonnet.ui.tutoria.recurso

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import com.example.educonnet.R
import com.example.educonnet.databinding.DialogInformeFormBinding
import com.example.educonnet.ui.tutoria.TutoriaClass
import com.google.firebase.firestore.FirebaseFirestore
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.*

class InformeFormulario private constructor() {

    companion object {
        fun mostrar(
            context: Context,
            tutoria: TutoriaClass,
            citaData: Cita? = null,
            callback: TutoriaUpdateCallback? = null
        ) {
            val weakContext = WeakReference(context)
            val binding = DialogInformeFormBinding.inflate(LayoutInflater.from(context))
            val db = FirebaseFirestore.getInstance()

            val dialog = AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.titulo_informe))
                .setView(binding.root)
                .setNegativeButton(context.getString(R.string.btn_cancelar)) { d, _ -> d.dismiss() }
                .create()

            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val calendar = Calendar.getInstance()

            with(binding) {
                // Configuración inicial de fecha y hora
                editTextFecha.setText(dateFormat.format(calendar.time))
                editTextHora.setText(timeFormat.format(calendar.time))

                // Cargar datos de la cita si existen
                citaData?.let {
                    textViewApoderado.setText(it.apoderado)
                    textViewParentesco.setText(it.parentesco)
                } ?: run {
                    cargarInformacionBasicaCita(db, tutoria.id, binding)
                }

                // Selectores de fecha y hora
                editTextFecha.setOnClickListener {
                    showDatePicker(weakContext.get(), editTextFecha, calendar, dateFormat)
                }

                editTextHora.setOnClickListener {
                    showTimePicker(weakContext.get(), editTextHora, calendar, timeFormat)
                }

                // Validación al perder foco
                editTextDetalles.setOnFocusChangeListener { _, hasFocus ->
                    if (!hasFocus) validarDetalles(context, binding)
                }

                // Botones
                btnCancelarInforme.setOnClickListener { dialog.dismiss() }

                btnEnviarInforme.setOnClickListener {
                    if (!validarFormulario(weakContext.get(), binding)) return@setOnClickListener

                    manejarEnvioFormulario(
                        weakContext = weakContext,
                        binding = binding,
                        dialog = dialog,
                        tutoria = tutoria,
                        db = db,
                        callback = callback
                    )
                }
            }

            dialog.show()
        }

        private fun cargarInformacionBasicaCita(
            db: FirebaseFirestore,
            idCita: String,
            binding: DialogInformeFormBinding
        ) {
            db.collection("Cita")
                .document(idCita)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    val cita = documentSnapshot.toObject(Cita::class.java)
                    cita?.let {
                        binding.textViewApoderado.setText(it.apoderado)
                        binding.textViewParentesco.setText(it.parentesco)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("CitaInfo", "Error al obtener la cita: ${e.localizedMessage}")
                }
        }

        private fun showDatePicker(
            context: Context?,
            editText: EditText,
            calendar: Calendar,
            dateFormat: SimpleDateFormat
        ) {
            context ?: return
            DatePickerDialog(
                context,
                { _, year, month, day ->
                    calendar.set(year, month, day)
                    editText.setText(dateFormat.format(calendar.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).apply {
                datePicker.minDate = System.currentTimeMillis() - 1000
                show()
            }
        }

        private fun showTimePicker(
            context: Context?,
            editText: EditText,
            calendar: Calendar,
            timeFormat: SimpleDateFormat
        ) {
            context ?: return
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

        private fun validarFormulario(
            context: Context?,
            binding: DialogInformeFormBinding
        ): Boolean {
            context ?: return false
            return validarDetalles(context, binding)
        }

        private fun validarDetalles(
            context: Context?,
            binding: DialogInformeFormBinding
        ): Boolean {
            context ?: return false
            val detalles = binding.editTextDetalles.text.toString().trim()

            return when {
                detalles.isEmpty() -> {
                    binding.textInputDetalles.error =
                        context.getString(R.string.error_detalles_vacios)
                    false
                }

                detalles.length < 10 -> {
                    binding.textInputDetalles.error =
                        context.getString(R.string.error_detalles_cortos)
                    false
                }

                else -> {
                    binding.textInputDetalles.error = null
                    true
                }
            }
        }

        private fun manejarEnvioFormulario(
            weakContext: WeakReference<Context>,
            binding: DialogInformeFormBinding,
            dialog: AlertDialog,
            tutoria: TutoriaClass,
            db: FirebaseFirestore,
            callback: TutoriaUpdateCallback?
        ) {
            val context = weakContext.get() ?: return

            binding.apply {
                progressBar.isVisible = true
                btnEnviarInforme.isEnabled = false
                btnCancelarInforme.isEnabled = false
            }

            val informe = crearInforme(
                tutoria = tutoria,
                fecha = binding.editTextFecha.text.toString(),
                hora = binding.editTextHora.text.toString(),
                detalles = binding.editTextDetalles.text.toString().trim(),
                apoderado = binding.textViewApoderado.text.toString(),
                parentesco = binding.textViewParentesco.text.toString()
            )

            guardarInforme(
                context = context,
                db = db,
                informe = informe,
                tutoria = tutoria,
                binding = binding,
                dialog = dialog,
                callback = callback
            )
        }

        private fun crearInforme(
            tutoria: TutoriaClass,
            fecha: String,
            hora: String,
            detalles: String,
            apoderado: String,
            parentesco: String
        ) = Informe(
            idInforme = tutoria.id, // se debe guardar con el mismo id para poder adjuntar con tutoria
            createFecha = fecha,
            createHora = hora,
            detalles = detalles,
            apoderado = apoderado,
            relacionFamiliar = parentesco
        )

        private fun guardarInforme(
            context: Context,
            db: FirebaseFirestore,
            informe: Informe,
            tutoria: TutoriaClass,
            binding: DialogInformeFormBinding,
            dialog: AlertDialog,
            callback: TutoriaUpdateCallback?
        ) {
            val batch = db.batch()
            val informeRef = db.collection("Informe").document(informe.idInforme!!)
            val tutoriaRef = db.collection("Incidencia").document(tutoria.id)

            batch.set(informeRef, informe)
            if (tutoria.estado != "Completado") {
                batch.update(tutoriaRef, "estado", "Completado")
            }

            batch.commit()
                .addOnSuccessListener {
                    dialog.dismiss()
                    callback?.onTutoriaUpdated(tutoria.id, "Completado")
                    Toast.makeText(
                        context,
                        context.getString(R.string.informe_registrado_exito),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .addOnFailureListener { e ->
                    binding.run {
                        progressBar.isVisible = false
                        btnEnviarInforme.isEnabled = true
                        btnCancelarInforme.isEnabled = true
                    }
                    Toast.makeText(
                        context,
                        context.getString(R.string.error_registro_informe, e.localizedMessage),
                        Toast.LENGTH_LONG
                    ).show()
                }
        }
    }
}
