package com.example.educonnet.ui.tutoria
import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import androidx.core.content.res.ResourcesCompat
import com.example.educonnet.R
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.URL

class PdfGenerator(private val context: Context, private val tutoria: TutoriaClass) {

    private val paint = Paint().apply {
        // Establecer la fuente por defecto
        typeface = ResourcesCompat.getFont(context, R.font.open_sans)
    }
    fun generatePDF(outputPath: String): Boolean {
        return try {
            val document = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = document.startPage(pageInfo)
            val canvas: Canvas = page.canvas

            // Encabezado
            drawHeader(canvas)

            // Contenido del PDF
            var yPosition = drawStudentInfo(canvas, 130f)

            yPosition = drawTeacherInfo(canvas, yPosition)
            yPosition = drawIncidentDetails(canvas, yPosition)

            // Tabla de detalles de la incidencia
            yPosition = drawIncidentTable(canvas, yPosition)

            // URL de la imagen
            yPosition += 40f
            drawImageFromUrl(canvas, yPosition)

            // Finalizar la página
            document.finishPage(page)

            // Guardar el documento
            val outputFile = File(outputPath)
            document.writeTo(FileOutputStream(outputFile))
            document.close()

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    private fun drawHeader(canvas: Canvas) {
        paint.apply {
            color = Color.parseColor("#001b57")
            val headerRect = Rect(0, 0, canvas.width, 100)
            canvas.drawRect(headerRect, this)

            color = Color.parseColor("#FFA407")
            strokeWidth = 5f
            canvas.drawLine(0f, 100f, canvas.width.toFloat(), 100f, this)

            color = Color.WHITE
            textSize = 24f
            isFakeBoldText = true
            canvas.drawText("Reporte de Tutoría: ", 180f, 60f, this)
        }
    }
    private fun drawStudentInfo(canvas: Canvas, yPosition: Float): Float {
        var currentPosition = yPosition

        // Información del estudiante
        paint.apply {
            textSize = 16f
            color = Color.GRAY
            isFakeBoldText = true
            canvas.drawText("Información del Estudiante:", 30f, currentPosition, this)
        }
        currentPosition += 20f
        // Nombre
        paint.apply {
            textSize = 14f
            isFakeBoldText = true // Título en negrita
            canvas.drawText("Nombre:", 30f, currentPosition, this)
        }
        paint.apply {
            textSize = 14f
            isFakeBoldText = false // Desactivar la negrita para el contenido
            canvas.drawText("${tutoria.apellidoEstudiante} ${tutoria.nombreEstudiante}", 100f, currentPosition, this)
        }
        currentPosition += 20f
        // Celular del Apoderado
        paint.apply {
            textSize = 14f
            isFakeBoldText = true // Título en negrita
            canvas.drawText("Celular del Apoderado:", 30f, currentPosition, this)
        }
        paint.apply {
            textSize = 14f
            isFakeBoldText = false // Desactivar la negrita para el contenido
            canvas.drawText("${tutoria.celularApoderado}", 195f, currentPosition, this)
        }
        currentPosition += 20f

        // Grado
        paint.apply {
            textSize = 14f
            isFakeBoldText = true // Título en negrita
            canvas.drawText("Grado:", 30f, currentPosition, this)
        }
        paint.apply {
            textSize = 14f
            isFakeBoldText = false // Desactivar la negrita para el contenido
            canvas.drawText("${tutoria.grado}", 100f, currentPosition, this)
        }
        currentPosition += 20f
        // Nivel
        paint.apply {
            textSize = 14f
            isFakeBoldText = true // Título en negrita
            canvas.drawText("Nivel:", 30f, currentPosition, this)
        }
        paint.apply {
            textSize = 14f
            isFakeBoldText = false // Desactivar la negrita para el contenido
            canvas.drawText("${tutoria.nivel}", 100f, currentPosition, this)
        }
        // Línea separadora
        currentPosition += 30f
        paint.color = Color.LTGRAY
        canvas.drawLine(30f, currentPosition, 565f, currentPosition, paint)

        return currentPosition + 20f
    }

    private fun drawTeacherInfo(canvas: Canvas, yPosition: Float): Float {
        var currentPosition = yPosition

        // Configuración de la pintura para el encabezado en negrita
        paint.apply {
            textSize = 16f
            color = Color.GRAY
            isFakeBoldText = true
            canvas.drawText("Información del Profesor:", 30f, currentPosition, this)
        }

        // Ajuste de la posición después del encabezado
        currentPosition += 20f

        // Para los campos como "Profesor Responsable" y "Cargo", poner en negrita solo el título
        paint.apply {
            textSize = 14f
            isFakeBoldText = true // Títulos en negrita
            color = Color.GRAY
            canvas.drawText("Profesor Responsable:", 30f, currentPosition, this)
        }
        // Aquí el contenido no estará en negrita
        paint.apply {
            isFakeBoldText = false // Desactivar la negrita para el contenido
            canvas.drawText("${tutoria.nombreProfesor} ${tutoria.apellidoProfesor}", 200f, currentPosition, this)
        }
        currentPosition += 20f

        paint.apply {
            isFakeBoldText = true // Título en negrita
            canvas.drawText("Cargo:", 30f, currentPosition, this)
        }
        paint.apply {
            isFakeBoldText = false // Desactivar la negrita para el contenido
            canvas.drawText("${tutoria.cargo}", 100f, currentPosition, this)
        }
        // Línea separadora
        currentPosition += 30f
        paint.color = Color.LTGRAY
        canvas.drawLine(30f, currentPosition, 565f, currentPosition, paint)
        // Retornar la nueva posición para continuar con el resto de los elementos
        return currentPosition + 20f
    }
    private fun drawIncidentDetails(canvas: Canvas, yPosition: Float): Float {
        var currentPosition = yPosition

        // Configuración de la pintura para el encabezado en negrita
        paint.apply {
            textSize = 16f
            color = Color.GRAY
            isFakeBoldText = true
            canvas.drawText("Detalles de la Incidencia:", 30f, currentPosition, this)
        }

        // Ajuste de la posición después del encabezado
        currentPosition += 20f

        // Para los campos como "Fecha", "Hora" y "Detalle", poner en negrita solo el título
        paint.apply {
            textSize = 14f
            isFakeBoldText = true // Títulos en negrita
            color = Color.GRAY
            canvas.drawText("Fecha:", 30f, currentPosition, this)
        }
        // Aquí el contenido no estará en negrita
        paint.apply {
            isFakeBoldText = false // Desactivar la negrita para el contenido
            canvas.drawText("${tutoria.fecha}", 100f, currentPosition, this)
        }
        currentPosition += 20f

        paint.apply {
            isFakeBoldText = true // Título en negrita
            canvas.drawText("Hora:", 30f, currentPosition, this)
        }
        paint.apply {
            isFakeBoldText = false // Desactivar la negrita para el contenido
            canvas.drawText("${tutoria.hora}", 100f, currentPosition, this)
        }
        currentPosition += 20f

        paint.apply {
            isFakeBoldText = true // Título en negrita
            canvas.drawText("Detalle:", 30f, currentPosition, this)
        }

        paint.apply {
            isFakeBoldText = false // Desactivar la negrita para el contenido
            canvas.drawText("${tutoria.detalle}", 100f, currentPosition, this)
        }

        // Retornar la nueva posición para continuar con el resto de los elementos
        return currentPosition + 50f
    }
    private fun drawIncidentTable(canvas: Canvas, yPosition: Float): Float {
        var currentPosition = yPosition
        val rowHeight = 20f   // Altura de las filas
        val columnWidth = 100f  // Ancho de las columnas
        val rowHeightHeader = 20f // Altura del encabezado con respecto a las líneas
        val rowHeightValues = 20f // Altura de las filas de los valores

        // Dibujar líneas de la tabla
        // Línea superior de la tabla
        paint.strokeWidth = 2f
        paint.color = Color.GRAY
        canvas.drawLine(30f, currentPosition, 565f, currentPosition, paint) // Línea superior
        currentPosition += rowHeight
        // Encabezado de la tabla
        paint.isFakeBoldText = true
        canvas.drawText("Estado", columnWidth, currentPosition + rowHeightHeader / 2, paint) // Ajustar posición vertical
        canvas.drawText("Gravedad", columnWidth * 2 + 55f, currentPosition + rowHeightHeader / 2, paint)
        canvas.drawText("Tipo", columnWidth * 3 + 110f, currentPosition + rowHeightHeader / 2, paint)
        paint.isFakeBoldText = false

        // Línea debajo del encabezado
        currentPosition += rowHeightHeader
        canvas.drawLine(30f, currentPosition, 565f, currentPosition, paint) // Línea debajo del encabezado
        currentPosition += rowHeight

        // Detalles de la incidencia en la tabla
        canvas.drawText(tutoria.estado, columnWidth, currentPosition, paint)
        canvas.drawText(tutoria.atencion, columnWidth * 2 + 55f, currentPosition, paint)
        canvas.drawText(tutoria.tipo, columnWidth * 3 + 110f, currentPosition, paint)

        // Dibujar la línea inferior de la tabla
        currentPosition += rowHeightValues
        paint.color = Color.GRAY
        canvas.drawLine(30f, currentPosition, 565f, currentPosition, paint)

        return currentPosition
    }
    private fun drawImageFromUrl(canvas: Canvas, yPosition: Float): Float {
        try {
            // Obtener la URL de la imagen
            val imageUrl = tutoria.urlImagen
            // Verificar si la URL no está vacía
            if (imageUrl.isNotEmpty()) {
                // Descargar la imagen de la URL
                val url = URL(imageUrl)
                val inputStream: InputStream = url.openStream()
                // Decodificar la imagen en un Bitmap
                val bitmap: Bitmap = BitmapFactory.decodeStream(inputStream)
                // Verificar si la imagen es válida
                if (bitmap != null) {
                    // Definir tamaño para la imagen
                    val imageWidth = 100f // Ancho de la imagen
                    val imageHeight = 100f // Alto de la imagen
                    // Dibujar la imagen en el canvas
                    canvas.drawBitmap(bitmap, 30f, yPosition, paint)
                    // Actualizar la posición en el eje Y para el siguiente contenido
                    return yPosition + imageHeight + 10f
                } else {
                    // Si el Bitmap es null, mostrar un mensaje de error
                    paint.apply {
                        color = Color.GRAY
                        textSize = 12f
                    }
                    canvas.drawText("Error: Imagen no válida.", 30f, yPosition, paint)
                    return yPosition + 20f // Dejar espacio para el mensaje de error
                }
            } else {
                // Si la URL está vacía, mostrar un mensaje de error
                paint.apply {
                    color = Color.GRAY
                    textSize = 12f
                }
                canvas.drawText("", 30f, yPosition, paint)
                return yPosition + 20f // Dejar espacio para el mensaje de error
            }
        } catch (e: Exception) {
            // Manejar errores (como problemas de conexión)
            paint.apply {
                color = Color.GRAY
                textSize = 12f
            }
            canvas.drawText("", 30f, yPosition, paint)
            return yPosition + 20f // Dejar espacio para el mensaje de error
        }
    }


}