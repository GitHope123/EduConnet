package com.example.educonnet.ui.incidencia

import android.annotation.SuppressLint
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
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.educonnet.LoginActivity
import com.example.educonnet.R
import com.example.educonnet.databinding.ActivityAgregarIncidenciaBinding
import com.example.educonnet.databinding.ItemEstudianteSeleccionadoBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class AgregarIncidencia : AppCompatActivity() {

    private lateinit var binding: ActivityAgregarIncidenciaBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var storageRef: StorageReference
    private var imageUri: Uri? = null
    private lateinit var takePhotoLauncher: ActivityResultLauncher<Intent>
    private lateinit var pickImageLauncher: ActivityResultLauncher<Intent>
    private lateinit var idUsuario: String
    private lateinit var datoTipoUsuario: String
    private var incidenciasRegistradas = 0
    private var totalIncidencias = 0
    private val sugerenciasPositivas = listOf("Participativo", "Colaborativo", "Responsable")
    private val sugerenciasNegativas = listOf("Académica", "Conductual", "Otro")
    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val estudiantes = mutableListOf<EstudianteAgregar>()
    private lateinit var adapter: EstudiantesSeleccionadosAdapter

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
                    imageUri = saveBitmapToCache(it)
                    loadImage()
                }
            }
        }
    }

    private fun saveBitmapToCache(bitmap: Bitmap): Uri {
        val file = File(cacheDir, "temp_photo_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        return Uri.fromFile(file)
    }

    private fun loadImage() {
        Glide.with(this)
            .load(imageUri)
            .apply(RequestOptions().centerCrop())
            .into(binding.imageViewEvidencia)
    }

    @SuppressLint("SetTextI18n")
    private fun setupUI() {
        val estudiantesRecibidos = intent.extras?.getParcelableArrayList<EstudianteAgregar>("EXTRA_STUDENTS") ?: emptyList()
        estudiantes.clear()
        estudiantes.addAll(estudiantesRecibidos)
        totalIncidencias = estudiantes.size

        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Registrar Incidencia"
        }
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.tvFecha.text = obtenerFechaActual()
        binding.tvHora.text = obtenerHoraActual()

        binding.btnAddEstudianteIncidencia.setOnClickListener {
            finish() // Retrocede a la anterior
        }

        if (estudiantes.isEmpty()) {
            Toast.makeText(this, "No se recibieron estudiantes para registrar incidencia", Toast.LENGTH_LONG).show()
        } else {
            val cantidad = estudiantes.size
            val mensaje = if (cantidad == 1) "Se recibió 1 estudiante" else "Se recibieron $cantidad estudiantes"
            Toast.makeText(this, "$mensaje para registrar incidencia", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupRecyclerView() {
        adapter = EstudiantesSeleccionadosAdapter(estudiantes) { estudianteEliminado ->
            // Callback cuando se elimina un estudiante
            Toast.makeText(this, "Estudiante eliminado: ${estudianteEliminado.nombres}", Toast.LENGTH_SHORT).show()
        }

        binding.rvEstudiantesSeleccionados.apply {
            layoutManager = LinearLayoutManager(this@AgregarIncidencia)
            adapter = this@AgregarIncidencia.adapter
        }

        // Configura el swipe to delete directamente en el adaptador
        adapter.attachSwipeToDelete(binding.rvEstudiantesSeleccionados)
    }


    private fun setupSpinners() {
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
        val sugerencias = if (tipo == "Reconocimiento") sugerenciasPositivas else sugerenciasNegativas
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
            estudiantes.isEmpty() -> {
                Toast.makeText(this, "Seleccione al menos un estudiante", Toast.LENGTH_SHORT).show()
                false
            }
            binding.edMultilinea.text.toString().trim().isEmpty() -> {
                binding.edMultilinea.error = "Ingrese los detalles"
                false
            }
            binding.spinnerTipo.selectedItem.toString() == "Falta" &&
                    binding.spinnerAtencion.selectedItemPosition == 0 -> {
                Toast.makeText(this, "Seleccione nivel de atención", Toast.LENGTH_SHORT).show()
                false
            }
            else -> true
        }
    }

    private fun showImagePickerDialog() {
        if (checkAndRequestPermissions()) {
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
    }

    private fun checkAndRequestPermissions(): Boolean {
        val permissions = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                android.Manifest.permission.READ_MEDIA_IMAGES,
                android.Manifest.permission.CAMERA
            )
        } else {
            arrayOf(
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.CAMERA
            )
        }

        val permissionsToRequest = permissions.filter {
            checkSelfPermission(it) != android.content.pm.PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        if (permissionsToRequest.isNotEmpty()) {
            requestPermissions(permissionsToRequest, PERMISSIONS_REQUEST_CODE)
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (grantResults.all { it == android.content.pm.PackageManager.PERMISSION_GRANTED }) {
                showImagePickerDialog()
            } else {
                Toast.makeText(
                    this,
                    "Se requieren permisos para acceder a la galería y cámara",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun seleccionarImagenGaleria() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }

    private fun tomarFotoCamara() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager) != null) {
            takePhotoLauncher.launch(intent)
        } else {
            Toast.makeText(this, "No se encontró aplicación de cámara", Toast.LENGTH_SHORT).show()
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
                if (!task.isSuccessful) task.exception?.let { throw it }
                ref.downloadUrl
            }
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    guardarDatosIncidencia(task.result.toString())
                } else {
                    mostrarError("Error al subir imagen")
                    guardarDatosIncidencia(null)
                }
            }
            .addOnFailureListener {
                mostrarError("Error al subir imagen")
                guardarDatosIncidencia(null)
            }
    }

    @SuppressLint("SimpleDateFormat")
    private fun guardarDatosIncidencia(urlImagen: String?) {
        idUsuario = LoginActivity.GlobalData.idUsuario
        datoTipoUsuario = LoginActivity.GlobalData.datoTipoUsuario

        val tipoIncidencia = binding.spinnerTipo.selectedItem.toString()
        val sugerencia = binding.spinnerSugerencias.selectedItem?.toString() ?: ""
        val detalles = binding.edMultilinea.text.toString().trim()
        val atencion = if (tipoIncidencia == "Falta") binding.spinnerAtencion.selectedItem.toString() else ""

        val detalleFinal = if (sugerencia.isNotEmpty()) {
            val estudiantesSection = if (estudiantes.size > 1) {
                val listaEstudiantes = estudiantes.joinToString("\n• ", "• ") { estudiante ->
                    "${estudiante.nombres} ${estudiante.apellidos} (${estudiante.grado}° ${estudiante.seccion})"
                }
                "|ESTUDIANTES COLABORADORES:\n|$listaEstudiantes\n|\n"
            } else {
                ""
            }

            """
        $estudiantesSection
        |${sugerencia.uppercase()}:
        |$detalles
        """.trimMargin()
        } else {
            detalles
        }

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
            val intent = Intent(this, AgregarEstudiantes::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
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

    companion object {
        private const val PERMISSIONS_REQUEST_CODE = 100
    }
}