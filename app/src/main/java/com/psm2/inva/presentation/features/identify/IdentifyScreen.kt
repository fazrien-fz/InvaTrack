package com.psm2.inva.presentation.features.identify

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.psm2.inva.model.Fish

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IdentifyScreen(
    state: FishListState,
    onNavigateToDetails: (String) -> Unit,
    onNavigateToClassification: (Uri) -> Unit,
    onNavigateToAddFish: () -> Unit
) {
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            if (uri != null) {
                onNavigateToClassification(uri)
            }
        }
    )

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    // Search Bar inside TopAppBar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Name or Scientific Name") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp), // Compact height
                        shape = RoundedCornerShape(15.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                        ),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium
                    )
                },
                actions = {
                    // Show Add button only for admin, next to search bar
                    if (state.isAdmin) {
                        IconButton(onClick = onNavigateToAddFish) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add New Fish"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->

        val filteredList = state.fishList.filter { fish ->
            val matchesSearch = fish.name.contains(searchQuery, ignoreCase = true) ||
                    fish.scientificName.contains(searchQuery, ignoreCase = true) ||
                    fish.localName.contains(searchQuery, ignoreCase = true)

            val matchesFilter = when (selectedFilter) {
                "Native" -> fish.type.equals("Native", ignoreCase = true)
                "Invasive" -> fish.type.equals("Invasive", ignoreCase = true)
                "Alien" -> fish.type.equals("Alien", ignoreCase = true)
                else -> true
            }
            matchesSearch && matchesFilter
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            FilterButtons(selectedFilter) { newFilter ->
                selectedFilter = newFilter
            }
            Spacer(modifier = Modifier.height(16.dp))

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredList, key = { it.id }) { fish ->
                        FishListItem(fish = fish, onClick = { onNavigateToDetails(fish.id) })
                    }
                }
            }
        }
    }
}

@Composable
fun FilterButtons(selectedFilter: String, onFilterSelected: (String) -> Unit) {
    val filters = listOf("All", "Native", "Invasive", "Alien")

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(filters) { filter ->
            val isSelected = selectedFilter == filter

            // Smooth color transitions
            val backgroundColor by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                label = "bgColor"
            )
            val textColor by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                label = "textColor"
            )
            val borderColor by animateColorAsState(
                targetValue = if (isSelected) Color.Transparent else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                label = "borderColor"
            )

            Surface(
                onClick = { onFilterSelected(filter) },
                shape = RoundedCornerShape(100), // Fully rounded pill shape
                color = backgroundColor,
                border = BorderStroke(1.dp, borderColor),
                modifier = Modifier.height(36.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(horizontal = 20.dp)
                ) {
                    Text(
                        text = filter,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = textColor
                    )
                }
            }
        }
    }
}

@Composable
fun FishListItem(fish: Fish, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp) // Slightly reduced height for list style
                .padding(vertical = 12.dp), // Inner padding for content
            verticalAlignment = Alignment.CenterVertically // Align items vertically center
        ) {
            // Left Side: Image (Half Width - 50%)
            Box(
                modifier = Modifier
                    .weight(0.5f)
                    .fillMaxHeight()
            ) {
                AsyncImage(
                    model = fish.imageUrl,
                    contentDescription = fish.name,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp)) // Rounded corners for image
                        .background(Color.LightGray), // Placeholder background
                    contentScale = ContentScale.Crop
                )

                // Invasive/Native/Alien Status Badge (Top Left Overlay)
                val isInvasive = fish.type.equals("Invasive", ignoreCase = true)
                val isAlien = fish.type.equals("Alien", ignoreCase = true)

                // Color Logic
                val badgeColor = when {
                    isInvasive -> MaterialTheme.colorScheme.error
                    isAlien -> Color(0xFFE65100) // Orange for Alien
                    else -> Color(0xFF2E7D32) // Dark Green for Native
                }

                val containerColor = when {
                    isInvasive -> MaterialTheme.colorScheme.errorContainer
                    isAlien -> Color(0xFFFFCC80) // Light Orange
                    else -> Color(0xFFE8F5E9) // Light Green
                }

                Surface(
                    color = containerColor,
                    shape = RoundedCornerShape(bottomEnd = 12.dp, topStart = 12.dp), // Match card corner
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Text(
                        text = fish.type.uppercase(),
                        color = badgeColor,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Right Side: Info (Half Width - 50%)
            Column(
                modifier = Modifier
                    .weight(0.5f)
                    .fillMaxHeight()
                    .padding(start = 12.dp), // Only left padding needed
                verticalArrangement = Arrangement.Center, // Center content vertically
                horizontalAlignment = Alignment.Start
            ) {
                // 1. Common Name
                Text(
                    text = fish.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                // 2. Local Name (Primary Color)
                if (fish.localName.isNotBlank()) {
                    Text(
                        text = fish.localName,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }

                // 3. Scientific Name (Italic, Gray)
                Text(
                    text = fish.scientificName,
                    style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Chevron Arrow
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Navigate to details",
                tint = Color.Gray,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        // Divider line separating items
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            thickness = 1.dp
        )
    }
}