package com.psm2.inva.presentation.features.catch_detail

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SetMeal
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatchDetailScreen(
    viewModel: CatchDetailViewModel,
    onNavigateUp: () -> Unit,
    onNavigateToEdit: (String) -> Unit = {} // Added with default to match SpeciesDetail style
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.deleteSuccess) {
        if (state.deleteSuccess) {
            Toast.makeText(context, "Catch deleted successfully", Toast.LENGTH_SHORT).show()
            viewModel.resetDeleteState()
            onNavigateUp()
        }
    }

    LaunchedEffect(state.deleteError) {
        state.deleteError?.let {
            Toast.makeText(context, "Error: $it", Toast.LENGTH_LONG).show()
        }
    }

    Scaffold { padding ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (state.sighting != null) {
            val sighting = state.sighting!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = padding.calculateBottomPadding())
                    .verticalScroll(rememberScrollState())
            ) {
                // --- 1. Header Image with Overlay Buttons ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp) // Matched to SpeciesDetailScreen
                ) {
                    if (sighting.imageUrl.isNotBlank()) {
                        AsyncImage(
                            model = sighting.imageUrl,
                            contentDescription = sighting.speciesName,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.LightGray),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No Image Available", color = Color.Gray)
                        }
                    }

                    // Top Gradient for Button Visibility
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Black.copy(alpha = 0.6f), Color.Transparent)
                                )
                            )
                            .align(Alignment.TopCenter)
                    )

                    // Toolbar Overlay
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, start = 16.dp, end = 16.dp), // Matched padding to SpeciesDetailScreen
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Back Button
                        IconButton(
                            onClick = onNavigateUp,
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }

                        // Options Menu (Edit/Delete)
                        if (state.isOwnerOrAdmin) {
                            Box {
                                IconButton(
                                    onClick = { showMenu = true },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = "Options",
                                        tint = Color.White
                                    )
                                }
                                DropdownMenu(
                                    expanded = showMenu,
                                    onDismissRequest = { showMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Edit Catch") },
                                        onClick = {
                                            showMenu = false
                                            onNavigateToEdit(sighting.id)
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                                        onClick = {
                                            showMenu = false
                                            showDeleteDialog = true
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // --- 2. Details Content ---
                Column(modifier = Modifier.padding(16.dp)) {
                    // Header: Name and Badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = sighting.speciesName,
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.weight(1f)
                        )

                        // Action Badge
                        val isRemoved = sighting.actionTaken.equals("Removed", ignoreCase = true)
                        val isReleased = sighting.actionTaken.equals("Released", ignoreCase = true)
                        val badgeText = if (isRemoved) "REMOVED" else if (isReleased) "RELEASED" else sighting.actionTaken.uppercase()
                        val badgeColor = if (isRemoved) Color.Red else if (isReleased) Color(0xFF4CAF50) else Color.Gray
                        val containerColor = badgeColor.copy(alpha = 0.1f)

                        Surface(
                            color = containerColor,
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, badgeColor)
                        ) {
                            Text(
                                text = badgeText,
                                color = badgeColor,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Category Badge
                    val isInvasive = sighting.category.equals("Invasive", ignoreCase = true)
                    Surface(
                        color = if (isInvasive) Color(0xFFFFEBEE) else Color(0xFFE8F5E9),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = sighting.category.uppercase(),
                            color = if (isInvasive) Color.Red else Color(0xFF2E7D32),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                    // Angler Stats Grid
                    Text("Catch Stats", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        StatBox(
                            icon = Icons.Default.Straighten,
                            value = if (sighting.length > 0) "${sighting.length} cm" else "N/A",
                            label = "Length"
                        )
                        StatBox(
                            icon = Icons.Default.FitnessCenter,
                            value = if (sighting.weight > 0) "${sighting.weight} kg" else "N/A",
                            label = "Weight"
                        )
                        StatBox(
                            icon = Icons.Default.SetMeal,
                            value = "${sighting.quantity}",
                            label = "Quantity"
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                    // Details Section
                    DetailRow(
                        icon = Icons.Default.AccessTime,
                        label = "Date & Time",
                        value = remember(sighting.date) {
                            SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(sighting.date.toDate())
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    DetailRow(
                        icon = Icons.Default.LocationOn,
                        label = "Location",
                        value = if (sighting.locationDescription.isNotBlank()) sighting.locationDescription else "Unknown Location"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (sighting.baitUsed.isNotBlank()) {
                        DetailRow(
                            icon = Icons.Default.SetMeal, // Reusing icon for bait
                            label = "Bait Used",
                            value = sighting.baitUsed
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    DetailRow(
                        icon = Icons.Default.Person,
                        label = "Angler",
                        value = sighting.username.ifBlank { "Anonymous" }
                    )
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(state.error ?: "Unknown Error")
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Catch") },
            text = { Text("Are you sure you want to delete this catch record? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteSighting()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun StatBox(icon: androidx.compose.ui.graphics.vector.ImageVector, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
    }
}

@Composable
fun DetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Text(value, style = MaterialTheme.typography.bodyLarge)
        }
    }
}