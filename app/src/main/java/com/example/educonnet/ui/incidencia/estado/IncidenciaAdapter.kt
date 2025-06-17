package com.example.educonnet.ui.incidencia.estado

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Typeface
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
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.LinearProgressIndicator
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

    companion object {
        private const val VIEW_TYPE_ITEM = 0
        private const val VIEW_TYPE_LOADER = 1

        fun newInstance(incidencias: List<IncidenciaClass>, context: Context) =
            IncidenciaAdapter(incidencias, context)
    }

    override fun getItemViewType(position: Int): Int {
        return if (incidencias[position].id == "LOADER") VIEW_TYPE_LOADER else VIEW_TYPE_ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IncidenciaViewHolder {
        val binding = ItemIncidenciaBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return IncidenciaViewHolder(binding, context, firestore)
    }

    override fun onBindViewHolder(holder: IncidenciaViewHolder, position: Int) {
        holder.bind(incidencias[position])
    }

    override fun getItemCount(): Int = incidencias.size

    fun updateData(newIncidencias: List<IncidenciaClass>) {
        incidencias = newIncidencias
        notifyDataSetChanged()
    }

    class IncidenciaViewHolder(
        private val binding: ItemIncidenciaBinding,
        private val context: Context,
        private val firestore: FirebaseFirestore
    ) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("MissingInflatedId", "SetTextI18n")
        fun bind(incidencia: IncidenciaClass) {
            setupBaseViews(incidencia)
            setupPdfButton(incidencia)
            setupStepper(incidencia)
            setupStyles(incidencia)
            setupClickListeners(incidencia)
        }

        private fun setupBaseViews(incidencia: IncidenciaClass) {
            with(binding) {
                tvEstudiante.text = "${incidencia.apellidoEstudiante} ${incidencia.nombreEstudiante}"
                tvAtencion.text = incidencia.atencion.ifEmpty { "Destacado" }
                tvHora.text = incidencia.hora
                tvFecha.text = incidencia.fecha
                tvEstado.text = incidencia.estado
                tvGrado.text = "${incidencia.grado}° ${incidencia.seccion}"
                tvTipo.text = incidencia.tipo
            }
        }

        private fun setupPdfButton(incidencia: IncidenciaClass) {
            with(binding) {
                chipVerPFDInformeIncidencia.visibility = if (incidencia.estado == "Completado") View.VISIBLE else View.GONE
                chipVerPFDInformeIncidencia.setOnClickListener {
                    generateAndShowPdf(incidencia)
                }
                tvEstado.visibility = if (incidencia.estado != "Completado") View.VISIBLE else View.GONE
            }
        }

        private fun setupStepper(incidencia: IncidenciaClass) {
            binding.clickFrameStepper.setOnClickListener {
                showStepperDialog(incidencia)
            }
        }

        @SuppressLint("ResourceType")
        private fun setupStyles(incidencia: IncidenciaClass) {
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

        private fun setupClickListeners(incidencia: IncidenciaClass) {
            binding.root.setOnClickListener {
                launchDetailActivity(incidencia)
            }
        }

        @SuppressLint("InflateParams")
        private fun showStepperDialog(incidencia: IncidenciaClass) {
            try {
                val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_stepper, null)

                val progressBar = dialogView.findViewById<LinearProgressIndicator>(R.id.progressBar)
                val statusSummary = dialogView.findViewById<TextView>(R.id.statusSummary)

                val stepViews = listOf(
                    Triple(
                        dialogView.findViewById<MaterialCardView>(R.id.stepEnviadoCard),
                        dialogView.findViewById<ImageView>(R.id.stepEnviadoIcon),
                        dialogView.findViewById<TextView>(R.id.stepEnviadoText)
                    ),
                    Triple(
                        dialogView.findViewById<MaterialCardView>(R.id.stepRevisadoCard),
                        dialogView.findViewById<ImageView>(R.id.stepRevisadoIcon),
                        dialogView.findViewById<TextView>(R.id.stepRevisadoText)
                    ),
                    Triple(
                        dialogView.findViewById<MaterialCardView>(R.id.stepNotificadoCard),
                        dialogView.findViewById<ImageView>(R.id.stepNotificadoIcon),
                        dialogView.findViewById<TextView>(R.id.stepNotificadoText)
                    ),
                    Triple(
                        dialogView.findViewById<MaterialCardView>(R.id.stepCitadoCard),
                        dialogView.findViewById<ImageView>(R.id.stepCitadoIcon),
                        dialogView.findViewById<TextView>(R.id.stepCitadoText)
                    ),
                    Triple(
                        dialogView.findViewById<MaterialCardView>(R.id.stepCompletadoCard),
                        dialogView.findViewById<ImageView>(R.id.stepCompletadoIcon),
                        dialogView.findViewById<TextView>(R.id.stepCompletadoText)
                    )
                )

                val pasosOrden = listOf("Enviado", "Revisado", "Notificado", "Citado", "Completado")
                val indexActual = pasosOrden.indexOf(incidencia.estado).coerceAtLeast(0)
                val progress = ((indexActual + 1) / pasosOrden.size.toFloat()) * 100

                progressBar.progress = progress.toInt()
                statusSummary.text = "Estado actual: ${incidencia.estado}"

                pasosOrden.forEachIndexed { index, _ ->
                    val (card, icon, text) = stepViews[index]
                    val isCompleted = index < indexActual
                    val isCurrent = index == indexActual

                    when {
                        isCompleted -> {
                            card.strokeColor = ContextCompat.getColor(context, R.color.md_theme_primary)
                            card.setCardBackgroundColor(ContextCompat.getColor(context, R.color.md_theme_primaryContainer))
                            icon.setImageResource(R.drawable.ic_check)
                            icon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.md_theme_onPrimaryContainer))
                            text.setTextColor(ContextCompat.getColor(context, R.color.md_theme_onSurface))
                            text.setTypeface(null, Typeface.BOLD)
                        }
                        isCurrent -> {
                            card.strokeColor = ContextCompat.getColor(context, R.color.md_theme_primary)
                            card.setCardBackgroundColor(ContextCompat.getColor(context, R.color.md_theme_primaryContainer))

                            if (incidencia.estado == "Completado") {
                                icon.setImageResource(R.drawable.ic_check)
                            } else {
                                icon.setImageResource(R.drawable.ic_clock)
                            }

                            icon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.md_theme_onPrimaryContainer))
                            text.setTextColor(ContextCompat.getColor(context, R.color.md_theme_onSurface))
                            text.setTypeface(null, Typeface.BOLD)
                        }
                        else -> {
                            card.strokeColor = ContextCompat.getColor(context, R.color.md_theme_outline)
                            card.setCardBackgroundColor(ContextCompat.getColor(context, R.color.md_theme_surface))
                            icon.setImageResource(R.drawable.ic_circle_outline)
                            icon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.md_theme_onSurfaceVariant))
                            text.setTextColor(ContextCompat.getColor(context, R.color.md_theme_onSurfaceVariant))
                        }
                    }
                }

                MaterialAlertDialogBuilder(context)
                    .setTitle("Progreso de la Incidencia")
                    .setView(dialogView)
                    .setPositiveButton("Cerrar", null)
                    .show()

            } catch (e: Exception) {
                Log.e("StepperDialog", "Error al mostrar diálogo", e)
                Toast.makeText(context, "Error al mostrar el progreso", Toast.LENGTH_SHORT).show()
            }
        }

        private fun generateAndShowPdf(incidencia: IncidenciaClass) {
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
                            showGeneratedPdf(file, incidencia)
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

        private fun showGeneratedPdf(pdfFile: File, incidencia: IncidenciaClass) {
            try {
                val uri: Uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    pdfFile
                )

                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/pdf")
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NO_HISTORY
                }

                val packageManager = context.packageManager
                val activities = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)

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
}