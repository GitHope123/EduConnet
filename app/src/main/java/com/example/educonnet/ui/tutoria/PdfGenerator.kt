package com.example.educonnet.ui.tutoria

import android.content.Context
import com.itextpdf.html2pdf.HtmlConverter
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class PdfGenerator(private val context: Context, private val tutoria: TutoriaClass) {

    fun generatePDF(outputPath: String): Boolean {
        return try {
            val htmlTemplate = createHtmlTemplate()
            HtmlConverter.convertToPdf(htmlTemplate, FileOutputStream(outputPath))
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun createHtmlTemplate(): String {
        val doc: Document = Jsoup.parse("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    @import url('https://fonts.googleapis.com/css2?family=Roboto:wght@300;400;500;700&family=Open+Sans:wght@400;600;700&display=swap');
                    
                    body { 
                        font-family: 'Open Sans', sans-serif; 
                        margin: 0; 
                        padding: 30px; 
                        color: #333333; 
                        line-height: 1.6;
                    }
                    .header { 
                        text-align: center; 
                        margin-bottom: 40px; 
                        padding-bottom: 20px;
                        border-bottom: 2px solid #0070EB;
                    }
                    .title { 
                        font-family: 'Roboto', sans-serif;
                        font-size: 28px; 
                        font-weight: 700; 
                        color: #0058BC;
                        margin-bottom: 8px;
                        letter-spacing: -0.5px;
                    }
                    .subtitle {
                        font-size: 16px;
                        color: #7f8c8d;
                        font-weight: 400;
                    }
                    .section { 
                        margin-bottom: 30px; 
                        padding-bottom: 20px; 
                    }
                    .section-title { 
                        font-family: 'Roboto', sans-serif;
                        font-size: 20px; 
                        font-weight: 600; 
                        color: #0058BC; 
                        margin-bottom: 15px;
                        padding-bottom: 8px;
                        border-bottom: 2px solid #0070EB;
                        display: inline-block;
                    }
                    .row { 
                        display: flex; 
                        margin-bottom: 12px; 
                    }
                    .label { 
                        font-weight: 600; 
                        width: 220px; 
                        color: #555555;
                        font-family: 'Roboto', sans-serif;
                    }
                    .value { 
                        flex: 1;
                        font-weight: 400;
                    }
                    .footer { 
                        margin-top: 40px; 
                        text-align: right; 
                        font-size: 13px; 
                        color: #95a5a6; 
                        padding-top: 15px;
                        border-top: 1px solid #e0e0e0;
                        font-family: 'Roboto', sans-serif;
                    }
                    .image-container { 
                        text-align: center; 
                        margin: 25px 0; 
                    }
                    .image-container img { 
                        max-width: 80%; 
                        height: auto; 
                        border: 1px solid #ddd; 
                        border-radius: 8px;
                        box-shadow: 0 2px 8px rgba(0,0,0,0.1);
                    }
                    .urgent { 
                        color: #e74c3c; 
                        font-weight: 700;
                        background-color: #FFEBEE;
                        padding: 4px 10px;
                        border-radius: 6px;
                        display: inline-block;
                        font-family: 'Roboto', sans-serif;
                    }
                    .details-content {
                        padding: 15px;
                        background-color: #F8FAFC;
                        border-radius: 8px;
                        border-left: 4px solid #0070EB;
                        margin-top: 10px;
                        font-size: 15px;
                        line-height: 1.7;
                    }
                    .logo {
                        height: 60px;
                        margin-bottom: 20px;
                    }
                    .highlight {
                        background-color: #E3F2FD;
                        padding: 2px 4px;
                        border-radius: 4px;
                    }
                </style>
            </head>
            <body>
                <div class="header">
                    <div class="title">Reporte de Tutoría</div>
                    <div class="subtitle">Registro académico detallado</div>
                </div>
            </body>
            </html>
        """.trimIndent())

        // Añadir secciones con tipografía moderna
        addSection(doc, "Información del Estudiante", listOf(
            "Nombre completo" to safeToString("${tutoria.nombreEstudiante} ${tutoria.apellidoEstudiante}", "highlight"),
            "Grado académico" to safeToString(tutoria.grado),
            "Nivel educativo" to safeToString(tutoria.nivel),
            "Contacto del apoderado" to safeToString(tutoria.celularApoderado)
        ))

        addSection(doc, "Información del Profesor", listOf(
            "Profesor a cargo" to safeToString("${tutoria.nombreProfesor} ${tutoria.apellidoProfesor}", "highlight"),
            "Cargo académico" to safeToString(tutoria.cargo)
        ))

        val gravedadClass = if (tutoria.atencion == "Urgente") "urgent" else ""

        addSection(doc, "Detalles de la Tutoría", listOf(
            "Fecha programada" to safeToString(tutoria.fecha),
            "Horario establecido" to safeToString(tutoria.hora),
            "Estado actual" to safeToString(tutoria.estado),
            "Tipo de tutoría" to safeToString(tutoria.tipo),
            "Nivel de prioridad" to safeToString(tutoria.atencion, gravedadClass)
        ))

        // Detalles adicionales con diseño moderno
        doc.selectFirst("body")?.let { body ->
            val detailsSection = body.appendElement("div").addClass("section")
            detailsSection.appendElement("div")
                .addClass("section-title")
                .text("Observaciones y detalles")

            val detailsContent = detailsSection.appendElement("div").addClass("details-content")
            detailsContent.html(tutoria.detalle ?: "<i>No se registraron observaciones adicionales</i>")
        }

        // Manejo de imagen con estilo moderno
        tutoria.urlImagen?.takeIf { it.isNotEmpty() }?.let { url ->
            try {
                doc.selectFirst("body")?.let { body ->
                    val imageContainer = body.appendElement("div").addClass("image-container")
                    imageContainer.appendElement("img").attr("src", url)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Pie de página moderno
        doc.selectFirst("body")?.appendElement("div")
            ?.addClass("footer")
            ?.html("""
                Documento generado el ${SimpleDateFormat("dd/MM/yyyy 'a las' HH:mm").format(Date())}<br>
                <span style="color: #0058BC; font-weight: 600;">EduConnect</span> - Sistema de Gestión Educativa
            """.trimIndent())

        return doc.html()
    }

    private fun addSection(doc: Document, title: String, items: List<Pair<String, String>>) {
        doc.selectFirst("body")?.let { body ->
            val section = body.appendElement("div").addClass("section")
            section.appendElement("div").addClass("section-title").text(title)

            items.forEach { (label, value) ->
                val row = section.appendElement("div").addClass("row")
                row.appendElement("div").addClass("label").text("$label:")
                row.appendElement("div").addClass(value.substringAfterLast('|'))
                    .text(value.substringBefore('|'))
            }
        }
    }

    private fun safeToString(value: Any?, cssClass: String = ""): String {
        return when (value) {
            null -> "No especificado"
            is String -> if (value.isBlank()) "No especificado" else value
            else -> value.toString()
        } + if (cssClass.isNotEmpty()) "|$cssClass" else ""
    }
}