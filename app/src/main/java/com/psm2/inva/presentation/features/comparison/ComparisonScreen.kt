package com.psm2.inva.presentation.features.comparison

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.psm2.inva.model.Fish

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComparisonScreen(
    viewModel: ComparisonViewModel,
    onNavigateUp: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Comparison") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (state.error != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(state.error!!, color = MaterialTheme.colorScheme.error)
            }
        } else if (state.fish1 != null && state.fish2 != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                ComparisonHeader(state.fish1!!, state.fish2!!)

                // Sections
                ComparisonSection("Head", state.fish1!!.headImageUrl, state.fish1!!.headDescription, state.fish2!!.headImageUrl, state.fish2!!.headDescription)
                ComparisonSection("Body", state.fish1!!.bodyImageUrl, state.fish1!!.bodyDescription, state.fish2!!.bodyImageUrl, state.fish2!!.bodyDescription)
                ComparisonSection("Tail", state.fish1!!.tailImageUrl, state.fish1!!.tailDescription, state.fish2!!.tailImageUrl, state.fish2!!.tailDescription)

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun ComparisonHeader(fish1: Fish, fish2: Fish) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Fish 1 Header
        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = fish1.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = fish1.type,
                style = MaterialTheme.typography.labelMedium,
                color = if (fish1.type.equals("Invasive", ignoreCase = true)) Color.Red else Color(0xFF2E7D32)
            )
        }

        Text(
            "VS",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black,
            color = Color.LightGray,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        // Fish 2 Header
        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = fish2.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = fish2.type,
                style = MaterialTheme.typography.labelMedium,
                color = if (fish2.type.equals("Invasive", ignoreCase = true)) Color.Red else Color(0xFF2E7D32)
            )
        }
    }
    HorizontalDivider()
}

@Composable
fun ComparisonSection(
    title: String,
    img1: String,
    desc1: String,
    img2: String,
    desc2: String
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
        // Section Title
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 6.dp),
                color = MaterialTheme.colorScheme.primary
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Left Side (Fish 1)
            Column(modifier = Modifier.weight(1f)) {
                ComparisonImage(img1)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = desc1.ifBlank { "No details available." },
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Start
                )
            }

            // Vertical Divider
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(150.dp) // Approximate height of section
                    .background(Color.LightGray.copy(alpha = 0.5f))
            )

            // Right Side (Fish 2)
            Column(modifier = Modifier.weight(1f)) {
                ComparisonImage(img2)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = desc2.ifBlank { "No details available." },
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Start
                )
            }
        }
    }
}

@Composable
fun ComparisonImage(url: String) {
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth().aspectRatio(1.3f)
    ) {
        if (url.isNotBlank()) {
            AsyncImage(
                model = url,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                Text("No Image", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}