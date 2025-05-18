package com.example.educonnet.ui.incidencia

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.educonnet.LoginActivity
import com.example.educonnet.R
import com.example.educonnet.databinding.ActivityAgregarIncidenciaBinding
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

class AgregarIncidencia : AppCompatActivity() {
    private lateinit var takePhotoLauncher: ActivityResultLauncher<Intent>

    private lateinit var binding: ActivityAgregarIncidenciaBinding
    private lateinit var studentId: String
    private lateinit var studentName: String
    private lateinit var studentLastName: String
    private lateinit var auth: FirebaseAuth
    private lateinit var spinnerAtencion: Spinner
    private lateinit var hora: TextView
    private lateinit var fecha: TextView
    private lateinit var edMultilinea: EditText
    private lateinit var spinnerTipo: Spinner
    private var studentGrade: Int = 0
    private lateinit var studentNivel: String
    private var studentCelular: Int = 0
    private lateinit var estudiante: TextView
    private var imageUri: Uri? = null
    private lateinit var storageRef: StorageReference
    private lateinit var imageViewEvidencia: ImageView
    private lateinit var pickImageLauncher: ActivityResultLauncher<Intent>
    private lateinit var idUsuario:String
    private lateinit var datoTipoUsuario:String

    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAgregarIncidenciaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        storageRef = FirebaseStorage.getInstance().reference
        init()
        set()
        selectedImagen()
        checkPermissions()
        selectedRegistrar()
    }

    private fun init() {
        studentId = intent.getStringExtra("EXTRA_STUDENT_ID").toString()
        studentName = intent.getStringExtra("EXTRA_STUDENT_NAME") ?: "N/A"
        studentLastName = intent.getStringExtra("EXTRA_STUDENT_LAST_NAME") ?: "N/A"
        studentGrade = intent.getIntExtra("EXTRA_STUDENT_GRADE", 0)
        studentNivel = intent.getStringExtra("EXTRA_STUDENT_SECTION") ?: "N/A"
        studentCelular = intent.getIntExtra("EXTRA_STUDENT_CELULAR",0)
        estudiante = findViewById(R.id.tvEstudiante)
        spinnerAtencion = findViewById(R.id.spinnerAtencion)
        spinnerTipo = findViewById(R.id.spinnerTipo)
        hora = findViewById(R.id.tvHora)
        fecha = findViewById(R.id.tvFecha)
        edMultilinea = findViewById(R.id.edMultilinea)
        imageViewEvidencia = findViewById(R.id.imageViewEvidencia)

    }

    private fun set() {
        estudiante.text = "$studentLastName $studentName"
        val atencion = arrayOf("Moderado", "Urgente", "Muy urgente")
        val adapterAtencion = ArrayAdapter(this, R.layout.item_spinner, atencion)
        adapterAtencion.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinnerAtencion.adapter = adapterAtencion

        val tipo = arrayOf("Conductual", "Académicas", "Otros")
        val adapterTipo = ArrayAdapter(this, R.layout.item_spinner, tipo)
        adapterTipo.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinnerTipo.adapter = adapterTipo
        fecha.text = obtenerFechaActual()
        hora.text = obtenerHoraActual()
    }

    private fun selectedImagen() {
        pickImageLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.data?.let {
                    imageUri = it
                    Glide.with(this)
                        .load(imageUri)
                        .apply(RequestOptions().centerCrop())
                        .into(imageViewEvidencia)
                }
            }
        }

        takePhotoLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val imageBitmap = result.data?.extras?.get("data") as? Bitmap
                imageBitmap?.let {
                    val imageUri = Uri.parse(MediaStore.Images.Media.insertImage(contentResolver, it, "Title", null))
                    this.imageUri = imageUri
                    Glide.with(this)
                        .load(imageUri)
                        .apply(RequestOptions().centerCrop())
                        .into(imageViewEvidencia)
                }
            }
        }

        // Configuración del clic en la vista de imagen
        imageViewEvidencia.setOnClickListener {
            val options = arrayOf("Seleccionar imagen", "Tomar foto")
            AlertDialog.Builder(this)
                .setTitle("Elegir opción")
                .setItems(options) { _, which ->
                    when (which) {
                        0 -> { // Opción para seleccionar imagen
                            val intent = Intent(Intent.ACTION_PICK)
                            intent.type = "image/*"
                            pickImageLauncher.launch(intent)
                        }
                        1 -> { // Opción para tomar foto
                            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                            if (takePictureIntent.resolveActivity(packageManager) != null) {
                                takePhotoLauncher.launch(takePictureIntent)
                            }
                        }
                    }
                }
                .show()
        }
    }

    private fun obtenerHoraActual(): String {
        val formatoHora = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        formatoHora.timeZone = TimeZone.getDefault()
        return formatoHora.format(Date())
    }

    private fun selectedRegistrar() {
        binding.btnRegistrarIncidencia.setOnClickListener {
            val detalles = edMultilinea.text.toString().trim()
            if (detalles.isEmpty()) {
                Toast.makeText(this, "Incluye detalles de la incidencia", Toast.LENGTH_SHORT).show()
                binding.btnRegistrarIncidencia.isEnabled = true
                return@setOnClickListener
            }
            binding.btnRegistrarIncidencia.isEnabled = false
            if (imageUri != null) {
                subirImagen(imageUri!!)
            } else {
                guardarDatosIncidencia(null)
            }
        }
    }

    private fun obtenerFechaActual(): String {
        val formatoFecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        formatoFecha.timeZone = TimeZone.getDefault()
        return formatoFecha.format(Date())
    }

    private fun subirImagen(imageUri: Uri) {

        val ref = storageRef.child("incidencias/${UUID.randomUUID()}")
        ref.putFile(imageUri).continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            ref.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                guardarDatosIncidencia(downloadUri.toString())
            } else {
                Toast.makeText(this, "Error al obtener la URL de la imagen", Toast.LENGTH_SHORT)
                    .show()
                guardarDatosIncidencia("")
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Error al subir la imagen", Toast.LENGTH_SHORT).show()
            guardarDatosIncidencia("")
        }

    }

    private fun guardarDatosIncidencia(urlImagen: String?) {
        idUsuario = LoginActivity.GlobalData.idUsuario
        datoTipoUsuario = LoginActivity.GlobalData.datoTipoUsuario
        val estado = "Pendiente"

        if (datoTipoUsuario == "Administrador") {
            // Si es Administrador, el ID de usuario no es necesario
            val incidencia = hashMapOf(
                "fecha" to obtenerFechaActual(),
                "hora" to obtenerHoraActual(),
                "idProfesor" to idUsuario,  // O puedes usar "" si prefieres no tener un campo nulo
                "nombreEstudiante" to studentName,
                "apellidoEstudiante" to studentLastName,
                "grado" to studentGrade,
                "nivel" to studentNivel,
                "celularApoderado" to studentCelular,
                "estado" to estado,
                "cargo" to null,
                "atencion" to spinnerAtencion.selectedItem.toString(),
                "tipo" to spinnerTipo.selectedItem.toString(),
                "detalle" to edMultilinea.text.toString(),
                "apellidoProfesor" to "",
                "nombreProfesor" to "Director",
                "urlImagen" to urlImagen
            )

            firestore.collection("Incidencia")
                .add(incidencia)
                .addOnSuccessListener {
                    incrementarCantidadIncidencia()
                    Toast.makeText(this, "Incidencia registrada exitosamente", Toast.LENGTH_SHORT).show()
                    finish()
                    binding.btnRegistrarIncidencia.isEnabled = true
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al registrar la incidencia", Toast.LENGTH_SHORT).show()
                    binding.btnRegistrarIncidencia.isEnabled = true
                }

        } else {
            // Si no es Administrador, obtén los datos del profesor normalmente
            firestore.collection("Profesor")
                .document(idUsuario)
                .get()
                .addOnSuccessListener { documents ->
                    if (documents.exists()) {
                        val nombresProfesor = documents.getString("nombres") ?: "Nombres no encontrados"
                        val apellidosProfesor = documents.getString("apellidos") ?: "Apellidos no encontrados"
                        val cargo = documents.getString("cargo") ?: "Cargo no encontrados"
                        val incidencia = hashMapOf(
                            "fecha" to obtenerFechaActual(),
                            "hora" to obtenerHoraActual(),
                            "idProfesor" to idUsuario,
                            "nombreEstudiante" to studentName,
                            "apellidoEstudiante" to studentLastName,
                            "grado" to studentGrade,
                            "nivel" to studentNivel,
                            "celularApoderado" to studentCelular,
                            "estado" to estado,
                            "atencion" to spinnerAtencion.selectedItem.toString(),
                            "tipo" to spinnerTipo.selectedItem.toString(),
                            "detalle" to edMultilinea.text.toString(),
                            "apellidoProfesor" to apellidosProfesor,
                            "nombreProfesor" to nombresProfesor,
                            "cargo" to cargo,
                            "urlImagen" to urlImagen
                        )

                        firestore.collection("Incidencia")
                            .add(incidencia)
                            .addOnSuccessListener {
                                incrementarCantidadIncidencia()
                                Toast.makeText(this, "Incidencia registrada exitosamente", Toast.LENGTH_SHORT).show()
                                finish()
                                binding.btnRegistrarIncidencia.isEnabled = true
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Error al registrar la incidencia", Toast.LENGTH_SHORT).show()
                                binding.btnRegistrarIncidencia.isEnabled = true
                            }
                    } else {
                        Toast.makeText(
                            this,
                            "No se encontró el profesor con el correo proporcionado.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(
                        this,
                        "Error al obtener datos del profesor: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }


    private fun incrementarCantidadIncidencia() {
        val studentRef = firestore.collection("Estudiante").document(studentId)

        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(studentRef)
            val currentCount = snapshot.getLong("cantidadIncidencias") ?: 0
            transaction.update(studentRef, "cantidadIncidencias", currentCount + 1)
        }.addOnSuccessListener {
            finish()
        }.addOnFailureListener { e ->
            Toast.makeText(
                this,
                "Error al actualizar contador de incidencias: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_CODE_PERMISSIONS)
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido
            } else {
                // Permiso denegado
                Toast.makeText(this, "Permiso necesario para usar la cámara", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 1001
    }

}