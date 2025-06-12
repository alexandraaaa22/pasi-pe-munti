package com.example.pasipemunti.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pasipemunti.auth.UserPreferencesManager

@Composable
fun ProfileScreen(profileViewModel: ProfileViewModel) {
    // Apelăm o singură dată la prima compunere
    LaunchedEffect(Unit) {
        profileViewModel.loadUserInfo()
    }

    ProfileContent(profileViewModel)
}


@Composable
fun ProfileContent(profileViewModel: ProfileViewModel) {
    val userInfo by profileViewModel.userInfo.collectAsState()
    val isLoading by profileViewModel.isLoading.collectAsState()

    when {
        isLoading -> {
            // Indicator de încărcare
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        userInfo != null -> {
            // Conținutul profilului
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                ProfileHeader(userInfo!!) // Folosim `!!` doar după verificare cu `!= null`

                Spacer(modifier = Modifier.height(24.dp))

                PersonalInfoSection(userInfo!!)

                Spacer(modifier = Modifier.height(24.dp))

                ProfileOptions(onLogout = {
                    profileViewModel.logout()
                })
            }
        }

        else -> {
            // Dacă userInfo e null și nu e loading
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Nu s-au găsit date despre utilizator.")
            }
        }
    }
}



@Composable
fun ProfileHeader(userInfo: UserInfo) {
    // Avatar
    Box(
        modifier = Modifier
            .size(120.dp)
            .clip(CircleShape)
            .background(Color(0xff47B36D)),
        contentAlignment = Alignment.Center
    ) {
        // Folosește prima literă din firstName și lastName pentru avatar
        val initials = "${userInfo.firstName.firstOrNull() ?: ""}${userInfo.lastName.firstOrNull() ?: ""}"
        if (initials.isNotEmpty()) {
            Text(
                text = initials.uppercase(),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        } else {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Avatar",
                modifier = Modifier.size(60.dp),
                tint = Color.White
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Nume complet
    Text(
        text = "${userInfo.firstName} ${userInfo.lastName}",
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xff1a570a)
    )

    Text(
        text = "@${userInfo.username}",
        fontSize = 14.sp,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
    )
}

@Composable
fun PersonalInfoSection(userInfo: UserInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Informații personale",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xff1a570a),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            InfoItem(
                icon = Icons.Default.AccountCircle,
                label = "Nume utilizator",
                value = userInfo.username
            )

            Spacer(modifier = Modifier.height(12.dp))

            InfoItem(
                icon = Icons.Default.Email,
                label = "Email",
                value = userInfo.email
            )

            Spacer(modifier = Modifier.height(12.dp))

            InfoItem(
                icon = Icons.Default.Person,
                label = "Nume complet",
                value = "${userInfo.firstName} ${userInfo.lastName}"
            )

            if (userInfo.userId != null) {
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "ID utilizator: ${userInfo.userId}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
fun InfoItem(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color(0xff47B36D),
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = label,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Text(
                text = value,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun ProfileOptions(
    onLogout: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            OptionItem(
                icon = Icons.Default.Settings,
                text = "Setări",
                onClick = { /* TODO: Deschide setări */ }
            )

            OptionItem(
                icon = Icons.Default.Help,
                text = "Ajutor și suport",
                onClick = { /* TODO: Deschide ajutor */ }
            )

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
            )

            OptionItem(
                icon = Icons.Default.ExitToApp,
                text = "Deconectare",
                textColor = Color.Red,
                onClick = onLogout
            )
        }
    }
}

@Composable
fun OptionItem(
    icon: ImageVector,
    text: String,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = ButtonDefaults.textButtonColors(
            contentColor = textColor
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = text,
                fontSize = 16.sp
            )
        }
    }
}

// Data class pentru informațiile utilizatorului
data class UserInfo(
    val userId: Int?,
    val username: String,
    val email: String,
    val firstName: String,
    val lastName: String
)