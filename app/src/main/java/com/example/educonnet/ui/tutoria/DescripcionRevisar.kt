package com.example.educonnet.ui.tutoria

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.bumptech.glide.Glide
import com.example.educonnet.R
import com.google.firebase.firestore.FirebaseFirestore
class DescripcionRevisar : AppCompatActivity() {
    private lateinit var tutoria: TutoriaClass
    private lateinit var tvNombreEstudiante: TextView
    private lateinit var tvRevisado: TextView
    private lateinit var tvFecha: TextView
    private lateinit var tvHora: TextView
    private lateinit var tvGrado: TextView
    private lateinit var tvSeccion: TextView
    private lateinit var tvProfesor: TextView
    private lateinit var tvEstado: TextView
    private lateinit var tvTipo: TextView
    private lateinit var tvCargo: TextView
    private lateinit var tvGravedad: TextView
    private lateinit var tvDetalle: TextView
    private lateinit var imagen: ImageView
    private lateinit var checkBoxRevisado: MaterialCheckBox
    private lateinit var btnEnviar: MaterialButton
    private lateinit var cardRevisarEnviar: MaterialCardView
    private lateinit var iconGravedad: ImageView
    private lateinit var linearView : View
    private lateinit var imageViewCard: MaterialCardView
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_descripcion_revisar)

        // Inicializar todas las vistas primero
        initViews()

        // Luego obtener los datos del intent
        getIntentData()

        // Configurar los datos en las vistas
        setData()

        // Configurar visibilidad según estado
        ocultar()

        // Configurar listeners
        setupListeners()
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun initViews() {
        tvNombreEstudiante = findViewById(R.id.tvNombreEstudiante)
        tvFecha = findViewById(R.id.tvFecha)
        tvHora = findViewById(R.id.tvHora)
        tvCargo = findViewById(R.id.tvCargo)
        checkBoxRevisado = findViewById(R.id.checkBoxRevisado)
        tvGrado = findViewById(R.id.tvGrado)
        tvSeccion = findViewById(R.id.tvNivel)
        tvEstado = findViewById(R.id.tvEstado)
        tvTipo = findViewById(R.id.tvTipo) // Asegúrate que este ID existe en tu XML
        tvGravedad = findViewById(R.id.tvGravedad)
        tvDetalle = findViewById(R.id.tvDetalle)
        imagen = findViewById(R.id.imagen)
        tvProfesor = findViewById(R.id.tvProfesor)
        tvRevisado = findViewById(R.id.tvRevisado)
        btnEnviar = findViewById(R.id.btnEnviar)
        cardRevisarEnviar = findViewById(R.id.cardRevisarEnviar)
        //ocultar esto cuando sea de tipo Reconocimiento
        iconGravedad=findViewById(R.id.iconGravedad)
        linearView=findViewById(R.id.viewLinear)
        // ocultar esto cuando no tenga imagen
        imageViewCard=findViewById(R.id.imageViewCard)
    }

    private fun getIntentData() {
        tutoria = intent.getSerializableExtra("TUTORIA_EXTRA") as TutoriaClass
    }

    private fun setData() {
        // Verificar que tutoria está inicializado
        if (!::tutoria.isInitialized) return

        // Configurar datos en las views
        tvNombreEstudiante.text = "${tutoria.nombreEstudiante} ${tutoria.apellidoEstudiante}"
        tvFecha.text = tutoria.fecha
        tvHora.text = tutoria.hora
        tvGrado.text = tutoria.grado.toString()
        tvSeccion.text = tutoria.seccion
        tvEstado.text = tutoria.estado
        tvTipo.text = tutoria.tipo ?: "No especificado" // Manejo de nulos
        tvCargo.text = tutoria.cargo ?: ""
        tvProfesor.text = "${tutoria.nombreProfesor} ${tutoria.apellidoProfesor}"
        tvGravedad.text = tutoria.atencion ?: ""
        tvDetalle.text = tutoria.detalle ?: ""

        tutoria.urlImagen?.let { uri ->
            Glide.with(this)
                .load(uri)
                .apply(com.bumptech.glide.request.RequestOptions().centerCrop())
                .into(imagen)
        } ?: run {
            imagen.setImageResource(R.drawable.placeholder_image) // Imagen por defecto
        }
        if (tutoria.tipo == "Reconocimiento") {
            iconGravedad.visibility = View.GONE
            linearView.visibility = View.GONE
        } else {
            iconGravedad.visibility = View.VISIBLE
            linearView.visibility = View.VISIBLE
        }

        if (tutoria.urlImagen.isNullOrEmpty()) {
            imageViewCard.visibility = View.GONE
        } else {
            imageViewCard.visibility = View.VISIBLE
        }

    }

    private fun ocultar() {
        if (::tutoria.isInitialized && (tutoria.estado == "Revisado" || tutoria.estado == "Notificado" || tutoria.estado == "Citado" || tutoria.estado == "Completado")) {
            cardRevisarEnviar.visibility = View.GONE // Ocultar todo el CardView

            // Mantener estas líneas por compatibilidad
            checkBoxRevisado.visibility = View.GONE
            tvRevisado.visibility = View.GONE
            btnEnviar.visibility = View.GONE
        } else {
            cardRevisarEnviar.visibility = View.VISIBLE // Asegurar que sea visible
        }

    }

    private fun setupListeners() {
        checkBoxRevisado.setOnCheckedChangeListener { _, isChecked ->
            btnEnviar.isEnabled = isChecked
        }

        btnEnviar.setOnClickListener {
            updateEstadoInDatabase()
        }
    }

    private fun updateEstadoInDatabase() {
        if (!::tutoria.isInitialized) return

        // Cambiar el estado localmente
        tutoria.cambiarEstado("Revisado")

        // Referencia al documento en Firestore
        val incidenciaRef = firestore.collection("Incidencia").document(tutoria.id)

        // Actualizar estado en la base de datos
        incidenciaRef.update("estado", tutoria.estado)
            .addOnSuccessListener {
                // Actualizar UI solo si Firestore responde con éxito
                tvEstado.text = tutoria.estado
                checkBoxRevisado.isEnabled = false
                btnEnviar.isEnabled = false
                finish()
            }
            .addOnFailureListener { e ->
                // En caso de error, revertir cambios si es necesario (opcional)
                e.printStackTrace()
                btnEnviar.isEnabled = true
            }
    }

}