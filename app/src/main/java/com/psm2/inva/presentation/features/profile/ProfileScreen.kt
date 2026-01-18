package com.psm2.inva.presentation.features.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.psm2.inva.model.Sighting
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    state: ProfileState,
    onSignOut: () -> Unit,
    onNavigateToCatchDetail: (String) -> Unit,
    onNavigateToEditProfile: () -> Unit = {} // Added default for compatibility
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { }, // Removed "Profile" text
                actions = {
                    // Edit Profile Button
                    IconButton(onClick = onNavigateToEditProfile) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Profile",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    // Sign Out Button
                    IconButton(onClick = onSignOut) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Sign Out",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // --- Header Section ---
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    if (state.signedInUser?.profilePictureUrl != null) {
                        AsyncImage(
                            model = state.signedInUser.profilePictureUrl,
                            contentDescription = "Profile picture",
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = state.signedInUser?.username?.take(1)?.uppercase() ?: "U",
                                style = MaterialTheme.typography.headlineLarge,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Full Name (Primary)
                    val fullName = if (!state.signedInUser?.firstName.isNullOrBlank() || !state.signedInUser?.lastName.isNullOrBlank()) {
                        "${state.signedInUser?.firstName.orEmpty()} ${state.signedInUser?.lastName.orEmpty()}".trim()
                    } else {
                        "Guest User"
                    }

                    Text(
                        text = fullName,
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Username (Secondary)
                    if (!state.signedInUser?.username.isNullOrBlank()) {
                        Text(
                            text = "${state.signedInUser?.username}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // --- Impact Stats Section (Individual Cards) ---
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Total Catches Card
                    StatCard(
                        label = "Total Catches",
                        value = state.sightingCount.toString(),
                        color = MaterialTheme.colorScheme.primary,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.weight(1f)
                    )

                    // Invasive Removed Card
                    StatCard(
                        label = "Invasive Removed",
                        value = state.invasiveRemovedCount.toString(),
                        color = Color.Red,
                        containerColor = Color(0xFFFFEBEE), // Light Red
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // --- My Catches Header (Tab A) ---
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "My Catches",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            }

            // --- My Catches List ---
            if (state.userSightings.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No catches yet. Start fishing!", color = Color.Gray)
                    }
                }
            } else {
                items(state.userSightings) { sighting ->
                    UserCatchItem(sighting = sighting, onClick = { onNavigateToCatchDetail(sighting.id) })
                }
            }
        }
    }
}

@Composable
fun StatCard(
    label: String,
    value: String,
    color: Color,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(0.dp) // Flat look
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = color
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = color.copy(alpha = 0.8f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
fun UserCatchItem(sighting: Sighting, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
            .clickable(onClick = onClick) // Make clickable
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Photo
            AsyncImage(
                model = sighting.imageUrl,
                contentDescription = sighting.speciesName,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.LightGray), // Placeholder background
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Center: Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = sighting.speciesName,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(4.dp))

                val sdf = remember { SimpleDateFormat("dd MMM", Locale.getDefault()) }
                val dateStr = sdf.format(sighting.date.toDate())
                val locationStr = if (sighting.locationDescription.isNotBlank()) sighting.locationDescription else "Unknown Location"

                Text(
                    text = "$locationStr • $dateStr",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 1
                )
            }
        }
    }
}