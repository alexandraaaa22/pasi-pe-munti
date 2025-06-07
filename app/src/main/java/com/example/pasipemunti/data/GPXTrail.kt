package com.example.pasipemunti.data

import org.osmdroid.util.GeoPoint
import java.util.Date

data class GPXTrail(
    val id: Int, // ID-ul din baza de date
    val name: String,
    val description: String?, // Nullable
    val points: List<GeoPoint>,
    val distance: Float, // În kilometri
    val elevationGain: Float, // În metri
    val maxElevation: Float, // În metri
    val date: Date? = null,
    val duration: Long = 0, // În secunde
    val zone: String?, // Zona traseului (nullable)
    val resourceId: String? = null, // ID-ul resursei (string)
    val imageResId: String? = null // ID-ul imaginii resursei (string)
)