package com.example.pasipemunti.home.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pasipemunti.home.models.Achievement
import com.example.pasipemunti.home.models.AppColors
import com.example.pasipemunti.home.utils.getAchievementColor
import com.example.pasipemunti.home.utils.getAchievementIcon

@Composable
fun AchievementsSection(
    achievements: List<Achievement>,
    isLoading: Boolean,
    error: String?
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with expand/collapse button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "Realizări",
                        tint = AppColors.accentOrange,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Realizări",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = AppColors.textDark
                    )

                    // Badge for earned achievements
                    val earnedCount = achievements.count { it.earned }
                    if (earnedCount > 0) {
                        Badge(
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .background(
                                    AppColors.accentOrange,
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = earnedCount.toString(),
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        tint = AppColors.textLight
                    )
                }
            }

            // Loading state
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = AppColors.primaryGreen,
                        strokeWidth = 2.dp
                    )
                }
            }
            // Error state
            else if (error != null) {
                Text(
                    text = error,
                    color = Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            // Content
            else {
                AnimatedVisibility(
                    visible = expanded,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(modifier = Modifier.padding(top = 8.dp)) {
                        achievements.forEach { achievement ->
                            AchievementItem(
                                title = achievement.name,
                                description = achievement.description,
                                progress = achievement.progress,
                                color = getAchievementColor(achievement.iconName),
                                icon = getAchievementIcon(achievement.iconName),
                                isEarned = achievement.earned
                            )
                        }
                    }
                }

                if (!expanded && achievements.isNotEmpty()) {
                    // Preview of achievements when collapsed
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        items(achievements.take(4)) { achievement ->
                            AchievementBadge(
                                progress = achievement.progress,
                                color = getAchievementColor(achievement.iconName),
                                icon = getAchievementIcon(achievement.iconName),
                                isEarned = achievement.earned
                            )
                        }

                        if (achievements.size > 4) {
                            item {
                                AchievementBadge(
                                    progress = 0f,
                                    color = AppColors.textLight,
                                    icon = Icons.Default.MoreHoriz,
                                    showMore = true
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AchievementItem(
    title: String,
    description: String,
    progress: Float,
    color: Color,
    icon: ImageVector,
    isEarned: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(
                if (isEarned) color.copy(alpha = 0.05f) else Color.Transparent,
                RoundedCornerShape(12.dp)
            )
            .padding(horizontal = if (isEarned) 12.dp else 0.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AchievementBadge(
            progress = progress,
            color = color,
            icon = icon,
            size = 50.dp,
            isEarned = isEarned
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    color = AppColors.textDark
                )

                if (isEarned) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Completed",
                        tint = color,
                        modifier = Modifier
                            .size(18.dp)
                            .padding(start = 4.dp)
                    )
                }
            }

            Text(
                text = description,
                fontSize = 14.sp,
                color = AppColors.textLight
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${(progress * 100).toInt()}%",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = if (isEarned) color else AppColors.textLight
            )

            if (isEarned) {
                Text(
                    text = "Obținută!",
                    fontSize = 12.sp,
                    color = color,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun AchievementBadge(
    progress: Float,
    color: Color,
    icon: ImageVector,
    size: Dp = 60.dp,
    isEarned: Boolean = false,
    showMore: Boolean = false
) {
    Box(contentAlignment = Alignment.Center) {
        // Progress indicator
        CircularProgressIndicator(
            progress = progress,
            modifier = Modifier.size(size),
            color = if (isEarned) color else color.copy(alpha = 0.6f),
            trackColor = color.copy(alpha = 0.2f),
            strokeWidth = 3.dp
        )

        // Center content
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(size - 8.dp)
                .clip(CircleShape)
                .background(
                    if (isEarned) {
                        Brush.radialGradient(
                            colors = listOf(
                                color.copy(alpha = 0.3f),
                                color.copy(alpha = 0.1f)
                            )
                        )
                    } else {
                        Brush.radialGradient(
                            colors = listOf(
                                color.copy(alpha = 0.15f),
                                color.copy(alpha = 0.05f)
                            )
                        )
                    }
                )
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isEarned) color else color.copy(alpha = 0.6f),
                modifier = Modifier.size(size / 2.2f)
            )
        }

        // Earned indicator
        if (isEarned) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Earned",
                tint = color,
                modifier = Modifier
                    .size(16.dp)
                    .offset(x = (size / 3), y = -(size / 3))
                    .background(Color.White, CircleShape)
            )
        }
    }
}