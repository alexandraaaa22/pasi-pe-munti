package com.example.pasipemunti.traillist

import android.content.Context
import android.util.Log
import org.osmdroid.util.GeoPoint
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

data class GPXTrail(
    val name: String,
    val description: String,
    val points: List<GeoPoint>,
    val distance: Float,
    val elevationGain: Float,
    val maxElevation: Float,
    val date: Date? = null,
    val duration: Long = 0, // in seconds
    val resourceId: Int = 0,  // Resource ID for the GPX file
    val imageResId: Int = 0   // Resource ID for the trail image
)

class GPXParser {
    companion object {
        private const val TAG = "GPXParser"
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
    }

    fun parse(context: Context, resourceId: Int, imageResId: Int = 0): GPXTrail? {
        try {
            val inputStream = context.resources.openRawResource(resourceId)
            return parseGPX(inputStream, resourceId, imageResId)
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing GPX file: ${e.message}")
            return null
        }
    }

    private fun parseGPX(inputStream: InputStream, resourceId: Int, imageResId: Int): GPXTrail? {
        var name = ""
        var description = ""
        var points = mutableListOf<GeoPoint>()
        var date: Date? = null
        var startTime: Date? = null
        var endTime: Date? = null

        var lat: Double? = null
        var lon: Double? = null
        var ele: Float = 0f
        val elevations = mutableListOf<Float>()

        var inTrackPoint = false
        var inName = false
        var inDesc = false
        var inElevation = false
        var inTime = false

        try {
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = true
            val parser = factory.newPullParser()
            parser.setInput(inputStream, null)

            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                val tagName = parser.name

                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        when (tagName) {
                            "trkpt", "wpt" -> {
                                inTrackPoint = true
                                lat = parser.getAttributeValue(null, "lat")?.toDoubleOrNull()
                                lon = parser.getAttributeValue(null, "lon")?.toDoubleOrNull()
                            }
                            "name" -> inName = true
                            "desc" -> inDesc = true
                            "ele" -> inElevation = true
                            "time" -> inTime = true
                        }
                    }
                    XmlPullParser.TEXT -> {
                        val text = parser.text.trim()
                        when {
                            inName && !inTrackPoint -> name = text
                            inDesc && !inTrackPoint -> description = text
                            inElevation && inTrackPoint -> {
                                ele = text.toFloatOrNull() ?: 0f
                                elevations.add(ele)
                            }
                            inTime && inTrackPoint -> {
                                try {
                                    val pointTime = dateFormat.parse(text)
                                    if (startTime == null) startTime = pointTime
                                    endTime = pointTime
                                    if (date == null) date = pointTime
                                } catch (e: Exception) {
                                    Log.w(TAG, "Failed to parse time: $text")
                                }
                            }
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        when (tagName) {
                            "trkpt", "wpt" -> {
                                inTrackPoint = false
                                if (lat != null && lon != null) {
                                    points.add(GeoPoint(lat!!, lon!!, ele.toDouble()))
                                }
                                lat = null
                                lon = null
                            }
                            "name" -> inName = false
                            "desc" -> inDesc = false
                            "ele" -> inElevation = false
                            "time" -> inTime = false
                        }
                    }
                }
                eventType = parser.next()
            }

            // Calculate distance
            var distance = 0f
            for (i in 0 until points.size - 1) {
                distance += calculateDistance(
                    points[i].latitude, points[i].longitude,
                    points[i + 1].latitude, points[i + 1].longitude
                )
            }

            // Calculate elevation gain
            var elevationGain = 0f
            for (i in 0 until elevations.size - 1) {
                val diff = elevations[i + 1] - elevations[i]
                if (diff > 0) elevationGain += diff
            }

            // Calculate duration
            val duration = if (startTime != null && endTime != null) {
                (endTime!!.time - startTime!!.time) / 1000
            } else 0

            val maxElevation = elevations.maxOrNull() ?: 0f

            return GPXTrail(
                name = name.ifEmpty { "Unnamed Trail" },
                description = description.ifEmpty { "No description available" },
                points = points,
                distance = distance,
                elevationGain = elevationGain,
                maxElevation = maxElevation,
                date = date,
                duration = duration,
                resourceId = resourceId,
                imageResId = imageResId
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing GPX: ${e.message}")
            e.printStackTrace()
            return null
        } finally {
            try {
                inputStream.close()
            } catch (e: Exception) {
                Log.e(TAG, "Error closing stream: ${e.message}")
            }
        }
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        val r = 6371000 // Earth radius in meters
        val phi1 = Math.toRadians(lat1)
        val phi2 = Math.toRadians(lat2)
        val deltaPhi = Math.toRadians(lat2 - lat1)
        val deltaLambda = Math.toRadians(lon2 - lon1)

        val a = Math.sin(deltaPhi / 2) * Math.sin(deltaPhi / 2) +
                Math.cos(phi1) * Math.cos(phi2) *
                Math.sin(deltaLambda / 2) * Math.sin(deltaLambda / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

        return (r * c).toFloat()
    }
}