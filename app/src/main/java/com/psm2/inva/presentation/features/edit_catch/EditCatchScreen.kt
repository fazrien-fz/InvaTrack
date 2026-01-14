package com.psm2.inva.presentation.features.edit_catch

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCatchScreen(
    viewModel: EditCatchViewModel,
    onNavigateUp: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri -> viewModel.addNewImage(uri) }
    )

    LaunchedEffect(state.updateSuccess) {
        if (state.updateSuccess) {
            Toast.makeText(context, "Catch updated successfully!", Toast.LENGTH_SHORT).show()
            onNavigateUp()
        }
    }
    LaunchedEffect(state.updateError) {
        state.updateError?.let {
            Toast.makeText(context, "Error: $it", Toast.LENGTH_LONG).show()
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Catch") },
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
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // 1. IMAGES
                Row(
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Existing Images
                    state.existingImageUrls.forEach { url ->
                        val isDeleted = state.deletedImageUrls.contains(url)
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                        ) {
                            AsyncImage(
                                model = url,
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .then(if (isDeleted) Modifier.background(Color.Black.copy(alpha = 0.7f)) else Modifier),
                                contentScale = ContentScale.Crop
                            )
                            IconButton(
                                onClick = { if (isDeleted) viewModel.undoDeleteImage(url) else viewModel.deleteExistingImage(url) },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .size(24.dp)
                                    .background(Color.White.copy(alpha = 0.8f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = if (isDeleted) Icons.Default.Restore else Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = if (isDeleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(4.dp)
                                )
                            }
                        }
                    }
                    // New Images
                    state.newImageUris.forEach { uri ->
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(uri),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            IconButton(
                                onClick = { viewModel.removeNewImage(uri) },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .size(24.dp)
                                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            ) {
                                Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.padding(4.dp))
                            }
                        }
                    }
                    // Add Button
                    if ((state.existingImageUrls.size + state.newImageUris.size) < 3) {
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

                // 2. SPECIES NAME (Searchable Dropdown)
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

                // 3. CATCH & RELEASE (Action Taken)
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

                    // --- INVASIVE SPECIES WARNING ---
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
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Warning",
                                    tint = Color(0xFFD32F2F)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "WARNING: Invasive species must NOT be released back into the water.",
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

                // 6. CATCH DATE AND TIME
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val dateSdf = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
                    OutlinedTextField(
                        value = dateSdf.format(Date(state.dateMillis)),
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
                        value = timeSdf.format(Date(state.dateMillis)),
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

                // 7. LOCATION PRIVACY
                Column {
                    Text("Location Privacy", style = MaterialTheme.typography.labelLarge)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { viewModel.updatePrivacy("Share Spot") }) {
                            RadioButton(selected = state.locationPrivacy == "Share Spot", onClick = { viewModel.updatePrivacy("Share Spot") })
                            Text("Share Spot")
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { viewModel.updatePrivacy("Secret Spot") }) {
                            RadioButton(selected = state.locationPrivacy == "Secret Spot", onClick = { viewModel.updatePrivacy("Secret Spot") })
                            Text("Secret Spot")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Submit Button
                Button(
                    onClick = { viewModel.submitUpdate() },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = !state.isUpdating
                ) {
                    if (state.isUpdating) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("Update Catch")
                    }
                }
            }
        }
    }

    // --- Date Picker Dialog ---
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = state.dateMillis)
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

    // --- Time Picker Dialog ---
    if (showTimePicker) {
        val calendar = Calendar.getInstance().apply { timeInMillis = state.dateMillis }
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