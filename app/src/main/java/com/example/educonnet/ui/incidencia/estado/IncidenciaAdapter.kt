package com.example.educonnet.ui.incidencia.estado

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.RecyclerView
import com.example.educonnet.R
import com.example.educonnet.databinding.ItemIncidenciaBinding
import com.example.educonnet.ui.incidencia.DescripcionIncidencia
import com.example.educonnet.ui.resources.GeneradorPdfInforme
import com.example.educonnet.ui.tutoria.recurso.Informe
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class IncidenciaAdapter(
    private var incidencias: List<IncidenciaClass>,
    private val context: Context
) : RecyclerView.Adapter<IncidenciaAdapter.IncidenciaViewHolder>() {

    private val firestore = FirebaseFirestore.getInstance()

    inner class IncidenciaViewHolder(private val binding: ItemIncidenciaBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("MissingInflatedId")
        fun bind(incidencia: IncidenciaClass) {
            with(binding) {
                // Configuración básica de la vista
                tvEstudiante.text = "${incidencia.apellidoEstudiante} ${incidencia.nombreEstudiante}"
                tvAtencion.text = incidencia.atencion.ifEmpty { "Destacado" }
                tvHora.text = incidencia.hora
                tvFecha.text = incidencia.fecha
                tvEstado.text = incidencia.estado
                tvGrado.text = "${incidencia.grado}°"
                tvNivel.text = incidencia.seccion
                tvTipo.text = incidencia.tipo

                // Configurar botón PDF
                configurarBotonPDF(incidencia)

                // Configurar stepper de estado
                configurarStepper(incidencia)

                // Configurar estilos visuales
                configurarEstilos(incidencia)

                // Configurar clic en el item
                root.setOnClickListener { launchDetailActivity(incidencia) }
            }
        }

        private fun configurarBotonPDF(incidencia: IncidenciaClass) {
            binding.chipVerPFDInformeIncidencia.apply {
                visibility = if (incidencia.estado == "Completado") View.VISIBLE else View.GONE
                setOnClickListener { generarYMostrarPDF(incidencia) }
            }
        }

        private fun configurarStepper(incidencia: IncidenciaClass) {
            binding.clickFrameStepper.setOnClickListener {
                try {
                    val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_stepper, null)

                    val stepViews = listOf(
                        dialogView.findViewById<ImageView>(R.id.stepEnviadoIcon) to dialogView.findViewById<TextView>(R.id.stepEnviadoText),
                        dialogView.findViewById<ImageView>(R.id.stepRevisadoIcon) to dialogView.findViewById<TextView>(R.id.stepRevisadoText),
                        dialogView.findViewById<ImageView>(R.id.stepNotificadoIcon) to dialogView.findViewById<TextView>(R.id.stepNotificadoText),
                        dialogView.findViewById<ImageView>(R.id.stepCitadoIcon) to dialogView.findViewById<TextView>(R.id.stepCitadoText),
                        dialogView.findViewById<ImageView>(R.id.stepCompletadoIcon) to dialogView.findViewById<TextView>(R.id.stepCompletadoText)
                    )

                    val pasosOrden = listOf("Enviado", "Revisado", "Notificado", "Citado", "Completado")
                    val indexActual = pasosOrden.indexOf(incidencia.estado).coerceAtLeast(0)

                    pasosOrden.forEachIndexed { index, _ ->
                        val (iconView, textView) = stepViews[index]
                        val isCompleted = index <= indexActual

                        iconView.setImageResource(
                            if (isCompleted) R.drawable.ic_radio_button_checked
                            else R.drawable.ic_radio_button_unchecked
                        )

                        val colorRes = if (isCompleted) R.color.md_theme_primary else R.color.md_theme_inverseSurface
                        textView.setTextColor(ContextCompat.getColor(context, colorRes))
                    }

                    MaterialAlertDialogBuilder(context)
                        .setTitle("Estado de la Incidencia")
                        .setView(dialogView)
                        .setPositiveButton("Cerrar", null)
                        .show()

                } catch (e: Exception) {
                    Log.e("StepperDialog", "Error al mostrar diálogo", e)
                    Toast.makeText(context, "Error al mostrar estado", Toast.LENGTH_SHORT).show()
                }
            }
        }

        private fun configurarEstilos(incidencia: IncidenciaClass) {
            with(binding) {
                // Color según estado
                val estadoColor = when (incidencia.estado) {
                    "Revisado" -> R.color.Green
                    "Notificado" -> R.color.color_blue
                    "Citado" -> R.color.md_theme_secondary
                    "Completado" -> R.color.md_theme_primary
                    else -> R.color.color_red
                }
                tvEstado.setTextColor(ContextCompat.getColor(context, estadoColor))

                // Color e icono según gravedad
                val (gravedadColor, gravedadIcon) = when (incidencia.atencion) {
                    "Moderado" -> Pair(R.color.Primary_green, R.drawable.ic_type_indicidencia)
                    "Urgente" -> Pair(R.color.color_orange, R.drawable.ic_type_indicidencia)
                    "Muy urgente" -> Pair(R.color.color_red, R.drawable.ic_type_indicidencia)
                    else -> Pair(R.color.color_blue, R.drawable.icon_incidencia_positiva)
                }
                tvAtencion.setTextColor(ContextCompat.getColor(context, gravedadColor))
                tvImagenGravedad.setColorFilter(ContextCompat.getColor(context, gravedadColor))
                tvImagenGravedad.setImageResource(gravedadIcon)

                // Progreso circular
                val progresoMap = mapOf(
                    "Revisado" to 25f,
                    "Notificado" to 50f,
                    "Citado" to 75f,
                    "Completado" to 100f
                )
                val progreso = progresoMap[incidencia.estado] ?: 0f
                circularProgressBar.setProgressWithAnimation(progreso, 1000)
                tvProgressText.text = "${progreso.toInt()}%"
            }
        }

        private fun generarYMostrarPDF(incidencia: IncidenciaClass) {
            val progressDialog = ProgressDialog(context).apply {
                setMessage("Generando informe...")
                setCancelable(false)
                show()
            }

            firestore.collection("Informe")
                .whereEqualTo("idInforme", incidencia.id)
                .limit(1)
                .get()
                .addOnSuccessListener { documents ->
                    progressDialog.dismiss()

                    if (documents.isEmpty) {
                        Toast.makeText(context, "No se encontró informe asociado", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }

                    val informe = documents.first().toObject(Informe::class.java)
                    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                    val fileName = "Informe_${incidencia.id}_$timestamp.pdf"
                    val file = File(context.getExternalFilesDir(null), fileName)

                    try {
                        GeneradorPdfInforme(context, incidencia, informe).apply {
                            generarPdf(file.absolutePath)
                            mostrarPDFGenerado(file, incidencia)
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error al generar PDF", Toast.LENGTH_SHORT).show()
                        Log.e("PDF_ERROR", "Error al generar PDF", e)
                    }
                }
                .addOnFailureListener {
                    progressDialog.dismiss()
                    Toast.makeText(context, "Error al obtener informe", Toast.LENGTH_SHORT).show()
                }
        }

        private fun mostrarPDFGenerado(pdfFile: File, incidencia: IncidenciaClass) {
            try {
                val uri: Uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    pdfFile
                )

                // Otorgar permisos temporales
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/pdf")
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NO_HISTORY
                }

                // Verificar si hay una aplicación para abrir PDFs
                val packageManager = context.packageManager
                val activities = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)

                // Otorgar permisos a todas las aplicaciones que puedan manejar el intent
                for (resolveInfo in activities) {
                    val packageName = resolveInfo.activityInfo.packageName
                    context.grantUriPermission(
                        packageName,
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                }

                try {
                    context.startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    // Si no hay visor de PDFs, ofrecer compartir
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/pdf"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        putExtra(Intent.EXTRA_TEXT,
                            "Informe de incidencia - ${incidencia.nombreEstudiante}\n" +
                                    "Fecha: ${incidencia.fecha}\n" +
                                    "Tipo: ${incidencia.tipo}"
                        )
                        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Compartir informe"))
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error al mostrar PDF: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e("PDF_VIEWER", "Error al mostrar PDF", e)
            }
        }

        private fun launchDetailActivity(incidencia: IncidenciaClass) {
            Intent(context, DescripcionIncidencia::class.java).apply {
                putExtras(bundleOf(
                    "INCIDENCIA_ID" to incidencia.id,
                    "INCIDENCIA_FECHA" to incidencia.fecha,
                    "INCIDENCIA_HORA" to incidencia.hora,
                    "INCIDENCIA_NOMBRE" to incidencia.nombreEstudiante,
                    "INCIDENCIA_APELLIDO" to incidencia.apellidoEstudiante,
                    "INCIDENCIA_GRADO" to incidencia.grado,
                    "INCIDENCIA_SECCION" to incidencia.seccion,
                    "INCIDENCIA_TIPO" to incidencia.tipo,
                    "INCIDENCIA_ATENCION" to incidencia.atencion,
                    "INCIDENCIA_ESTADO" to incidencia.estado,
                    "INCIDENCIA_DETALLE" to incidencia.detalle,
                    "INCIDENCIA_FOTO_URL" to incidencia.imageUri
                ))
                context.startActivity(this)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IncidenciaViewHolder {
        val binding = ItemIncidenciaBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return IncidenciaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: IncidenciaViewHolder, position: Int) {
        holder.bind(incidencias[position])
    }

    override fun getItemCount(): Int = incidencias.size

    fun updateData(newIncidencias: List<IncidenciaClass>) {
        incidencias = newIncidencias
        notifyDataSetChanged()
    }

    companion object {
        fun newInstance(incidencias: List<IncidenciaClass>, context: Context) =
            IncidenciaAdapter(incidencias, context)
    }
}