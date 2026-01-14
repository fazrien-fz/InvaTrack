package com.psm2.inva.presentation.features.home

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.psm2.inva.model.Blog
import com.psm2.inva.model.Fish
import com.psm2.inva.model.Sighting
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToBlogDetail: (String) -> Unit,
    onNavigateToComparison: (String, String) -> Unit,
    onNavigateToCatchDetail: (String) -> Unit // Added navigation callback
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    // Local state for selections
    var selectedFish1 by remember { mutableStateOf<Fish?>(null) }
    var selectedFish2 by remember { mutableStateOf<Fish?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = padding.calculateBottomPadding()),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }

            // --- 1. HOW IT WORKS SECTION (2 Cards per Row) ---
            item {
                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    Text(
                        text = "How It Works",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Row 1
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            HowItWorksCard(
                                title = "Identify",
                                description = "Take/upload photo, AI recognition",
                                color = Color(0xFF4CAF50), // Green
                                modifier = Modifier.fillMaxWidth(),
                                bgImageUrl = "https://images.unsplash.com/photo-1559863345-02eae458ddc0?q=80&w=2070&auto=format&fit=crop" // Sample fishing/nature image
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            HowItWorksCard(
                                title = "Check",
                                description = "Invasive / Native / Protected status",
                                color = Color(0xFF2196F3), // Blue
                                modifier = Modifier.fillMaxWidth(),
                                bgImageUrl = "https://images.unsplash.com/photo-1583212292454-1fe6229603b7?q=80&w=1974&auto=format&fit=crop" // Sample check/research image
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Row 2
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            HowItWorksCard(
                                title = "Action",
                                description = "Release / Do Not Release / Report",
                                color = Color(0xFFFF9800), // Orange
                                modifier = Modifier.fillMaxWidth(),
                                bgImageUrl = "https://images.unsplash.com/photo-1544551763-46a8723ba3f9?q=80&w=2074&auto=format&fit=crop" // Sample action/release image
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            HowItWorksCard(
                                title = "Learn",
                                description = "Compare species, see tips, explore habitat",
                                color = Color(0xFF9C27B0), // Purple
                                modifier = Modifier.fillMaxWidth(),
                                bgImageUrl = "https://images.unsplash.com/photo-1535591273668-578e31182c4f?q=80&w=2070&auto=format&fit=crop" // Sample learning/habitat image
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }

            // --- 2. BLOG SECTION ---
            if (state.blogs.isNotEmpty()) {
                item {
                    Text(
                        "Latest Articles",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                item {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(state.blogs) { blog ->
                            ModernBlogCard(
                                blog = blog,
                                onClick = { onNavigateToBlogDetail(blog.id) },
                                modifier = Modifier.width(320.dp)
                            )
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }

            // --- 3. FISH COMPARISON SECTION ---
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp)
                ) {
                    Text(
                        "Quick Comparison",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Simplified Comparison Layout without heavy Card wrapper
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Input 1
                        ModernSpeciesSelector(
                            label = "First Species",
                            selected = selectedFish1,
                            list = state.fishList,
                            onSelect = { selectedFish1 = it }
                        )

                        // VS Indicator
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                thickness = 1.dp
                            )
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        "VS",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Input 2
                        ModernSpeciesSelector(
                            label = "Second Species",
                            selected = selectedFish2,
                            list = state.fishList,
                            onSelect = { selectedFish2 = it }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Action Button
                        Button(
                            onClick = {
                                if (selectedFish1 != null && selectedFish2 != null) {
                                    if (selectedFish1!!.id == selectedFish2!!.id) {
                                        Toast.makeText(context, "Please select different species", Toast.LENGTH_SHORT).show()
                                    } else {
                                        onNavigateToComparison(selectedFish1!!.id, selectedFish2!!.id)
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            enabled = selectedFish1 != null && selectedFish2 != null,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                            )
                        ) {
                            Text(
                                "Compare Now",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun HowItWorksCard(
    title: String,
    description: String,
    color: Color,
    modifier: Modifier = Modifier,
    bgImageUrl: String? = null // New parameter for background image
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = modifier.height(140.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Image
            if (bgImageUrl != null) {
                AsyncImage(
                    model = bgImageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Dark Overlay Gradient for readability
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.3f),
                                    Color.Black.copy(alpha = 0.7f)
                                )
                            )
                        )
                )
            }

            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Bottom // Changed to Bottom to push text down if no icon
            ) {
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (bgImageUrl != null) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (bgImageUrl != null) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun InvasiveCatchCard(sighting: Sighting, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier
            .width(200.dp)
            .height(220.dp)
            .clickable(onClick = onClick) // Added click action
    ) {
        Column {
            // Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
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
                        Text("No Image", style = MaterialTheme.typography.labelSmall)
                    }
                }

                // Badge
                Surface(
                    color = MaterialTheme.colorScheme.error,
                    shape = RoundedCornerShape(bottomEnd = 8.dp),
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Text(
                        text = "INVASIVE",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Info
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = sighting.speciesName,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = sighting.locationDescription.ifBlank { "Unknown" },
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                val sdf = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }
                Text(
                    text = sdf.format(sighting.date.toDate()),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernSpeciesSelector(
    label: String,
    selected: Fish?,
    list: List<Fish>,
    onSelect: (Fish) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier.fillMaxWidth()
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .menuAnchor(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainer, // Clean modern surface color
            tonalElevation = 2.dp,
            onClick = { expanded = true }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = selected?.name ?: "Select Species",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = if (selected != null) FontWeight.Bold else FontWeight.Normal
                        ),
                        color = if (selected != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        ) {
            list.forEach { item ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(item.name, fontWeight = FontWeight.SemiBold)
                            Text(item.type, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        }
                    },
                    onClick = { onSelect(item); expanded = false }
                )
            }
        }
    }
}

@Composable
fun ModernBlogCard(
    blog: Blog,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = modifier
            .height(260.dp)
            .clickable(onClick = onClick)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (blog.coverImageUrl.isNotBlank()) {
                AsyncImage(
                    model = blog.coverImageUrl,
                    contentDescription = blog.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = blog.title.take(1).uppercase(),
                        style = MaterialTheme.typography.displayLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.5f)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.4f),
                                Color.Black.copy(alpha = 0.9f)
                            ),
                            startY = 0f
                        )
                    )
            )

            val badgeColor = when (blog.category.uppercase()) {
                "IDENTIFICATION" -> Color(0xFF2196F3)
                "RISK ALERT" -> Color(0xFFD32F2F)
                "NATIVE SPOTLIGHT" -> Color(0xFF4CAF50)
                else -> MaterialTheme.colorScheme.primary
            }

            Surface(
                color = badgeColor,
                shape = RoundedCornerShape(bottomEnd = 16.dp),
                modifier = Modifier.align(Alignment.TopStart)
            ) {
                Text(
                    text = blog.category.uppercase(),
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = blog.title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = blog.summary.ifBlank { "No summary available." },
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Read More",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF81D4FA)
                            )
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color(0xFF81D4FA),
                            modifier = Modifier
                                .size(12.dp)
                                .padding(start = 2.dp)
                        )
                    }
                }
            }
        }
    }
}