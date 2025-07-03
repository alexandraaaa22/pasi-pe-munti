package com.example.pasipemunti.home

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Calendar
import java.util.Locale

class HikingStatsViewModel : ViewModel() {
    private val _hikingData = mutableStateOf<List<Float>>(emptyList())
    val hikingData: State<List<Float>> = _hikingData

    private val _months = mutableStateOf<List<String>>(emptyList())
    val months: State<List<String>> = _months

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    private val _achievements = mutableStateOf<List<Achievement>>(emptyList())
    val achievements: State<List<Achievement>> = _achievements

    private val _isAchievementsLoading = mutableStateOf(false)
    val isAchievementsLoading: State<Boolean> = _isAchievementsLoading

    private val _achievementsError = mutableStateOf<String?>(null)
    val achievementsError: State<String?> = _achievementsError

    private val apiService: HikingApiService = createApiService()

    fun loadHikingData(userId: String, timeRange: TimeRange) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val response = apiService.getHikingStats(userId, timeRange.months)

                if (response.isSuccessful) {
                    val data = response.body()
                    if (data != null) {
                        val monthlyData = processHikingData(data.hikes, timeRange.months)
                        _hikingData.value = monthlyData.map { it.distance }
                        _months.value = monthlyData.map { it.month }
                    }
                } else {
                    _error.value = "Eroare la încărcarea datelor: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Eroare de rețea: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadAchievements(userId: String) {
        viewModelScope.launch {
            _isAchievementsLoading.value = true
            _achievementsError.value = null
            try {
                val response = apiService.getUserAchievements(userId.toInt().toString())
                if (response.isSuccessful) {
                    _achievements.value = response.body() ?: emptyList()
                } else {
                    _achievementsError.value = "Eroare la realizări: ${response.code()}"
                }
            } catch (e: Exception) {
                _achievementsError.value = "Eroare: ${e.message}"
            } finally {
                _isAchievementsLoading.value = false
            }
        }
    }

    private fun processHikingData(hikes: List<HikeData>, monthsCount: Int): List<HikeData> {
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)

        val monthsList = mutableListOf<HikeData>()

        for (i in 0 until monthsCount) {
            calendar.set(Calendar.MONTH, currentMonth - i)
            calendar.set(Calendar.YEAR, currentYear)

            if (calendar.get(Calendar.MONTH) < 0) {
                calendar.add(Calendar.YEAR, -1)
                calendar.set(Calendar.MONTH, 11 + calendar.get(Calendar.MONTH))
            }

            val monthName = calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault()) ?: ""
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)

            val monthDistance = hikes
                .filter { it.year == year && getMonthNumber(it.month) == month }
                .sumOf { it.distance.toDouble() }
                .toFloat()

            monthsList.add(0, HikeData(monthName, monthDistance, year))
        }

        return monthsList
    }

    private fun getMonthNumber(monthName: String): Int {
        return when (monthName.lowercase()) {
            "jan", "ianuarie" -> 0
            "feb", "februarie" -> 1
            "mar", "martie" -> 2
            "apr", "aprilie" -> 3
            "mai", "may" -> 4
            "jun", "iunie" -> 5
            "jul", "iulie" -> 6
            "aug", "august" -> 7
            "sep", "septembrie" -> 8
            "oct", "octombrie" -> 9
            "nov", "noiembrie" -> 10
            "dec", "decembrie" -> 11
            else -> 0
        }
    }

    private fun createApiService(): HikingApiService {
        val client = OkHttpClient.Builder().build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://5148-109-99-204-12.ngrok-free.app/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()

        return retrofit.create(HikingApiService::class.java)
    }
}