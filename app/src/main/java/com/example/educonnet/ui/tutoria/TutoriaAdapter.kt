package com.example.educonnet.ui.tutoria

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.educonnet.R
import com.example.educonnet.databinding.ItemTutoriaBinding
import com.example.educonnet.ui.incidencia.estado.IncidenciaClass
import com.example.educonnet.ui.resources.GeneradorPdfInforme
import com.example.educonnet.ui.tutoria.recurso.CitarFormulario
import com.example.educonnet.ui.tutoria.recurso.Informe
import com.example.educonnet.ui.tutoria.recurso.InformeFormulario
import com.example.educonnet.ui.tutoria.recurso.TutoriaUpdateCallback
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File


class TutoriaAdapter(
    private var listaTutorias: MutableList<TutoriaClass>,
    private val updateCallback: TutoriaUpdateCallback
) : RecyclerView.Adapter<TutoriaAdapter.TutoriaViewHolder>() {

    inner class TutoriaViewHolder(private val binding: ItemTutoriaBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val imagenViewAlerta: ImageView = binding.ImagenViewAlertaTutoria
        private val textViewGravedad: MaterialTextView = binding.TextViewGravedadTutoria
        private val textViewFecha: MaterialTextView = binding.textViewFechaTutoria
        private val textViewHora: MaterialTextView = binding.textViewHoraTutoria
        private val textViewEstudiante: MaterialTextView = binding.textViewEstudianteTutoria
        private val textViewProfesor: MaterialTextView = binding.textViewProfesorTutoria
        private val textViewTipo: Chip = binding.textViewTipoIncidenciaTutoria
        private val textViewEstado: Chip = binding.textViewEstadoTutoria
        private val buttonMore: MaterialButton = binding.buttonMore
        val rootView: View = binding.root

        @SuppressLint("ResourceAsColor")
        fun bind(tutoria: TutoriaClass) {
            // Configuración de datos
            textViewFecha.text = tutoria.fecha
            textViewHora.text = tutoria.hora
            textViewEstudiante.text = "${tutoria.apellidoEstudiante} ${tutoria.nombreEstudiante}"
            textViewProfesor.text = "${tutoria.nombreProfesor} ${tutoria.apellidoProfesor}"
            textViewTipo.text = tutoria.tipo
            textViewEstado.text = tutoria.estado
            textViewGravedad.text = tutoria.atencion

            binding.buttonPdf.visibility = if (tutoria.estado == "Completado") View.VISIBLE else View.GONE

            binding.buttonPdf.setOnClickListener {
                // muestra el boton si el estado esta en Completado
                // Verificar primero si la tutoría tiene un ID de informe asociado
                if (tutoria.id.isNullOrEmpty()) {
                    Toast.makeText(it.context, "Esta tutoría no tiene informe asociado", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Mostrar progreso mientras se carga
                val progressDialog = ProgressDialog(it.context).apply {
                    setMessage("Generando informe...")
                    setCancelable(false)
                    show()
                }

                // Obtener el informe desde Firestore
                FirebaseFirestore.getInstance().collection("Informe")
                    .document(tutoria.id)
                    .get()
                    .addOnSuccessListener { document ->
                        progressDialog.dismiss()

                        val informe = document.toObject(Informe::class.java)?.apply {
                            // Asegurarnos que el ID del informe esté establecido
                            idInforme = document.id
                        }

                        if (informe != null) {
                            generarYMostrarPdf(it.context, tutoria, informe)
                        } else {
                            Toast.makeText(it.context, "No se pudo cargar el informe", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener { e ->
                        progressDialog.dismiss()
                        Toast.makeText(
                            it.context,
                            "Error al cargar el informe: ${e.localizedMessage}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }

            // Configuración de estilos
            setEstadoStyle(tutoria.estado)
            setAlertColors(getAlertColor(tutoria.atencion))

            // Configuración del botón de opciones
            setupMoreButton(tutoria)

            // Click en el item
            binding.root.setOnClickListener {
                val intent = Intent(it.context, DescripcionRevisar::class.java).apply {
                    putExtra("TUTORIA_EXTRA", tutoria)
                }
                it.context.startActivity(intent)
            }
        }

        private fun setupMoreButton(tutoria: TutoriaClass) {
            buttonMore.setOnClickListener { view ->
                if (tutoria.estado == "Pendiente") {
                    Snackbar.make(
                        rootView,
                        "Debes revisar la tutoria antes de usar otras funciones",
                        Snackbar.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                showPopupMenu(view, tutoria)
            }
        }

        private fun showPopupMenu(view: View, tutoria: TutoriaClass) {
            val context = view.context
            val popup = PopupMenu(context, view)
            popup.menuInflater.inflate(R.menu.tutoria_more_menu, popup.menu)

            // Cambiar el color del texto a negro
            for (i in 0 until popup.menu.size()) {
                val menuItem = popup.menu.getItem(i)
                val spanString = SpannableString(menuItem.title)
                spanString.setSpan(ForegroundColorSpan(Color.BLACK), 0, spanString.length, 0)
                menuItem.title = spanString
            }

            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_notificar -> handleNotificarAction(tutoria)
                    R.id.action_citar -> handleCitarAction(tutoria)
                    R.id.action_completar -> handleCompletarAction(tutoria)
                    else -> false
                }
            }
            popup.show()
        }

        private fun handleNotificarAction(tutoria: TutoriaClass): Boolean {
            return when (tutoria.estado) {
                "Revisado" -> {
                    showContactOptionsDialog(tutoria)
                    true
                }
                "Notificado" -> {
                    Snackbar.make(rootView,
                        "Ya notificaste al apoderado",
                        Snackbar.LENGTH_LONG).show()
                    true
                }
                "Citado" -> {
                    Snackbar.make(rootView,
                        "Esta tutoría ya fue citada",
                        Snackbar.LENGTH_LONG).show()
                    true
                }
                "Completado" -> {
                    Snackbar.make(rootView,
                        "Esta tutoría ya fue completada, no se puede notificar",
                        Snackbar.LENGTH_SHORT).show()
                    true
                }
                "Pendiente" -> {
                    Snackbar.make(rootView,
                        "Debes revisar la tutoría antes de notificar al apoderado",
                        Snackbar.LENGTH_LONG).show()
                    true
                }
                else -> {
                    Snackbar.make(rootView,
                        "Estado no válido para notificación: ${tutoria.estado}",
                        Snackbar.LENGTH_SHORT).show()
                    true
                }
            }
        }
        private fun handleCitarAction(tutoria: TutoriaClass): Boolean {
            return when (tutoria.estado) {
                "Notificado" -> {
                    CitarFormulario.mostrar(
                        context = rootView.context,
                        tutoria = tutoria,
                        callback = object : TutoriaUpdateCallback {
                            override fun onTutoriaUpdated(tutoriaId: String, nuevoEstado: String) {
                                updateTutoriaState(tutoriaId, nuevoEstado)
                            }
                        }
                    )
                    true
                }
                "Revisado" -> {
                    Snackbar.make(rootView,
                        "Primero debes notificar al apoderado antes de citar",
                        Snackbar.LENGTH_LONG).show()
                    true
                }
                "Pendiente" -> {
                    Snackbar.make(rootView,
                        "Debes revisar y notificar al apoderado antes de citar",
                        Snackbar.LENGTH_LONG).show()
                    true
                }
                "Completado" -> {
                    Snackbar.make(rootView,
                        "Esta tutoría ya fue completada, no se puede citar",
                        Snackbar.LENGTH_SHORT).show()
                    true
                }
                else -> {
                    Snackbar.make(rootView,
                        "Estado no reconocido: ${tutoria.estado}",
                        Snackbar.LENGTH_SHORT).show()
                    true
                }
            }
        }
        private fun handleCompletarAction(tutoria: TutoriaClass): Boolean {
            return when (tutoria.estado) {
                "Citado" -> {
                    InformeFormulario.mostrar(
                        context = rootView.context,
                        tutoria = tutoria,
                        callback = object : TutoriaUpdateCallback {
                            override fun onTutoriaUpdated(tutoriaId: String, nuevoEstado: String) {
                                updateTutoriaState(tutoriaId, nuevoEstado)
                            }
                        }
                    )
                    true
                }
                "Notificado" -> {
                    Snackbar.make(rootView,
                        "Primero debes citar al apoderado antes de completar el informe",
                        Snackbar.LENGTH_LONG).show()
                    true
                }
                "Revisado" -> {
                    Snackbar.make(rootView,
                        "Debes notificar al apoderado y citarlo antes de completar el informe",
                        Snackbar.LENGTH_LONG).show()
                    true
                }
                "Pendiente" -> {
                    Snackbar.make(rootView,
                        "Debes revisar, notificar y citar antes de completar el informe",
                        Snackbar.LENGTH_LONG).show()
                    true
                }
                "Completado" -> {
                    Snackbar.make(rootView,
                        "El informe de esta tutoría ya fue completado",
                        Snackbar.LENGTH_SHORT).show()
                    true
                }
                else -> {
                    Snackbar.make(rootView,
                        "Estado no reconocido: ${tutoria.estado}",
                        Snackbar.LENGTH_SHORT).show()
                    true
                }
            }
        }

        private fun updateTutoriaState(tutoriaId: String, nuevoEstado: String) {
            listaTutorias.find { it.id == tutoriaId }?.estado = nuevoEstado
            updateCallback.onTutoriaUpdated(tutoriaId, nuevoEstado)
        }

        private fun setEstadoStyle(estado: String) {
            val context = binding.root.context
            when (estado) {
                "Pendiente" -> {
                    textViewEstado.setChipBackgroundColorResource(R.color.md_theme_light_errorContainer)
                    textViewEstado.setTextColor(ContextCompat.getColor(context, R.color.md_theme_light_error))
                    textViewEstado.chipStrokeColor = ContextCompat.getColorStateList(context, R.color.md_theme_light_error)
                }
                "Revisado" -> {
                    textViewEstado.setChipBackgroundColorResource(R.color.md_theme_light_tertiaryContainer)
                    textViewEstado.setTextColor(ContextCompat.getColor(context, R.color.md_theme_light_onTertiaryContainer))
                    textViewEstado.chipStrokeColor = ContextCompat.getColorStateList(context, R.color.md_theme_light_tertiary)
                }
                "Completado" -> {
                    textViewEstado.setChipBackgroundColorResource(R.color.md_theme_light_tertiaryContainerBlue)
                    textViewEstado.setTextColor(ContextCompat.getColor(context, R.color.md_theme_light_onTertiaryContainerBlue))
                    textViewEstado.chipStrokeColor = ContextCompat.getColorStateList(context, R.color.md_theme_light_tertiaryBlue)
                }
                "Notificado" -> {
                    textViewEstado.setChipBackgroundColorResource(R.color.md_theme_light_tertiaryContainerYellow)
                    textViewEstado.setTextColor(ContextCompat.getColor(context, R.color.md_theme_light_onTertiaryContainerYellow))
                    textViewEstado.chipStrokeColor = ContextCompat.getColorStateList(context, R.color.md_theme_light_tertiaryYellow)
                }
                else -> {
                    textViewEstado.setChipBackgroundColorResource(R.color.md_theme_light_secondaryContainer)
                    textViewEstado.setTextColor(ContextCompat.getColor(context, R.color.md_theme_light_onSecondaryContainer))
                    textViewEstado.chipStrokeColor = ContextCompat.getColorStateList(context, R.color.md_theme_light_secondary)
                }
            }
        }

        private fun getAlertColor(gravedad: String): Int {
            val context = binding.root.context
            return when (gravedad) {
                "Moderado" -> ContextCompat.getColor(context, R.color.md_theme_light_primary)
                "Urgente" -> ContextCompat.getColor(context, R.color.color_orange)
                "Muy urgente" -> ContextCompat.getColor(context, R.color.md_theme_light_error)
                else -> {
                    imagenViewAlerta.visibility = View.GONE
                    ContextCompat.getColor(context, R.color.md_theme_light_error)
                }
            }
        }

        private fun setAlertColors(color: Int) {
            imagenViewAlerta.setColorFilter(color)
            textViewGravedad.setTextColor(color)
        }

        private fun showContactOptionsDialog(tutoria: TutoriaClass) {
            val phoneNumber = tutoria.celularApoderado.toString()
            val context = binding.root.context

            if (phoneNumber.isBlank()) {
                Toast.makeText(context, "Número de teléfono no válido", Toast.LENGTH_SHORT).show()
                return
            }

            if (tutoria.estado == "Pendiente") {
                Toast.makeText(context, "Por favor, revise la incidencia antes de proceder con la notificación", Toast.LENGTH_LONG).show()
                return
            }

            val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_contact_options, null)
            val optionsList = dialogView.findViewById<ListView>(R.id.options_list)
            val checkBox = dialogView.findViewById<MaterialCheckBox>(R.id.confirm_checkbox)
            val btnEnviar = dialogView.findViewById<MaterialButton>(R.id.btn_enviar)
            val btnCancelar = dialogView.findViewById<MaterialButton>(R.id.btn_cancelar)

            val options = listOf(
                "Llamar a Apoderado de ${tutoria.nombreEstudiante}",
                "Enviar mensaje por WhatsApp",
                "Enviar PDF por WhatsApp"
            )
            val icons = listOf(R.drawable.ic_call_phone, R.drawable.ic_whatsapp, R.drawable.ic_pdf)

            val adapter = object : ArrayAdapter<String>(context, R.layout.dialog_option_item, options) {
                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val view = convertView ?: LayoutInflater.from(context)
                        .inflate(R.layout.dialog_option_item, parent, false)
                    view.findViewById<ImageView>(R.id.icon).setImageResource(icons[position])
                    view.findViewById<TextView>(R.id.option_text).text = options[position]
                    return view
                }
            }

            optionsList.adapter = adapter
            var selectedOption = -1

            optionsList.setOnItemClickListener { _, _, position, _ ->
                selectedOption = position
                checkBox.isEnabled = true
            }

            val dialog = MaterialAlertDialogBuilder(context)
                .setTitle("Selecciona una opción")
                .setView(dialogView)
                .create()

            btnEnviar.setOnClickListener {
                if (checkBox.isChecked && selectedOption != -1) {
                    when (selectedOption) {
                        0 -> makePhoneCall(phoneNumber)
                        1 -> openWhatsApp(phoneNumber)
                        2 -> sendPdfViaWhatsApp(tutoria)
                    }
                    updateIncidenciaEstado(tutoria.id)
                    dialog.dismiss()
                } else {
                    Toast.makeText(context, "Selecciona una opción y confirma el envío", Toast.LENGTH_SHORT).show()
                }
            }

            btnCancelar.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }

        private fun updateIncidenciaEstado(incidenciaId: String) {
            FirebaseFirestore.getInstance().collection("Incidencia")
                .document(incidenciaId)
                .update("estado", "Notificado")
                .addOnSuccessListener { Log.d("Estado", "Incidencia marcada como notificada") }
                .addOnFailureListener { e -> Log.e("Estado", "Error al actualizar", e) }
        }

        private fun makePhoneCall(phoneNumber: String) {
            try {
                val callIntent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:$phoneNumber")
                }
                binding.root.context.startActivity(callIntent)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(binding.root.context,
                    "Error al intentar realizar la llamada", Toast.LENGTH_SHORT).show()
            }
        }

        private fun openWhatsApp(phoneNumber: String) {
            try {
                val whatsappIntent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("https://wa.me/$phoneNumber")
                }
                binding.root.context.startActivity(whatsappIntent)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(binding.root.context,
                    "Error al intentar abrir WhatsApp", Toast.LENGTH_SHORT).show()
            }
        }

        private fun sendPdfViaWhatsApp(tutoria: TutoriaClass) {
            val context = binding.root.context

            try {
                val pdfGenerator = PdfGenerator(context, tutoria)
                val pdfFile = File(context.cacheDir, "Reporte_Tutoria_${tutoria.nombreEstudiante}.pdf")
                pdfGenerator.generatePDF(pdfFile.absolutePath)

                if (pdfFile.exists()) {
                    val fileUri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        pdfFile
                    )

                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/pdf"
                        putExtra(Intent.EXTRA_STREAM, fileUri)
                        putExtra(Intent.EXTRA_TEXT, "Consulta la información del estudiante ${tutoria.nombreEstudiante}")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }

                    shareIntent.setPackage("com.whatsapp")
                    context.startActivity(Intent.createChooser(shareIntent, "Enviar PDF por WhatsApp"))
                } else {
                    Toast.makeText(context, "Error al generar el PDF", Toast.LENGTH_SHORT).show()
                }
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(context, "WhatsApp no está instalado", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Error al generar o enviar el PDF: ${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TutoriaViewHolder {
        val binding = ItemTutoriaBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TutoriaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TutoriaViewHolder, position: Int) {
        holder.bind(listaTutorias[position])
    }

    private fun generarYMostrarPdf(context: Context, tutoria: TutoriaClass, informe: Informe) {
        try {
            // Archivo PDF a mostrar
            val fileName = "Informe_Tutoria_${tutoria.id}.pdf"
            val file = File(context.getExternalFilesDir(null), fileName)

            // Convertir TutoriaClass a IncidenciaClass si es necesario
            val incidencia = convertirTutoriaAIncidencia(tutoria)

            // Crear generador con los datos correctos
            val generador = GeneradorPdfInforme(context, incidencia, informe)

            // Generar el PDF
            generador.generarPdf(file.absolutePath)

            // Obtener URI segura
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            // Intent para abrir el PDF
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NO_HISTORY)
            }

            // Verificar si hay aplicación para abrir PDFs
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                // Opción alternativa: compartir el archivo
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_TEXT, "Informe de tutoría: ${tutoria.nombreEstudiante}")
                }
                context.startActivity(Intent.createChooser(shareIntent, "Abrir con..."))
            }

        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "No hay aplicación para ver PDF instalada", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Error al generar PDF: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            Log.e("PDF_ERROR", "Error al generar PDF", e)
        }
    }
    private fun convertirTutoriaAIncidencia(tutoria: TutoriaClass): IncidenciaClass {
        return IncidenciaClass(
            id = tutoria.id,
            fecha = tutoria.fecha,
            hora = tutoria.hora,
            nombreEstudiante = tutoria.nombreEstudiante,
            apellidoEstudiante = tutoria.apellidoEstudiante,
            grado = tutoria.grado, // Ajustar según corresponda
            seccion = tutoria.seccion, // Ajustar según corresponda
            celularApoderado = tutoria.celularApoderado ?: 0,
            tipo = tutoria.tipo,
            atencion = tutoria.atencion,
            estado = tutoria.estado,
            detalle =  tutoria.detalle, // Ajustar según corresponda
            imageUri = tutoria.urlImagen // Ajustar si hay imagen asociada
        )
    }

    fun updateData(newListTutoria: List<TutoriaClass>) {
        val uniqueTutorias = newListTutoria.distinctBy { it.id }
        listaTutorias.clear()
        listaTutorias.addAll(uniqueTutorias)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = listaTutorias.size
}