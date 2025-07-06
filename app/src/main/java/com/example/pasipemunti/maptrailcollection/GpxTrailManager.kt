package com.example.pasipemunti.maptrailcollection

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pasipemunti.R
import com.example.pasipemunti.searchhike.SearchHikeViewModel
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.infowindow.InfoWindow
import org.osmdroid.views.overlay.infowindow.BasicInfoWindow
import org.w3c.dom.Document
import org.w3c.dom.NodeList
import java.io.InputStream
import javax.xml.parsers.DocumentBuilderFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.views.overlay.MapEventsOverlay
import kotlin.math.*

data class GpxTrail(
    val name: String,
    val points: List<GeoPoint>,
    val zone: String,
    val color: Int = Color.Blue.toArgb(),
    val distance: Double = 0.0, // în km
    val difficulty: TrailDifficulty = TrailDifficulty.MEDIUM
)

enum class TrailDifficulty(val displayName: String, val color: Int) {
    EASY("Ușor", Color.Green.toArgb()),
    MEDIUM("Mediu", Color(0xFFFF8C00).toArgb()), // Orange
    HARD("Dificil", Color.Red.toArgb()),
    EXPERT("Expert", Color(0xFF8B0000).toArgb()) // Dark Red
}

class GpxTrailManager(private val context: Context) {

    private val trailColors = listOf(
        Color(0xFF2196F3).toArgb(), // Blue
        Color(0xFFE91E63).toArgb(), // Pink
        Color(0xFF4CAF50).toArgb(), // Green
        Color(0xFF9C27B0).toArgb(), // Purple
        Color(0xFF00BCD4).toArgb(), // Cyan
        Color(0xFFFF5722).toArgb(), // Deep Orange
        Color(0xFF795548).toArgb(), // Brown
        Color(0xFF607D8B).toArgb(), // Blue Grey
    )

    private var currentInfoWindow: InfoWindow? = null

    suspend fun loadTrailsForZone(zoneName: String): List<GpxTrail> = withContext(Dispatchers.IO) {
        val trails = mutableListOf<GpxTrail>()

        try {
            val rawFiles = getRawResourceFiles()
            val zoneFiles = rawFiles.filter { fileName ->
                fileName.startsWith(getZonePrefix(zoneName), ignoreCase = true) &&
                        fileName.endsWith(".gpx", ignoreCase = true)
            }

            zoneFiles.forEachIndexed { index, fileName ->
                val resourceId = getResourceId(fileName)
                if (resourceId != 0) {
                    val inputStream = context.resources.openRawResource(resourceId)
                    val trail = parseGpxFile(inputStream, fileName, zoneName, index)
                    trail?.let { trails.add(it) }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        trails
    }

    private fun getZonePrefix(zoneName: String): String {
        return when (zoneName.lowercase()) {
            "munții făgăraș" -> "fagaras"
            "munții bucegi" -> "bucegi"
            "muntii piatra craiului" -> "crai"
            "munții postăvaru" -> "postavaru"
            "munții ceahlău" -> "ceahlau"
            "munții retezat" -> "retezat"
            "munții ciucaș" -> "ciucas"
            "munții apuseni" -> "apuseni"
            else -> zoneName.lowercase().replace(" ", "").replace("ă", "a").replace("î", "i").replace("ș", "s").replace("ț", "t")
        }
    }

    private fun getRawResourceFiles(): List<String> {
        return listOf(
            "ciucas_babarunca_cabana_vf_ciucas.gpx",
            "ciucas_cheia_saua_gropsoarele.gpx",
            "ciucas_pasul_bratocea_varful_ciucas.gpx",
            "ciucas_vama_buzaului_vf_ciucas.gpx",
            "ciucas_cabana_voina_refugiul_iezer.gpx",
            "ciucas_babarunca_cabana_vf_ciucas.gpx",

            "fagaras_cabana_negoiu_vf_negoiu.gpx",
            "faragaras_fereastra_zmeilor_cabana_podragu.gpx",
            "fagaras_piscul_negru_vf_lespezi.gpx",
            "fararas_stana_lui_burneei_vf_moldoveanu.gpx",
            "fagaras_valea_sambetei_fereastra_mica.gpx",

            "bucegi_piatra_arsa_caraiman_vf_omu.gpx",
            "bucegi_rasnov_cabana_malaiesti.gpx",
            "bucegi_cheile_tatarului_cabana_padina.gpx",
            "bucegi_valuea_gaura_vf_omu.gpx",

            "crai_fantana_lui_botorog_curmatura_piatra_mica.gpx",
            "crai_pestera_casa_folea_saua_joaca.gpx",
            "crai_zarnesti_padina_sindileriei_turnu_padina_hotarului.gpx",
            "crai_fanatana_lui_botorog_prapastiile_zarnestilor_cabana_curmatura.gpx"
        )
    }

    private fun getResourceId(fileName: String): Int {
        val resourceName = fileName.replace(".gpx", "").replace("-", "_").lowercase()
        return context.resources.getIdentifier(resourceName, "raw", context.packageName)
    }

    private fun calculateDistance(points: List<GeoPoint>): Double {
        if (points.size < 2) return 0.0

        var totalDistance = 0.0
        for (i in 0 until points.size - 1) {
            totalDistance += distanceBetween(points[i], points[i + 1])
        }
        return totalDistance / 1000.0 // Convert to km
    }

    private fun distanceBetween(point1: GeoPoint, point2: GeoPoint): Double {
        val earthRadius = 6371000.0 // Earth radius in meters
        val lat1Rad = Math.toRadians(point1.latitude)
        val lat2Rad = Math.toRadians(point2.latitude)
        val deltaLat = Math.toRadians(point2.latitude - point1.latitude)
        val deltaLon = Math.toRadians(point2.longitude - point1.longitude)

        val a = sin(deltaLat / 2) * sin(deltaLat / 2) +
                cos(lat1Rad) * cos(lat2Rad) * sin(deltaLon / 2) * sin(deltaLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadius * c
    }

    private fun determineDifficulty(fileName: String, distance: Double): TrailDifficulty {
        val name = fileName.lowercase()

        // Determine difficulty based on keywords and distance
        return when {
            name.contains("vf_") || name.contains("varful") || name.contains("moldoveanu") || name.contains("omu") -> {
                when {
                    distance > 15 -> TrailDifficulty.EXPERT
                    distance > 10 -> TrailDifficulty.HARD
                    else -> TrailDifficulty.MEDIUM
                }
            }
            name.contains("cabana") || name.contains("refugiul") -> {
                when {
                    distance > 12 -> TrailDifficulty.HARD
                    distance > 8 -> TrailDifficulty.MEDIUM
                    else -> TrailDifficulty.EASY
                }
            }
            name.contains("saua") || name.contains("curmatura") -> TrailDifficulty.MEDIUM
            name.contains("fereastra") || name.contains("prapastii") -> TrailDifficulty.HARD
            else -> {
                when {
                    distance > 12 -> TrailDifficulty.HARD
                    distance > 6 -> TrailDifficulty.MEDIUM
                    else -> TrailDifficulty.EASY
                }
            }
        }
    }

    private suspend fun parseGpxFile(
        inputStream: InputStream,
        fileName: String,
        zoneName: String,
        colorIndex: Int
    ): GpxTrail? = withContext(Dispatchers.IO) {
        try {
            val factory = DocumentBuilderFactory.newInstance()
            val builder = factory.newDocumentBuilder()
            val document: Document = builder.parse(inputStream)

            val trackPoints = mutableListOf<GeoPoint>()

            // Parse track points
            val trkptNodes: NodeList = document.getElementsByTagName("trkpt")
            for (i in 0 until trkptNodes.length) {
                val node = trkptNodes.item(i)
                val lat = node.attributes.getNamedItem("lat").nodeValue.toDouble()
                val lon = node.attributes.getNamedItem("lon").nodeValue.toDouble()
                trackPoints.add(GeoPoint(lat, lon))
            }

            // Parse waypoints if no track points found
            if (trackPoints.isEmpty()) {
                val wptNodes: NodeList = document.getElementsByTagName("wpt")
                for (i in 0 until wptNodes.length) {
                    val node = wptNodes.item(i)
                    val lat = node.attributes.getNamedItem("lat").nodeValue.toDouble()
                    val lon = node.attributes.getNamedItem("lon").nodeValue.toDouble()
                    trackPoints.add(GeoPoint(lat, lon))
                }
            }

            if (trackPoints.isNotEmpty()) {
                val distance = calculateDistance(trackPoints)
                val difficulty = determineDifficulty(fileName, distance)

                // Map de denumiri personalizate
                val customNames = mapOf(
                    "ciucas_babarunca_cabana_vf_ciucas" to "Babarunca – Cabana – Vf. Ciucaș",
                    "ciucas_cheia_saua_gropsoarele" to "Cheia – Șaua Gropșoarele",
                    "ciucas_pasul_bratocea_varful_ciucas" to "Pasul Bratocea – Vf. Ciucaș",
                    "ciucas_vama_buzaului_vf_ciucas" to "Vama Buzăului – Vf. Ciucaș",
                    "ciucas_cabana_voina_refugiul_iezer" to "Cabana Voina – Refugiul Iezer",
                    "fagaras_cabana_negoiu_vf_negoiu" to "Cabana Negoiu – Vf. Negoiu",
                    "faragaras_fereastra_zmeilor_cabana_podragu" to "Fereastra Zmeilor – Cabana Podragu",
                    "fagaras_piscul_negru_vf_lespezi" to "Piscul Negru – Vf. Lespezi",
                    "fararas_stana_lui_burneei_vf_moldoveanu" to "Stâna lui Burnei – Vf. Moldoveanu",
                    "fagaras_valea_sambetei_fereastra_mica" to "Valea Sâmbetei – Fereastra Mică",
                    "bucegi_piatra_arsa_caraiman_vf_omu" to "Piatra Arsă – Caraiman – Vf. Omu",
                    "bucegi_rasnov_cabana_malaiesti" to "Râșnov – Cabana Mălăiești",
                    "bucegi_cheile_tatarului_cabana_padina" to "Cheile Tătarului – Cabana Padina",
                    "bucegi_valuea_gaura_vf_omu" to "Valea Gaura – Vf. Omu",
                    "crai_fantana_lui_botorog_curmatura_piatra_mica" to "Fântâna lui Botorog – Curmătura – Piatra Mică",
                    "crai_pestera_casa_folea_saua_joaca" to "Peștera Casa Folea – Șaua Joaca",
                    "crai_zarnesti_padina_sindileriei_turnu_padina_hotarului" to "Zărnești – Padina Șindileriei – Turnu – Padina Hotarului",
                    "crai_fanatana_lui_botorog_prapastiile_zarnestilor_cabana_curmatura" to "Fântâna lui Botorog – Prăpăstiile Zărneștilor – Cabana Curmătura"
                )

                val fileKey = fileName.removeSuffix(".gpx").lowercase()
                val trailName = customNames[fileKey]
                    ?: fileKey.replace("_", " ")
                        .replaceFirst(Regex("^[a-z]+\\s+"), "") // elimină prefixul zonei din nume

                val color = trailColors[colorIndex % trailColors.size]

                GpxTrail(
                    name = trailName,
                    points = trackPoints,
                    zone = zoneName,
                    color = color,
                    distance = distance,
                    difficulty = difficulty
                )
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            inputStream.close()
        }
    }


    fun addTrailsToMap(
        mapView: MapView,
        trails: List<GpxTrail>,
        viewModel: SearchHikeViewModel
    ) {
        // Remove existing polylines
        mapView.overlays.removeAll { it is Polyline }

        trails.forEach { trail ->
            val polyline = Polyline(mapView).apply {
                setPoints(trail.points)
                color = trail.color
                width = 8f // Increased width for better visibility

                setOnClickListener { polyline, mapView, eventPos ->
                    currentInfoWindow?.close()
                    val infoWindow = createTrailInfoWindow(mapView, trail, viewModel, mapView.context)
                    currentInfoWindow = infoWindow
                    infoWindow.open(polyline, eventPos, 0, -50)
                    true
                }

            }
            mapView.overlays.add(polyline)
        }

        if (trails.isNotEmpty() && trails.first().points.isNotEmpty()) {
            val bounds = calculateBounds(trails.flatMap { it.points })
            mapView.zoomToBoundingBox(bounds, false, 100)
        }

        val mapEventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                currentInfoWindow?.close()
                currentInfoWindow = null
                return true
            }

            override fun longPressHelper(p: GeoPoint?): Boolean {
                return false
            }
        })
        mapView.overlays.add(0, mapEventsOverlay)

        mapView.invalidate()
    }

    fun createTrailInfoWindow(
        mapView: MapView,
        trail: GpxTrail,
        viewModel: SearchHikeViewModel,
        context: Context
    ): InfoWindow
    {
        val context = mapView.context

        val contentLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 24, 32, 24)
            setBackgroundColor(android.graphics.Color.WHITE)

            background = GradientDrawable().apply {
                setColor(android.graphics.Color.WHITE)
                cornerRadius = 24f
                setStroke(2, Color(0xFFE0E0E0).toArgb())
            }

            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        // Titlul traseului
        val titleView = TextView(context).apply {
            text = trail.name
            textSize = 16f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color(0xFF2E7D32).toArgb())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 16 }
        }
        contentLayout.addView(titleView)

        // Distanța
        val distanceView = TextView(context).apply {
            text = "📏 Distanță: ${String.format("%.1f", trail.distance)} km"
            textSize = 14f
            setTextColor(Color(0xFF333333).toArgb())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 8 }
        }
        contentLayout.addView(distanceView)

        // Dificultatea
        val difficultyView = TextView(context).apply {
            text = "⛰️ Dificultate: ${trail.difficulty.displayName}"
            textSize = 14f
            setTypeface(null, Typeface.BOLD)
            setTextColor(trail.difficulty.color)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 8 }
        }
        contentLayout.addView(difficultyView)

        // 🔘 Buton START (pe care îl vom conecta ulterior)
        val startButton = TextView(context).apply {
            text = "▶ Start"
            textSize = 14f
            setTypeface(null, Typeface.BOLD)
            setTextColor(android.graphics.Color.WHITE)
            setBackgroundColor(Color(0xFF4CAF50).toArgb())
            setPadding(32, 16, 32, 16)
            setOnClickListener {
                // Setează punctele traseului și numele locațiilor
                viewModel.routePoints = trail.points
                viewModel.start = "Start automat"
                viewModel.end = "Finish automat"

                if (viewModel.locationPermissionGranted) {
                    viewModel.startNavigation()
                    Toast.makeText(context, "Navigarea a început", Toast.LENGTH_SHORT).show()
                    currentInfoWindow?.close()
                } else {
                    Toast.makeText(context, "Permisiunile lipsesc pentru locație!", Toast.LENGTH_SHORT).show()
                }
            }

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 16
                gravity = android.view.Gravity.CENTER_HORIZONTAL
            }
        }
        contentLayout.addView(startButton)

        return object : InfoWindow(contentLayout, mapView) {
            override fun onOpen(item: Any?) {
                // nimic aici momentan
            }

            override fun onClose() {
                // cleanup dacă e nevoie
            }
        }
    }


    private fun calculateBounds(points: List<GeoPoint>): org.osmdroid.util.BoundingBox {
        if (points.isEmpty()) {
            return org.osmdroid.util.BoundingBox(46.0, 26.0, 45.0, 25.0) // Romania default
        }

        var minLat = points.first().latitude
        var maxLat = points.first().latitude
        var minLon = points.first().longitude
        var maxLon = points.first().longitude

        points.forEach { point ->
            minLat = minOf(minLat, point.latitude)
            maxLat = maxOf(maxLat, point.latitude)
            minLon = minOf(minLon, point.longitude)
            maxLon = maxOf(maxLon, point.longitude)
        }

        return org.osmdroid.util.BoundingBox(maxLat, maxLon, minLat, minLon)
    }
}