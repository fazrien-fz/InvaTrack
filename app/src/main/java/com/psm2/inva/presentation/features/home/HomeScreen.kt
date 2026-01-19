package com.psm2.inva.presentation.features.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.psm2.inva.model.Blog
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
            // 1. ECO-REPORT ENTRY (Stats Dashboard Link)
            item {
                Spacer(modifier = Modifier.height(16.dp)) // Extra spacing since header is gone
                SimpleReportCard(onClick = onNavigateToStats)
                Spacer(modifier = Modifier.height(24.dp))
            }

            // 2. LATEST ARTICLES (Content Feed)
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