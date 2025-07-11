package com.example.pasipemunti.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun ProfileScreen(profileViewModel: ProfileViewModel) {

    // la prima afisare incarcam datele utilizatorului
    LaunchedEffect(Unit) {
        profileViewModel.loadUserInfo()
    }

    ProfileContent(profileViewModel)
}

@Composable
fun ProfileContent(profileViewModel: ProfileViewModel) {

    // preluam starile din view model
    val userInfo by profileViewModel.userInfo.collectAsState()
    val isLoading by profileViewModel.isLoading.collectAsState()

    // in functie de stari afisam un spinner de incarcare sau informatiile din profil
    when {
        isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        userInfo != null -> {
            // continutul profilului
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                ProfileHeader(
                    userInfo = userInfo!!,
                    onProfilePictureChange = { uri ->
                        profileViewModel.updateProfilePicture(uri.toString())
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                PersonalInfoSection(userInfo = userInfo!!)

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        else -> {
            // daca userInfo e null și nu e loading
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Nu s-au gasit date despre utilizator")
            }
        }
    }
}

@Composable
fun ProfileHeader(
    userInfo: UserInfo,
    onProfilePictureChange: (Uri) -> Unit
) {
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onProfilePictureChange(it) }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(120.dp)
        ) {
            if (userInfo.profilePictureUri != null) {
                // afisam poza de profil
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(userInfo.profilePictureUri)
                        .build(),
                    contentDescription = "Poza de profil",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .clickable { imagePickerLauncher.launch("image/*") },
                    contentScale = ContentScale.Crop
                )
            } else {
                // avatar cu initiale daca nu a pus poza de profil
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color(0xff47B36D))
                        .clickable { imagePickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
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
            }

            // buton pt editare poza
            FloatingActionButton(
                onClick = { imagePickerLauncher.launch("image/*") },
                modifier = Modifier
                    .size(36.dp)
                    .align(Alignment.BottomEnd),
                containerColor = Color(0xff47B36D),
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Schimbă poza",
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // nume user
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
                label = "Prenume",
                value = userInfo.firstName
            )

            Spacer(modifier = Modifier.height(12.dp))

            InfoItem(
                icon = Icons.Default.Person,
                label = "Nume de familie",
                value = userInfo.lastName
            )
        }
    }
}

@Composable
fun InfoItem(
    icon: ImageVector,
    label: String,
    value: String
) {
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

        Column(modifier = Modifier.weight(1f)) {
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

data class UserInfo(
    val username: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val profilePictureUri: String? = null
)