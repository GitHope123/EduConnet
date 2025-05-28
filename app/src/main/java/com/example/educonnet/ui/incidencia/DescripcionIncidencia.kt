package com.example.educonnet.ui.incidencia

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.educonnet.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class DescripcionIncidencia : AppCompatActivity() {

    // Datos de la incidencia
    private lateinit var id: String
    private lateinit var fecha: String
    private lateinit var hora: String
    private lateinit var nombreEstudiante: String
    private lateinit var apellidoEstudiante: String
    private lateinit var seccion: String
    private lateinit var tipo: String
    private lateinit var atencion: String
    private lateinit var estado: String
    private lateinit var detalle: String
    private lateinit var imageUri: String
    private var grado: Int = 0

    // Views
    private lateinit var tvFecha: TextView
    private lateinit var tvHora: TextView
    private lateinit var tvNombreCompleto: TextView
    private lateinit var tvGrado: TextView
    private lateinit var tvSeccion: TextView
    private lateinit var tvEstado: TextView
    private lateinit var tvTipo: TextView
    private lateinit var tvAtencion: TextView
    private lateinit var tvDetalle: TextView
    private lateinit var imagen: ImageView
    private lateinit var btnEliminar: MaterialButton

    // Firebase
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_descripcion_incidencia)

        obtenerDatosIntent()
        inicializarViews()
        configurarVistas()
        configurarBotonEliminar()
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun obtenerDatosIntent() {
        with(intent) {
            id = getStringExtra("INCIDENCIA_ID").orEmpty()
            fecha = getStringExtra("INCIDENCIA_FECHA").orEmpty()
            hora = getStringExtra("INCIDENCIA_HORA").orEmpty()
            nombreEstudiante = getStringExtra("INCIDENCIA_NOMBRE").orEmpty()
            apellidoEstudiante = getStringExtra("INCIDENCIA_APELLIDO").orEmpty()
            grado = getIntExtra("INCIDENCIA_GRADO", 0)
            seccion = getStringExtra("INCIDENCIA_SECCION").orEmpty()
            tipo = getStringExtra("INCIDENCIA_TIPO").orEmpty()
            atencion = getStringExtra("INCIDENCIA_ATENCION").orEmpty()
            estado = getStringExtra("INCIDENCIA_ESTADO").orEmpty()
            detalle = getStringExtra("INCIDENCIA_DETALLE").orEmpty()
            imageUri = getStringExtra("INCIDENCIA_FOTO_URL").orEmpty()
        }
    }

    private fun inicializarViews() {
        tvFecha = findViewById(R.id.tvFecha)
        tvHora = findViewById(R.id.tvHora)
        tvNombreCompleto = findViewById(R.id.tvNombreCompleto)
        tvGrado = findViewById(R.id.tvGrado)
        tvSeccion= findViewById(R.id.tvNivel)
        tvEstado = findViewById(R.id.tvEstado)
        tvTipo = findViewById(R.id.tvTipo)
        tvAtencion = findViewById(R.id.tvAtencion)
        tvDetalle = findViewById(R.id.tvDetalle)
        imagen = findViewById(R.id.imagen)
        btnEliminar = findViewById(R.id.eliminarIncidencia)
    }

    private fun configurarVistas() {
        // Configurar textos
        tvFecha.text = fecha
        tvHora.text = hora
        tvNombreCompleto.text = getString(R.string.full_name_format, apellidoEstudiante, nombreEstudiante)
        tvGrado.text = getString(R.string.grade_format, grado)
        tvSeccion.text = getString(R.string.section_format, seccion)
        tvEstado.text = getString(R.string.status_format, estado)
        tvTipo.text = getString(R.string.type_format, tipo)

        val textoAtencion = if (atencion.isBlank()) {
            getString(R.string.attention_format, getString(R.string.attention_default))
        } else {
            getString(R.string.attention_format, atencion)
        }

        tvAtencion.text = textoAtencion


        tvDetalle.text = detalle

        // Configurar imagen
        if (imageUri.isNotEmpty()) {
            Glide.with(this)
                .load(imageUri)
                .apply(RequestOptions().centerCrop())
                .placeholder(R.drawable.ic_menu_gallery)
                .error(R.drawable.ic_camera)
                .into(imagen)
        } else {
            imagen.setImageResource(R.drawable.ic_menu_camera)
        }
    }

    private fun configurarBotonEliminar() {
        btnEliminar.visibility = if (estado == "Pendiente"||estado == "Notificado" || estado=="Citado" || estado =="Completado") View.VISIBLE else View.GONE
        btnEliminar.setOnClickListener { confirmarEliminacion() }
    }

    private fun confirmarEliminacion() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.delete_confirmation_title)
            .setMessage(R.string.delete_confirmation_message)
            .setPositiveButton(R.string.delete) { _, _ -> eliminarIncidencia() }
            .setNegativeButton(R.string.cancel, null)
            .setCancelable(true)  // Permite cerrar tocando fuera del diálogo
            .show()
    }


    private fun eliminarIncidencia() {
        if (id.isEmpty() || nombreEstudiante.isEmpty() || apellidoEstudiante.isEmpty()) {
            mostrarError(getString(R.string.invalid_incident_data))
            return
        }

        firestore.collection("Incidencia")
            .document(id)
            .delete()
            .addOnSuccessListener {
                actualizarConteoIncidenciasEstudiante()
                mostrarMensaje(getString(R.string.incident_deleted))
            }
            .addOnFailureListener { e ->
                mostrarError(getString(R.string.delete_error, e.message))
            }
    }

    private fun actualizarConteoIncidenciasEstudiante() {
        firestore.collection("Estudiante")
            .whereEqualTo("nombres", nombreEstudiante)
            .whereEqualTo("apellidos", apellidoEstudiante)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) {
                    mostrarError(getString(R.string.student_not_found))
                    return@addOnSuccessListener
                }

                val estudianteDocument = querySnapshot.documents.first()
                val idEstudiante = estudianteDocument.id

                firestore.collection("Estudiante")
                    .document(idEstudiante)
                    .update("cantidadIncidencias", FieldValue.increment(-1))
                    .addOnSuccessListener {
                        finish()
                    }
                    .addOnFailureListener { e ->
                        mostrarError(getString(R.string.update_count_error, e.message))
                    }
            }
            .addOnFailureListener { e ->
                mostrarError(getString(R.string.search_student_error, e.message))
            }
    }

    private fun mostrarMensaje(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }

    private fun mostrarError(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }
}