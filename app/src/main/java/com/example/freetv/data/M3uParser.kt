package com.example.freetv.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL
import java.util.Scanner

class M3uParser {

    suspend fun parseFromUrl(url: String): List<Channel> = withContext(Dispatchers.IO) {
        val channels = mutableListOf<Channel>()
        val content = URL(url).readText()
        if (content.isBlank()) throw Exception("El contenido del M3U está vacío")
        
        val scanner = Scanner(content)
        var currentNombre = "Desconocido"
        var currentLogo = ""
        var currentCategoria = "General"

        while (scanner.hasNextLine()) {
            val linea = scanner.nextLine()
            if (linea.startsWith("#EXTINF:-1")) {
                if (linea.contains("tvg-logo=\"")) {
                    val start = linea.indexOf("tvg-logo=\"") + 10
                    val end = linea.indexOf("\"", start)
                    if (start < end) currentLogo = linea.substring(start, end)
                }
                if (linea.contains("group-title=\"")) {
                    val start = linea.indexOf("group-title=\"") + 13
                    val end = linea.indexOf("\"", start)
                    if (start in 0 until linea.length && end > start) {
                        currentCategoria = linea.substring(start, end)
                    }
                }
                currentNombre = if (linea.contains(",")) {
                    linea.substringAfterLast(",").trim()
                } else {
                    "Canal sin nombre"
                }

                // Smart category guessing if it's "General" or empty
                if (currentCategoria == "General" || currentCategoria.isBlank()) {
                    val nameLow = currentNombre.lowercase()
                    currentCategoria = when {
                        nameLow.contains("news") || nameLow.contains("noticias") || nameLow.contains("24h") || nameLow.contains("tvc") || nameLow.contains("milenio") -> "Noticias"
                        nameLow.contains("sport") || nameLow.contains("espn") || nameLow.contains("fox") || nameLow.contains("deportes") || nameLow.contains("tudn") || nameLow.contains("gol") -> "Deportes"
                        nameLow.contains("movie") || nameLow.contains("cine") || nameLow.contains("hbo") || nameLow.contains("star") || nameLow.contains("golden") || nameLow.contains("axn") || nameLow.contains("warner") || nameLow.contains("tnt") -> "Cine y Series"
                        nameLow.contains("kids") || nameLow.contains("nickelodeon") || nameLow.contains("disney") || nameLow.contains("niños") || nameLow.contains("cartoon") || nameLow.contains("discovery kids") -> "Infantil"
                        nameLow.contains("music") || nameLow.contains("musica") || nameLow.contains("mtv") || nameLow.contains("telehit") || nameLow.contains("vh1") || nameLow.contains("bandamax") -> "Música"
                        nameLow.contains("documentary") || nameLow.contains("national") || nameLow.contains("discovery") || nameLow.contains("historia") || nameLow.contains("animal") || nameLow.contains("nature") -> "Cultura y Ciencia"
                        nameLow.contains("relig") || nameLow.contains("iglesia") || nameLow.contains("crist") || nameLow.contains("enlace") || nameLow.contains("vaticano") -> "Religión"
                        nameLow.contains("cocina") || nameLow.contains("food") || nameLow.contains("gourmet") || nameLow.contains("home") || nameLow.contains("style") -> "Estilo de Vida"
                        nameLow.contains("mexico") || nameLow.contains("azteca") || nameLow.contains("televisa") || nameLow.contains("canal") || nameLow.contains("estrellas") -> "Canales Nacionales"
                        else -> "Entretenimiento"
                    }
                } else {
                    // Translate known categories from M3U
                    currentCategoria = when (currentCategoria.lowercase()) {
                        "movies" -> "Cine y Series"
                        "sports" -> "Deportes"
                        "news" -> "Noticias"
                        "kids" -> "Infantil"
                        "music" -> "Música"
                        "documentary" -> "Cultura y Ciencia"
                        "entertainment" -> "Entretenimiento"
                        else -> currentCategoria
                    }
                }
            } else if (linea.startsWith("http")) {
                channels.add(
                    Channel(
                        nombre = currentNombre,
                        categoria = currentCategoria,
                        logoUrl = currentLogo,
                        streamUrl = linea.trim()
                    )
                )
            }
        }
        scanner.close()
        channels
    }
}
