package com.example.pasipemunti.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.osmdroid.util.GeoPoint
import java.util.Date

// Type converter pentru List<GeoPoint>
data class GeoPointDTO(val lat: Double, val lon: Double, val ele: Double)

class GeoPointListConverter {

    @TypeConverter
    fun fromGeoPointList(value: List<GeoPoint>?): String? {
        if (value == null) return null
        val dtoList = value.map { GeoPointDTO(it.latitude, it.longitude, it.altitude) }
        return Gson().toJson(dtoList)
    }

    @TypeConverter
    fun toGeoPointList(value: String?): List<GeoPoint> {
        if (value.isNullOrBlank()) return emptyList()
        val listType = object : TypeToken<List<GeoPointDTO>>() {}.type
        val dtoList: List<GeoPointDTO> = Gson().fromJson(value, listType)
        return dtoList.map { GeoPoint(it.lat, it.lon, it.ele) }
    }
}

// Type converter pentru Date
class DateConverter {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}

@Entity(tableName = "gpx_trails")
@TypeConverters(GeoPointListConverter::class, DateConverter::class)
data class GPXTrailEntity(
    @PrimaryKey(autoGenerate = true) val id: Int? = null, // Make nullable
    val name: String,
    val description: String?,
    val points: List<GeoPoint>,
    val distance: Float,
    val elevation_gain: Float,
    val max_elevation: Float,
    val date: Date? = null,
    val duration: Long,
    val zone: String?,
    val resource_id: String?,
    val image_res_id: String?
) {fun toGPXTrail(): GPXTrail {
    return GPXTrail(
        id = this.id ?: 0, // Handle null case
        name = this.name,
        description = this.description,
        points = this.points,
        distance = this.distance,
        elevationGain = this.elevation_gain,
        maxElevation = this.max_elevation,
        date = this.date,
        duration = this.duration,
        zone = this.zone,
        resourceId = this.resource_id,
        imageResId = this.image_res_id
    )
}
}