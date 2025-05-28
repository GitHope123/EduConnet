package com.example.educonnet.ui.incidencia

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.educonnet.LoginActivity
import com.example.educonnet.R
import com.example.educonnet.databinding.ActivityAgregarIncidenciaBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.text.SimpleDateFormat
import java.util.*

private typealias TakePhotoLauncher = ActivityResultLauncher<Intent>
private typealias PickImageLauncher = ActivityResultLauncher<Intent>

class AgregarIncidencia : AppCompatActivity() {

    private lateinit var binding: ActivityAgregarIncidenciaBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var storageRef: StorageReference

    private var imageUri: Uri? = null
    private lateinit var takePhotoLauncher: TakePhotoLauncher
    private lateinit var pickImageLauncher: PickImageLauncher

    private lateinit var idUsuario: String
    private lateinit var datoTipoUsuario: String

    private var incidenciasRegistradas = 0
    private var totalIncidencias = 0

    private val sugerenciasPositivas = listOf("Participativo", "Colaborativo", "Responsable")
    private val sugerenciasNegativas = listOf("Académica", "Conductual", "Otro")

    private val firestore by lazy { FirebaseFirestore.getInstance() }

    private lateinit var estudiantes: List<EstudianteAgregar>
    private lateinit var adapterEstudiantes: EstudiantesSeleccionadosAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAgregarIncidenciaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        storageRef = FirebaseStorage.getInstance().reference

        setupLaunchers()
        setupUI()
        setupRecyclerView()
        setupSpinners()
        setupListeners()
    }

    private fun setupLaunchers() {
        pickImageLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.data?.let {
                    imageUri = it
                    loadImage()
                }
            }
        }

        takePhotoLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val bitmap = result.data?.extras?.get("data") as? Bitmap
                bitmap?.let {
                    val uri = Uri.parse(MediaStore.Images.Media.insertImage(contentResolver, it, "Title", null))
                    imageUri = uri
                    loadImage()
                }
            }
        }
    }

    private fun loadImage() {
        Glide.with(this)
            .load(imageUri)
            .apply(RequestOptions().centerCrop())
            .into(binding.imageViewEvidencia)
    }

    private fun setupUI() {
        estudiantes = intent.extras?.getParcelableArrayList<EstudianteAgregar>("EXTRA_STUDENTS") ?: run {
            Log.w("AgregarIncidencia", "No se recibió lista de estudiantes o está vacía")
            emptyList()
        }

        totalIncidencias = estudiantes.size

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Registrar Incidencia"
        }
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.tvFecha.text = obtenerFechaActual()
        binding.tvHora.text = obtenerHoraActual()

        if (estudiantes.isEmpty()) {
            Toast.makeText(this, "No se recibieron estudiantes para registrar incidencia", Toast.LENGTH_LONG).show()
        } else {
            val cantidad = estudiantes.size
            val mensaje = if (cantidad == 1) {
                "Se recibió 1 estudiante para registrar incidencia"
            } else {
                "Se recibieron $cantidad estudiantes para registrar incidencia"
            }
            Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
            Log.i("AgregarIncidencia", mensaje)
        }
    }



    private fun setupRecyclerView() {
        adapterEstudiantes = EstudiantesSeleccionadosAdapter(estudiantes)
        binding.rvEstudiantesSeleccionados.apply {
            layoutManager = LinearLayoutManager(this@AgregarIncidencia)
            adapter = adapterEstudiantes
            setHasFixedSize(true)
        }
    }

    private fun setupSpinners() {
        // Spinner de tipo (Reconocimiento/Falta)
        ArrayAdapter.createFromResource(
            this,
            R.array.tipos_incidencia,
            R.layout.item_spinner
        ).also { adapter ->
            adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
            binding.spinnerTipo.adapter = adapter
        }

        binding.spinnerTipo.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                val tipoSeleccionado = parent?.getItemAtPosition(position).toString()
                binding.layoutGravedad.isVisible = tipoSeleccionado == "Falta"
                updateSugerencias(tipoSeleccionado)
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }

        // Spinner de atención (solo para faltas)
        ArrayAdapter.createFromResource(
            this,
            R.array.niveles_atencion,
            R.layout.item_spinner
        ).also { adapter ->
            adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
            binding.spinnerAtencion.adapter = adapter
        }
    }

    private fun updateSugerencias(tipo: String) {
        val sugerencias = if (tipo == "Reconocimiento") {
            sugerenciasPositivas
        } else {
            sugerenciasNegativas
        }

        ArrayAdapter(this, R.layout.item_spinner, sugerencias).also { adapter ->
            adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
            binding.spinnerSugerencias.adapter = adapter
        }
    }

    private fun setupListeners() {
        binding.imageViewEvidencia.setOnClickListener { showImagePickerDialog() }

        binding.btnRegistrarIncidencia.setOnClickListener {
            if (validarFormulario()) {
                binding.btnRegistrarIncidencia.isEnabled = false
                binding.progressBar.isVisible = true

                if (imageUri != null) {
                    subirImagen(imageUri!!)
                } else {
                    guardarDatosIncidencia(null)
                }
            }
        }
    }

    private fun validarFormulario(): Boolean {
        return when {
            binding.edMultilinea.text.toString().trim().isEmpty() -> {
                binding.edMultilinea.error = "Por favor ingrese los detalles"
                false
            }
            binding.spinnerTipo.selectedItem.toString() == "Falta" &&
                    binding.spinnerAtencion.selectedItemPosition == 0 -> {
                Toast.makeText(this, "Seleccione un nivel de atención", Toast.LENGTH_SHORT).show()
                false
            }
            else -> true
        }
    }

    private fun showImagePickerDialog() {
        AlertDialog.Builder(this)
            .setTitle("Seleccionar imagen")
            .setItems(R.array.opciones_imagen) { _, which ->
                when (which) {
                    0 -> seleccionarImagenGaleria()
                    1 -> tomarFotoCamara()
                }
            }
            .show()
    }

    private fun seleccionarImagenGaleria() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        pickImageLauncher.launch(intent)
    }

    private fun tomarFotoCamara() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            takePhotoLauncher.launch(takePictureIntent)
        } else {
            Toast.makeText(this, "No se encontró una aplicación de cámara", Toast.LENGTH_SHORT).show()
        }
    }

    private fun subirImagen(imageUri: Uri) {
        val ref = storageRef.child("incidencias/${UUID.randomUUID()}")
        ref.putFile(imageUri)
            .addOnProgressListener {
                val progress = (100.0 * it.bytesTransferred / it.totalByteCount).toInt()
                binding.progressBar.progress = progress
            }
            .continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let { throw it }
                }
                ref.downloadUrl
            }
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    guardarDatosIncidencia(task.result.toString())
                } else {
                    mostrarError("Error al obtener URL de la imagen")
                    guardarDatosIncidencia(null)
                }
            }
            .addOnFailureListener {
                mostrarError("Error al subir imagen")
                guardarDatosIncidencia(null)
            }
    }

    private fun guardarDatosIncidencia(urlImagen: String?) {
        idUsuario = LoginActivity.GlobalData.idUsuario
        datoTipoUsuario = LoginActivity.GlobalData.datoTipoUsuario

        val tipoIncidencia = binding.spinnerTipo.selectedItem.toString()
        val sugerencia = binding.spinnerSugerencias.selectedItem?.toString() ?: ""
        val detalles = binding.edMultilinea.text.toString().trim()
        val atencion = if (tipoIncidencia == "Falta") {
            binding.spinnerAtencion.selectedItem.toString()
        } else {
            ""
        }

        val detalleFinal = if (sugerencia.isNotEmpty()) "$sugerencia: $detalles" else detalles

        estudiantes.forEach { estudiante ->
            val incidencia = hashMapOf(
                "fecha" to obtenerFechaActual(),
                "hora" to obtenerHoraActual(),
                "idProfesor" to idUsuario,
                "estado" to "Pendiente",
                "atencion" to atencion,
                "tipo" to tipoIncidencia,
                "detalle" to detalleFinal,
                "urlImagen" to urlImagen,
                "nombreEstudiante" to estudiante.nombres,
                "apellidoEstudiante" to estudiante.apellidos,
                "grado" to estudiante.grado,
                "seccion" to estudiante.seccion,
                "celularApoderado" to estudiante.celularApoderado,
                "nombreProfesor" to if (datoTipoUsuario == "Administrador") "Director" else LoginActivity.GlobalData.nombresUsuario,
                "apellidoProfesor" to if (datoTipoUsuario == "Administrador") "" else LoginActivity.GlobalData.apellidosUsuario,
                "cargo" to if (datoTipoUsuario == "Administrador") "Administrador" else LoginActivity.GlobalData.cargoUsuario
            )

            firestore.collection("Incidencia").add(incidencia)
                .addOnSuccessListener {
                    actualizarContadorEstudiante(estudiante.id)
                    mostrarExitoSiCompletado()
                }
                .addOnFailureListener { e ->
                    Log.e("AgregarIncidencia", "Error al guardar incidencia", e)
                    mostrarError("Error al guardar incidencia")
                    binding.btnRegistrarIncidencia.isEnabled = true
                    binding.progressBar.isVisible = false
                }
        }
    }

    private fun actualizarContadorEstudiante(studentId: String) {
        firestore.collection("Estudiante").document(studentId)
            .update("cantidadIncidencias", FieldValue.increment(1))
            .addOnFailureListener { e ->
                Log.e("AgregarIncidencia", "Error al actualizar contador", e)
            }
    }

    private fun mostrarExitoSiCompletado() {
        incidenciasRegistradas++
        if (incidenciasRegistradas >= totalIncidencias) {
            Toast.makeText(this, "Incidencias registradas exitosamente", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun mostrarError(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }

    private fun obtenerFechaActual(): String {
        return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
    }

    private fun obtenerHoraActual(): String {
        return SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
    }
}