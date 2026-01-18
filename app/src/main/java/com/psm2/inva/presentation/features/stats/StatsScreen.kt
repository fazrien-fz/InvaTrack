package com.psm2.inva.presentation.features.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    viewModel: StatsViewModel,
    onNavigateUp: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ecosystem Report") },
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
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // 1. Species Trend Graph (Line Chart)
                if (state.invasiveSpeciesList.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                "Monthly Catch Trend",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            // Filters Row
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                // Species Dropdown
                                StatsDropdown(
                                    label = "Species",
                                    options = state.invasiveSpeciesList,
                                    selectedOption = state.selectedSpecies,
                                    onOptionSelected = { viewModel.onSpeciesSelected(it) }
                                )

                                // State Dropdown
                                StatsDropdown(
                                    label = "State",
                                    options = state.stateList,
                                    selectedOption = state.selectedState,
                                    onOptionSelected = { viewModel.onStateSelected(it) }
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Line Chart
                            if (state.trendData.isNotEmpty()) {
                                LineChart(
                                    data = state.trendData,
                                    lineColor = Color(0xFFD32F2F) // Red for invasive
                                )
                            } else {
                                Box(
                                    modifier = Modifier.fillMaxWidth().height(150.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No trend data available", color = Color.Gray)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 2. Top Threats List (Details)
                Text(
                    "Top Invasive Threats Details",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                if (state.topInvasiveSpecies.isEmpty()) {
                    Text(
                        "No invasive species reported yet.",
                        modifier = Modifier.padding(16.dp),
                        color = Color.Gray
                    )
                } else {
                    state.topInvasiveSpecies.forEachIndexed { index, (name, count) ->
                        ThreatItem(rank = index + 1, name = name, count = count, total = state.totalSightings)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun StatsDropdown(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Select $label")
                }
            },
            modifier = Modifier.fillMaxWidth().clickable { expanded = true }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.9f).heightIn(max = 250.dp)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun LineChart(
    data: List<Pair<String, Int>>,
    lineColor: Color
) {
    if (data.isEmpty()) return

    val maxVal = data.maxOf { it.second }.coerceAtLeast(1)

    // Smooth path logic
    val points = remember(data) { data }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(start = 16.dp, end = 16.dp, bottom = 24.dp) // Padding for labels
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val xStep = if (data.size > 1) width / (data.size - 1) else width / 2

            val path = Path()

            // Draw grid lines and labels
            val yStep = height / 4
            for (i in 0..4) {
                val y = height - (i * yStep)
                // Grid line
                drawLine(
                    color = Color.LightGray.copy(alpha = 0.5f),
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            points.forEachIndexed { index, (label, value) ->
                val x = if (data.size > 1) index * xStep else width / 2
                val y = height - ((value.toFloat() / maxVal) * height)

                // Line Path
                if (index == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }

                // Draw Point
                drawCircle(
                    color = lineColor,
                    radius = 4.dp.toPx(),
                    center = Offset(x, y)
                )

                // Draw X-Axis Label
                val textPaint = android.graphics.Paint().apply {
                    color = android.graphics.Color.GRAY
                    textSize = 10.sp.toPx()
                    textAlign = android.graphics.Paint.Align.CENTER
                }

                drawContext.canvas.nativeCanvas.drawText(
                    label,
                    x,
                    height + 20.dp.toPx(), // Below the graph
                    textPaint
                )

                // Draw Value Label (optional, above point)
                if (value > 0) {
                    drawContext.canvas.nativeCanvas.drawText(
                        value.toString(),
                        x,
                        y - 10.dp.toPx(),
                        textPaint
                    )
                }
            }

            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
fun ThreatItem(rank: Int, name: String, count: Int, total: Int) {
    val percent = if (total > 0) (count.toFloat() / total) else 0f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.errorContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(rank.toString(), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(name, fontWeight = FontWeight.Bold)
                Text("$count reports", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { percent },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.error,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        }
    }
}