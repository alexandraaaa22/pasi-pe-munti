package com.example.pasipemunti.data

import org.osmdroid.util.GeoPoint
import java.util.Date

// modelul folosit in aplicatie pt un traseu montan in format gpx

data class GPXTrail(
    val id: Int,
    val name: String,
    val description: String?, // nullable
    val points: List<GeoPoint>,
    val distance: Float, // km
    val elevationGain: Float, // m
    val maxElevation: Float, // m
    val date: Date? = null,
    val duration: Long = 0, // secunde
    val zone: String?, // nullable
    val resourceId: String? = null,
    val imageResId: String? = null
)