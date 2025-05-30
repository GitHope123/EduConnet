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
        val html = """
            <html>
            <head>
                <style>
                    body {
                        font-family: 'Arial', sans-serif;
                        line-height: 1.6;
                        color: #333;
                        padding: 25px;
                        max-width: 800px;
                        margin: 0 auto;
                    }
                    .header {
                        text-align: center;
                        margin-bottom: 30px;
                        padding-bottom: 20px;
                        border-bottom: 2px solid #2c3e50;
                    }
                    .title {
                        color: #2c3e50;
                        font-size: 24px;
                        margin-bottom: 5px;
                    }
                    .subtitle {
                        color: #7f8c8d;
                        font-size: 14px;
                    }
                    .section {
                        margin: 25px 0;
                        padding: 20px;
                        background-color: #f8f9fa;
                        border-radius: 8px;
                        box-shadow: 0 2px 4px rgba(0,0,0,0.05);
                    }
                    .section-title {
                        color: #3498db;
                        font-weight: bold;
                        font-size: 18px;
                        margin-bottom: 15px;
                        padding-bottom: 8px;
                        border-bottom: 1px solid #eee;
                    }
                    .info-grid {
                        display: grid;
                        grid-template-columns: 160px 1fr;
                        gap: 12px;
                        margin-bottom: 10px;
                    }
                    .info-label {
                        font-weight: bold;
                        color: #7f8c8d;
                    }
                    .info-value {
                        color: #2c3e50;
                    }
                    .highlight-box {
                        background-color: #e8f4fd;
                        padding: 15px;
                        border-radius: 5px;
                        border-left: 4px solid #3498db;
                        margin: 15px 0;
                    }
                    .signature-area {
                        margin-top: 40px;
                        padding-top: 20px;
                        border-top: 1px dashed #ccc;
                        display: grid;
                        grid-template-columns: 1fr 1fr;
                        gap: 30px;
                    }
                    .footer {
                        margin-top: 30px;
                        text-align: center;
                        font-size: 12px;
                        color: #95a5a6;
                    }
                    .badge {
                        display: inline-block;
                        padding: 3px 8px;
                        border-radius: 4px;
                        font-size: 12px;
                        font-weight: bold;
                    }
                    .badge-urgent {
                        background-color: #ffebee;
                        color: #c62828;
                    }
                    .badge-normal {
                        background-color: #e8f5e9;
                        color: #2e7d32;
                    }
                </style>
            </head>
            <body>
                <div class="header">
                    <div class="title">REPORTE DE INCIDENCIA</div>
                    <div class="subtitle">${incidencia.fecha} - ${incidencia.hora}</div>
                </div>
                
                <div class="section">
                    <div class="section-title">INFORMACIÓN PRINCIPAL</div>
                    <div class="info-grid">
                        <div class="info-label">Estudiante:</div>
                        <div class="info-value">${incidencia.nombreEstudiante} ${incidencia.apellidoEstudiante}</div>
                        <div class="info-label">Grado/Sección:</div>
                        <div class="info-value">${incidencia.grado}° ${incidencia.seccion}</div>
                        <div class="info-label">Tipo de incidencia:</div>
                        <div class="info-value">${incidencia.tipo}</div>
                        <div class="info-label">Nivel de atención:</div>
                        <div class="info-value">
                            <span class="badge ${if (incidencia.atencion.contains("Urgente")) "badge-urgent" else "badge-normal"}">
                                ${incidencia.atencion}
                            </span>
                        </div>
                        <div class="info-label">Contacto apoderado:</div>
                        <div class="info-value">${incidencia.celularApoderado}</div>
                    </div>
                </div>

                <div class="section">
                    <div class="section-title">DETALLES DE LA INCIDENCIA</div>
                    <div class="highlight-box">
                        ${incidencia.detalle.replace("\n", "<br>")}
                    </div>
                    ${if (!incidencia.imageUri.isNullOrEmpty()) """
                        <div class="info-grid">
                            <div class="info-label">Evidencia:</div>
                            <div class="info-value">Documento adjunto</div>
                        </div>
                    """ else ""}
                </div>

                ${if (informe != null) """
                <div class="section">
                    <div class="section-title">INFORME DE SEGUIMIENTO</div>
                    <div class="info-grid">
                        <div class="info-label">Fecha informe:</div>
                        <div class="info-value">${informe.createFecha ?: "N/A"}</div>
                        <div class="info-label">Hora informe:</div>
                        <div class="info-value">${informe.createHora ?: "N/A"}</div>
                        <div class="info-label">Apoderado:</div>
                        <div class="info-value">${informe.apoderado ?: "N/A"}</div>
                        <div class="info-label">Relación familiar:</div>
                        <div class="info-value">${informe.relacionFamiliar ?: "N/A"}</div>
                    </div>
                    <div class="highlight-box">
                        <strong>Acuerdos y conclusiones:</strong><br>
                        ${informe.detalles?.replace("\n", "<br>") ?: "No se registraron detalles adicionales."}
                    </div>
                </div>
                """ else ""}

                <div class="footer">
                    Documento generado automáticamente - Sistema EduConnet © ${java.time.Year.now().value}
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
