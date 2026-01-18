package com.psm2.inva.presentation.features.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.psm2.inva.model.Blog
import com.psm2.inva.model.Fish
import com.psm2.inva.model.Sighting
import com.psm2.inva.model.UserData

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToBlogDetail: (String) -> Unit,
    onNavigateToCatchDetail: (String) -> Unit,
    onNavigateToSpeciesDetail: (String) -> Unit,
    onNavigateToStats: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = padding.calculateBottomPadding()),
            contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
        ) {
            // 1. Welcome Header (New)
            item {
                WelcomeHeader(user = state.user)
                Spacer(modifier = Modifier.height(24.dp))
            }

            // 2. SPECIES SPOTLIGHT (Moved to Top for Visual Impact)
            if (state.fishList.isNotEmpty()) {
                item {
                    SpeciesSpotlight(
                        fishList = state.fishList,
                        onFishClick = onNavigateToSpeciesDetail
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            // 3. ECO-REPORT ENTRY (Stats Dashboard Link)
            item {
                SimpleReportCard(onClick = onNavigateToStats)
                Spacer(modifier = Modifier.height(24.dp))
            }

            // 4. INVASIVE CATCH IN YOUR AREA (Recent Activity)
            if (state.recentInvasiveSightings.isNotEmpty()) {
                item {
                    InvasiveCatchAreaSection(
                        sightings = state.recentInvasiveSightings,
                        userState = state.userLocationState,
                        onSightingClick = onNavigateToCatchDetail
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }

            // 5. LATEST ARTICLES (Content Feed)
            if (state.blogs.isNotEmpty()) {
                item {
                    Text(
                        "Tips & Awareness",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                items(state.blogs) { blog ->
                    ModernBlogCard(
                        blog = blog,
                        onClick = { onNavigateToBlogDetail(blog.id) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .padding(bottom = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun WelcomeHeader(user: UserData?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Welcome back,",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
            Text(
                text = user?.username ?: "Explorer",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Profile Picture Thumbnail
        if (!user?.profilePictureUrl.isNullOrBlank()) {
            AsyncImage(
                model = user!!.profilePictureUrl,
                contentDescription = "Profile",
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = (user?.username?.take(1) ?: "U").uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
fun SimpleReportCard(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Icon Box
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = "Ecosystem Report",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "View community statistics",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Go",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun InvasiveCatchAreaSection(
    sightings: List<Sighting>,
    userState: String?,
    onSightingClick: (String) -> Unit
) {
    val sectionTitle = if (!userState.isNullOrBlank()) {
        "Nearby Catches"
    } else {
        "Recent Catches"
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Title Row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            Text(
                text = sectionTitle,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Horizontal List
        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(sightings) { sighting ->
                InvasiveCatchCard(sighting = sighting, onClick = { onSightingClick(sighting.id) })
            }
        }
    }
}

@Composable
fun InvasiveCatchCard(sighting: Sighting, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier
            .width(220.dp)
            .height(150.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // 1. Background Image
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
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No Image", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // 2. Gradient Overlay for Text Readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.3f),
                                Color.Black.copy(alpha = 0.8f)
                            ),
                            startY = 100f
                        )
                    )
            )

            // 3. Text Content (Bottom Left)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
            ) {
                // Common Name
                Text(
                    text = sighting.speciesName,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Total Catches
                Text(
                    text = "Logged: ${sighting.quantity}",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

@Composable
fun SpeciesSpotlight(fishList: List<Fish>, onFishClick: (String) -> Unit) {
    // Pick a random fish to spotlight (any species)
    val fish = remember(fishList) { fishList.random() }

    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Text(
            text = "Species Spotlight",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Card(
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp) // Slightly taller
                .clickable { onFishClick(fish.id) }
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Background Image
                AsyncImage(
                    model = fish.imageUrl,
                    contentDescription = fish.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Gradient Overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f))
                            )
                        )
                )

                // Content
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(20.dp)
                ) {
                    // Determine Badge Color and Text based on Type
                    val isInvasive = fish.type.equals("Invasive", ignoreCase = true)
                    val isNative = fish.type.equals("Native", ignoreCase = true)
                    val isAlien = fish.type.equals("Alien", ignoreCase = true)

                    val badgeColor = when {
                        isInvasive -> MaterialTheme.colorScheme.error
                        isNative -> Color(0xFF2E7D32) // Green
                        isAlien -> Color(0xFFE65100) // Orange
                        else -> Color.Gray
                    }

                    val badgeText = when {
                        isInvasive -> "INVASIVE"
                        isNative -> "NATIVE"
                        isAlien -> "ALIEN"
                        else -> fish.type.uppercase()
                    }

                    Surface(
                        color = badgeColor,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = badgeText,
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = fish.name,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text = fish.scientificName,
                        style = MaterialTheme.typography.bodyMedium.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
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
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier
            .height(280.dp)
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top: Image Area
            Box(
                modifier = Modifier
                    .weight(0.6f)
                    .fillMaxWidth()
            ) {
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

                // Badge (Top Left, Floating)
                val badgeColor = when (blog.category.uppercase()) {
                    "IDENTIFICATION" -> Color(0xFF2196F3)
                    "RISK ALERT" -> Color(0xFFD32F2F)
                    "NATIVE SPOTLIGHT" -> Color(0xFF4CAF50)
                    else -> MaterialTheme.colorScheme.primary
                }

                Surface(
                    color = badgeColor.copy(alpha = 0.9f), // Slightly transparent
                    shape = RoundedCornerShape(bottomEnd = 12.dp),
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Text(
                        text = blog.category.uppercase(),
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            // Bottom: Content Area
            Column(
                modifier = Modifier
                    .weight(0.4f)
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = blog.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = blog.summary.ifBlank { "No summary available." },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Read More",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(14.dp)
                            .padding(start = 4.dp)
                    )
                }
            }
        }
    }
}