package com.example.pasipemunti.maptrailcollection

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polyline
import org.w3c.dom.Document
import org.w3c.dom.NodeList
import java.io.InputStream
import javax.xml.parsers.DocumentBuilderFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class GpxTrail(
    val name: String,
    val points: List<GeoPoint>,
    val zone: String,
    val color: Int = Color.Blue.toArgb()
)

class GpxTrailManager(private val context: Context) {

    private val trailColors = listOf(
        Color.Blue.toArgb(),
        Color.Red.toArgb(),
        Color.Green.toArgb(),
        Color.Magenta.toArgb(),
        Color.Cyan.toArgb(),
        Color(0xFFFF8C00).toArgb(), // Dark Orange
        Color(0xFF8A2BE2).toArgb(), // Blue Violet
        Color(0xFF00CED1).toArgb(), // Dark Turquoise
    )

    suspend fun loadTrailsForZone(zoneName: String): List<GpxTrail> = withContext(Dispatchers.IO) {
        val trails = mutableListOf<GpxTrail>()

        try {
            // Obține lista tuturor fișierelor din raw
            val rawFiles = getRawResourceFiles()

            // Filtrează fișierele care încep cu numele zonei
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
                val trailName = fileName.replace(".gpx", "").replace("_", " ").split(" ").drop(1).joinToString(" ")
                val color = trailColors[colorIndex % trailColors.size]

                GpxTrail(
                    name = trailName.ifEmpty { fileName },
                    points = trackPoints,
                    zone = zoneName,
                    color = color
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

    fun addTrailsToMap(mapView: MapView, trails: List<GpxTrail>) {
        mapView.overlays.removeAll { it is Polyline }

        trails.forEach { trail ->
            val polyline = Polyline(mapView).apply {
                setPoints(trail.points)
                color = trail.color
                width = 5f
                title = trail.name
            }
            mapView.overlays.add(polyline)
        }

        if (trails.isNotEmpty() && trails.first().points.isNotEmpty()) {
            val bounds = calculateBounds(trails.flatMap { it.points })
            mapView.zoomToBoundingBox(bounds, false, 100)
        }

        mapView.invalidate()
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