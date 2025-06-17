package com.example.educonnet.ui.resources

import android.content.Context
import com.example.educonnet.ui.incidencia.estado.IncidenciaClass
import com.example.educonnet.ui.tutoria.recurso.Informe
import com.itextpdf.html2pdf.HtmlConverter
import java.io.FileOutputStream

class GeneradorPdfInforme(
    private val context: Context,
    private val incidencia: IncidenciaClass,
    private val informe: Informe? = null
) {

    fun generarPdf(rutaSalida: String) {
        val esReconocimiento = incidencia.tipo.contains("Reconocimiento", ignoreCase = true)

        val html = """
            <html>
            <head>
                <style>
                    body {
                        font-family: 'Arial', sans-serif;
                        line-height: 1.5;
                        color: #333333;
                        padding: 0;
                        margin: 0;
                    }
                    .container {
                        max-width: 800px;
                        margin: 0 auto;
                        padding: 30px;
                    }
                    .header {
                        text-align: center;
                        margin-bottom: 30px;
                        padding-bottom: 20px;
                        border-bottom: 2px solid #0058BC;
                    }
                    .title {
                        color: #0058BC;
                        font-size: 22px;
                        font-weight: bold;
                        margin-bottom: 5px;
                        text-transform: uppercase;
                    }
                    .subtitle {
                        color: #666666;
                        font-size: 14px;
                    }
                    .section {
                        margin: 25px 0;
                    }
                    .section-title {
                        color: #0058BC;
                        font-weight: bold;
                        font-size: 16px;
                        margin-bottom: 15px;
                        padding-bottom: 5px;
                        border-bottom: 1px solid #0070EB;
                    }
                    table {
                        width: 100%;
                        border-collapse: collapse;
                        margin-bottom: 20px;
                    }
                    .data-table td {
                        padding: 10px 12px;
                        border-bottom: 1px solid #E0E0E0;
                        vertical-align: top;
                    }
                    .compact-table td {
                        padding: 8px 10px;
                        border-bottom: 1px solid #E0E0E0;
                        vertical-align: middle;
                    }
                    .highlight-box {
                        background-color: #F5F9FF;
                        padding: 15px;
                        border-radius: 4px;
                        border-left: 4px solid #0058BC;
                        margin: 15px 0;
                        font-size: 14px;
                    }
                    .footer {
                        margin-top: 40px;
                        text-align: center;
                        font-size: 11px;
                        color: #666666;
                        padding-top: 15px;
                        border-top: 1px solid #E0E0E0;
                    }
                    .badge {
                        display: inline-block;
                        padding: 3px 8px;
                        border-radius: 4px;
                        font-size: 12px;
                        font-weight: bold;
                        background-color: #0070EB;
                        color: #FFFFFF;
                    }
                    .badge-urgent {
                        background-color: #D32F2F;
                    }
                    .info-label {
                        font-weight: bold;
                        color: #555555;
                        display: block;
                        margin-bottom: 2px;
                        font-size: 13px;
                    }
                    .info-value {
                        color: #333333;
                        font-size: 14px;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <div class="title">${if (esReconocimiento) "Reporte de Reconocimiento" else "Reporte de Incidencia"}</div>
                        <div class="subtitle">${incidencia.fecha} - ${incidencia.hora} | Sistema EduConnet</div>
                    </div>

                    <div class="section">
                        <div class="section-title">Información Principal</div>
                        ${if (esReconocimiento) """
                        <table class="compact-table">
                            <tr>
                                <td style="width: 50%;">
                                    <span class="info-label">Estudiante</span>
                                    <span class="info-value">${incidencia.nombreEstudiante} ${incidencia.apellidoEstudiante}</span>
                                </td>
                                <td style="width: 50%;">
                                    <span class="info-label">Grado/Sección</span>
                                    <span class="info-value">${incidencia.grado}° ${incidencia.seccion}</span>
                                </td>
                            </tr>
                            <tr>
                                <td colspan="2">
                                    <span class="info-label">Tipo</span>
                                    <span class="info-value">${incidencia.tipo}</span>
                                </td>
                            </tr>
                            <tr>
                                <td colspan="2">
                                    <span class="info-label">Contacto apoderado</span>
                                    <span class="info-value">${incidencia.celularApoderado}</span>
                                </td>
                            </tr>
                        </table>
                        """ else """
                        <table class="compact-table">
                            <tr>
                                <td style="width: 50%;">
                                    <span class="info-label">Estudiante</span>
                                    <span class="info-value">${incidencia.nombreEstudiante} ${incidencia.apellidoEstudiante}</span>
                                </td>
                                <td style="width: 50%;">
                                    <span class="info-label">Grado/Sección</span>
                                    <span class="info-value">${incidencia.grado}° ${incidencia.seccion}</span>
                                </td>
                            </tr>
                            <tr>
                                <td style="width: 50%;">
                                    <span class="info-label">Tipo</span>
                                    <span class="info-value">${incidencia.tipo}</span>
                                </td>
                                <td style="width: 50%;">
                                    <span class="info-label">Nivel</span>
                                    <span class="info-value">
                                        <span class="badge ${if (incidencia.atencion.contains("Urgente")) "badge-urgent" else ""}">
                                            ${incidencia.atencion}
                                        </span>
                                    </span>
                                </td>
                            </tr>
                            <tr>
                                <td colspan="2">
                                    <span class="info-label">Contacto apoderado</span>
                                    <span class="info-value">${incidencia.celularApoderado}</span>
                                </td>
                            </tr>
                        </table>
                        """}
                    </div>

                    <div class="section">
                        <div class="section-title">${if (esReconocimiento) "Detalles del Reconocimiento" else "Detalles de la Incidencia"}</div>
                        <div class="highlight-box">
                            ${incidencia.detalle.replace("\n", "<br>")}
                        </div>
                    </div>

                    ${if (informe != null) """
                    <div class="section">
                        <div class="section-title">Informe de Seguimiento</div>
                        <table class="compact-table">
                            <tr>
                                <td style="width: 25%;">
                                    <span class="info-label">Fecha</span>
                                    <span class="info-value">${informe.createFecha ?: "N/A"}</span>
                                </td>
                                <td style="width: 25%;">
                                    <span class="info-label">Hora</span>
                                    <span class="info-value">${informe.createHora ?: "N/A"}</span>
                                </td>
                                <td style="width: 25%;">
                                    <span class="info-label">Apoderado</span>
                                    <span class="info-value">${informe.apoderado ?: "N/A"}</span>
                                </td>
                                <td style="width: 25%;">
                                    <span class="info-label">Relación</span>
                                    <span class="info-value">${informe.relacionFamiliar ?: "N/A"}</span>
                                </td>
                            </tr>
                        </table>
                        <div class="section-title">Acuerdos y conclusiones</div>
                        <div class="highlight-box">
                            ${informe.detalles?.replace("\n", "<br>") ?: "No se registraron detalles adicionales."}
                        </div>
                    </div>
                    """ else ""}

                    <div class="footer">
                        Documento confidencial - Generado automáticamente por EduConnet © ${java.time.Year.now().value}<br>
                        Todos los derechos reservados
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent()

        try {
            FileOutputStream(rutaSalida).use { outputStream ->
                HtmlConverter.convertToPdf(html, outputStream)
            }
        } catch (e: Exception) {
            throw e
        }
    }
}
