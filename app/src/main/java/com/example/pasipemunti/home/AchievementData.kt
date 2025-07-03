package com.example.pasipemunti.home

data class Achievement(
    val id: Int,
    val name: String,
    val description: String,
    val iconName: String?,
    val progress: Float,
    val earned: Boolean
)

data class AchievementResponse(
    val achievements: List<Achievement>
)
