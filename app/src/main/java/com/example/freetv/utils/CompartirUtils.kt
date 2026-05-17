package com.example.freetv.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.freetv.data.Channel

object CompartirUtils {

    fun compartirCanal(
        contexto: Context,
        canal: Channel
    ) {
        val textoParaCompartir = crearTextoParaCompartir(canal)

        val intentCompartir = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, textoParaCompartir)
        }

        val menuCompartir = Intent.createChooser(
            intentCompartir,
            "Compartir canal con..."
        )

        try {
            contexto.startActivity(menuCompartir)
        } catch (error: ActivityNotFoundException) {
            Toast.makeText(
                contexto,
                "Sin aplicaciones compatibles para compartir.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun crearTextoParaCompartir(canal: Channel): String {
        return buildString {
            appendLine("¡Mira esto! El canal ${canal.nombre} está disponible en FreeTV 📺")
            appendLine()

            if (canal.categoria.isNotBlank()) {
                appendLine("Categoría: ${canal.categoria}")
            }

            if (canal.descripcion.isNotBlank()) {
                appendLine("Descripción: ${canal.descripcion}")
            }

            if (canal.streamUrl.isNotBlank()) {
                appendLine()
                appendLine("Enlace del canal:")
                appendLine(canal.streamUrl)
            }

            appendLine()
            appendLine("Únete y mira televisión de forma sencilla con FreeTV.")
        }
    }
}