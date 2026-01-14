package com.psm2.inva.presentation.features.species_details

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeciesDetailScreen(
    viewModel: SpeciesDetailViewModel,
    onNavigateUp: () -> Unit,
    onNavigateToEdit: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.deleteSuccess) {
        if (state.deleteSuccess) {
            Toast.makeText(context, "Species deleted successfully", Toast.LENGTH_SHORT).show()
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
        } else if (state.fish != null) {
            val fish = state.fish!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = padding.calculateBottomPadding())
                    .verticalScroll(rememberScrollState())
            ) {
                // 1. Header Image with Overlay Buttons
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {
                    if (fish.imageUrl.isNotBlank()) {
                        AsyncImage(
                            model = fish.imageUrl,
                            contentDescription = fish.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        // Gradient Overlay for visibility
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Black.copy(alpha = 0.6f), Color.Transparent)
                                    )
                                )
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.LightGray),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No Image", color = Color.White)
                        }
                    }

                    // Toolbar Buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, start = 16.dp, end = 16.dp),
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

                        // Options Menu (Admin Only)
                        if (state.isAdmin) {
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
                                        text = { Text("Edit Species") },
                                        onClick = {
                                            showMenu = false
                                            onNavigateToEdit()
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Delete Species", color = MaterialTheme.colorScheme.error) },
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

                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 2. Title & Subtitle
                    Column {
                        Text(
                            text = fish.name,
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        // Subtitle: Scientific Name • Local Name
                        val subtitle = if (fish.localName.isNotBlank()) {
                            "${fish.scientificName} • ${fish.localName}"
                        } else {
                            fish.scientificName
                        }
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontStyle = FontStyle.Italic,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        )
                    }

                    // 3. Conservation Status Box (Redesigned)
                    val isInvasive = fish.type.equals("Invasive", ignoreCase = true)
                    val isNative = fish.type.equals("Native", ignoreCase = true)

                    if (isInvasive || isNative) {
                        // Determine colors and message
                        val cardColor = if (isInvasive) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)
                        val contentColor = if (isInvasive) Color(0xFFD32F2F) else Color(0xFF2E7D32)
                        val icon = if (isInvasive) Icons.Default.Warning else Icons.Default.Info
                        val statusTitle = if (isInvasive) "Invasive Species" else "Native Species"
                        val statusDesc = if (isInvasive) {
                            "High risk to local ecosystem. Do not release back into water."
                        } else {
                            "Part of local ecosystem. Catch & release recommended."
                        }

                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = cardColor),
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(0.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.Top
                            ) {
                                // Icon Circle
                                Surface(
                                    color = contentColor.copy(alpha = 0.1f),
                                    shape = CircleShape,
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = contentColor,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Column {
                                    Text(
                                        text = "CONSERVATION STATUS",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp
                                        ),
                                        color = contentColor.copy(alpha = 0.8f)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = statusTitle,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = contentColor
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = statusDesc,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = contentColor.copy(alpha = 0.9f)
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider()

                    // 4. Description
                    if (fish.description.isNotBlank()) {
                        DetailSection(title = "Description", content = fish.description)
                    }

                    // 5. Habitat
                    if (fish.habitat.isNotBlank()) {
                        DetailSection(title = "Habitat", content = fish.habitat)
                    }

                    // 6. Diet
                    if (fish.diet.isNotBlank()) {
                        DetailSection(title = "Diet", content = fish.diet)
                    }

                    // 7. Origin
                    if (fish.origin.isNotBlank()) {
                        DetailSection(title = "Origin", content = fish.origin)
                    }

                    // 8. Impact
                    if (fish.impact.isNotBlank()) {
                        DetailSection(title = "Impact", content = fish.impact)
                    }

                    // 9. Conservation Status
                    // Display actual status from database, not hardcoded
                    DetailSection(title = "Conservation Status", content = fish.conservationStatus)

                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Species not found.")
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Species") },
            text = { Text("Are you sure you want to delete this species? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteSpecies()
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
fun DetailSection(title: String, content: String) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = content,
            style = MaterialTheme.typography.bodyLarge.copy(
                lineHeight = 24.sp
            )
        )
    }
}