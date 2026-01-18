package com.psm2.inva.presentation.features.report

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.DragState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    viewModel: ReportViewModel,
    onNavigateUp: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var isMapInteracting by remember { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { viewModel.getLocation(context) }
    )

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri -> viewModel.addImageUri(uri) }
    )

    LaunchedEffect(key1 = true) {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    LaunchedEffect(state.submissionError) {
        state.submissionError?.let {
            Toast.makeText(context, "Error: $it", Toast.LENGTH_LONG).show()
            viewModel.clearMessages()
        }
    }
    LaunchedEffect(state.submissionSuccess) {
        if (state.submissionSuccess) {
            Toast.makeText(context, "Catch logged successfully!", Toast.LENGTH_LONG).show()
            onNavigateUp()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Log Catch") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState(), enabled = !isMapInteracting),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // 1. IMAGES
            Row(
                modifier = Modifier.fillMaxWidth().height(120.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                state.imageUris.forEach { uri ->
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(uri),
                            contentDescription = "Selected Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(
                            onClick = { viewModel.removeImageUri(uri) },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(24.dp)
                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.padding(4.dp))
                        }
                    }
                }
                if (state.imageUris.size < 3) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .clickable { imagePickerLauncher.launch("image/*") }
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AddPhotoAlternate, null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                            Text("Add Photo", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
                        }
                    }
                }
            }

            // 2. SPECIES NAME
            var expanded by remember { mutableStateOf(false) }
            val suggestions = state.speciesSuggestions.filter {
                it.name.contains(state.speciesName, ignoreCase = true) ||
                        it.scientificName.contains(state.speciesName, ignoreCase = true)
            }

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = state.speciesName,
                    onValueChange = {
                        viewModel.updateSpeciesName(it)
                        expanded = true
                    },
                    label = { Text("Species Name *") },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                )

                if (suggestions.isNotEmpty() && expanded) {
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.heightIn(max = 300.dp)
                    ) {
                        suggestions.forEach { fish ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(text = fish.name, fontWeight = FontWeight.Bold)
                                        Text(text = fish.type, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                    }
                                },
                                onClick = {
                                    viewModel.updateSpeciesName(fish.name)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            // 3. ACTION TAKEN & WARNING
            Column {
                Text("Catch & Release? *", style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { viewModel.updateActionTaken("Released") }) {
                        RadioButton(selected = state.actionTaken == "Released", onClick = { viewModel.updateActionTaken("Released") })
                        Text("Released", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { viewModel.updateActionTaken("Removed") }) {
                        RadioButton(selected = state.actionTaken == "Removed", onClick = { viewModel.updateActionTaken("Removed") })
                        Text("Removed", color = Color(0xFFC62828), fontWeight = FontWeight.Bold)
                    }
                }

                if (state.category.equals("Invasive", ignoreCase = true)) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, "Warning", tint = Color(0xFFD32F2F))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "WARNING: Invasive species must NOT be released.",
                                color = Color(0xFFD32F2F),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // 4. WEIGHT AND LENGTH
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = state.weight,
                    onValueChange = { viewModel.updateWeight(it) },
                    label = { Text("Weight (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = state.length,
                    onValueChange = { viewModel.updateLength(it) },
                    label = { Text("Length (cm)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
            }

            // 5. QUANTITY
            OutlinedTextField(
                value = state.quantity,
                onValueChange = { viewModel.updateQuantity(it) },
                label = { Text("Quantity") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            // 6. DATE/TIME
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val dateSdf = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
                OutlinedTextField(
                    value = dateSdf.format(Date(state.selectedDateMillis)),
                    onValueChange = {},
                    label = { Text("Catch Date") },
                    modifier = Modifier.weight(1f).clickable { showDatePicker = true },
                    readOnly = true,
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    trailingIcon = { Icon(Icons.Default.EditCalendar, "Select Date") }
                )

                val timeSdf = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
                OutlinedTextField(
                    value = timeSdf.format(Date(state.selectedDateMillis)),
                    onValueChange = {},
                    label = { Text("Catch Time") },
                    modifier = Modifier.weight(1f).clickable { showTimePicker = true },
                    readOnly = true,
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    trailingIcon = { Icon(Icons.Default.AccessTime, "Select Time") }
                )
            }

            // 7. LOCATION MAP
            Text("Location *", style = MaterialTheme.typography.labelLarge)
            val defaultLocation = LatLng(3.1390, 101.6869)
            val currentMapLocation = state.location ?: defaultLocation
            val cameraPositionState = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(currentMapLocation, 15f)
            }

            LaunchedEffect(state.location) {
                state.location?.let {
                    cameraPositionState.animate(com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(it, 15f))
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent(PointerEventPass.Initial)
                                isMapInteracting = event.changes.any { it.pressed }
                            }
                        }
                    }
            ) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    onMapClick = { latLng -> viewModel.updateLocation(latLng, context, null) },
                    onPOIClick = { poi -> viewModel.updateLocation(poi.latLng, context, poi.name) }
                ) {
                    if (state.location != null) {
                        val markerState = rememberMarkerState(position = state.location!!)
                        LaunchedEffect(state.location) { markerState.position = state.location!! }
                        LaunchedEffect(markerState.dragState) {
                            if (markerState.dragState == DragState.END) {
                                viewModel.updateLocation(markerState.position, context, null)
                            }
                        }
                        Marker(
                            state = markerState,
                            draggable = true,
                            title = "Selected Location"
                        )
                    }
                }
                FloatingActionButton(
                    onClick = { viewModel.getLocation(context) },
                    modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp).size(40.dp),
                    shape = RoundedCornerShape(12.dp),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Icon(Icons.Default.MyLocation, "Get My Location", tint = MaterialTheme.colorScheme.primary)
                }
            }

            // 8. LOCATION NAME & STATE DISPLAY
            Column {
                OutlinedTextField(
                    value = state.locationDescription,
                    onValueChange = { viewModel.updateLocationDescription(it) },
                    label = { Text("Location Name") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g. Tasek Lagenda") },
                    supportingText = if (state.locationState.isNotBlank()) {
                        { Text("Region: ${state.locationState}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) }
                    } else null
                )
            }

            // 9. PRIVACY
            Column {
                Text("Location Privacy", style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { viewModel.updateLocationPrivacy("Share Spot") }) {
                        RadioButton(selected = state.locationPrivacy == "Share Spot", onClick = { viewModel.updateLocationPrivacy("Share Spot") })
                        Text("Share Spot")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { viewModel.updateLocationPrivacy("Secret Spot") }) {
                        RadioButton(selected = state.locationPrivacy == "Secret Spot", onClick = { viewModel.updateLocationPrivacy("Secret Spot") })
                        Text("Secret Spot")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.submitReport() },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !state.isSubmitting
            ) {
                if (state.isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Log Catch")
                }
            }
        }
    }

    // Dialogs...
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = state.selectedDateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showDatePicker = false
                    viewModel.updateDate(datePickerState.selectedDateMillis ?: System.currentTimeMillis())
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = datePickerState) }
    }

    if (showTimePicker) {
        val calendar = Calendar.getInstance().apply { timeInMillis = state.selectedDateMillis }
        val timePickerState = rememberTimePickerState(initialHour = calendar.get(Calendar.HOUR_OF_DAY), initialMinute = calendar.get(Calendar.MINUTE))
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showTimePicker = false
                    viewModel.updateTime(timePickerState.hour, timePickerState.minute)
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text("Cancel") } },
            text = { TimePicker(state = timePickerState) }
        )
    }
}