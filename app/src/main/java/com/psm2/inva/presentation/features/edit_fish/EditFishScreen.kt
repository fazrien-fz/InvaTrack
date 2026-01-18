package com.psm2.inva.presentation.features.edit_fish

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditFishScreen(
    viewModel: EditFishViewModel,
    onNavigateUp: () -> Unit,
    onDeleteSuccess: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val types = listOf("Invasive", "Native", "Alien")

    // Updated conservation status list
    val conservationStatuses = listOf(
        "(NE) Not Evaluated",
        "(DD) Data Deficient",
        "(LC) Least Concern",
        "(NT) Near Threatened",
        "(VU) Vulnerable",
        "(EN) Endangered",
        "(CR) Critically Endangered",
        "(EW) Extinct in the Wild",
        "(EX) Extinct"
    )

    var expandedStatus by remember { mutableStateOf(false) }
    var expandedType by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri -> viewModel.updateImageUri(uri) }
    )

    LaunchedEffect(state.updateError) {
        state.updateError?.let {
            Toast.makeText(context, "Error: $it", Toast.LENGTH_LONG).show()
            viewModel.clearMessages()
        }
    }
    LaunchedEffect(state.updateSuccess) {
        if (state.updateSuccess) {
            Toast.makeText(context, "Species updated successfully!", Toast.LENGTH_LONG).show()
            onNavigateUp()
        }
    }
    // Delete effects are kept harmlessly in case ViewModel triggers them, though UI trigger is removed
    LaunchedEffect(state.deleteError) {
        state.deleteError?.let {
            Toast.makeText(context, "Error: $it", Toast.LENGTH_LONG).show()
            viewModel.clearMessages()
        }
    }
    LaunchedEffect(state.deleteSuccess) {
        if (state.deleteSuccess) {
            Toast.makeText(context, "Species deleted successfully!", Toast.LENGTH_LONG).show()
            onDeleteSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Fish Species") },
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
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Main Image Picker
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .clickable { imagePickerLauncher.launch("image/*") }
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    val imageToShow = state.imageUri ?: state.existingImageUrl
                    if (imageToShow is Uri || (imageToShow is String && imageToShow.isNotBlank())) {
                        Image(
                            painter = rememberAsyncImagePainter(imageToShow),
                            contentDescription = "Selected fish image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.AddPhotoAlternate,
                                contentDescription = "Add Photo",
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text("Tap to add photo *", color = MaterialTheme.colorScheme.onSecondaryContainer)
                        }
                    }
                }

                OutlinedTextField(
                    value = state.name,
                    onValueChange = { viewModel.updateName(it) },
                    label = { Text("Species Name *") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = state.localName,
                    onValueChange = { viewModel.updateLocalName(it) },
                    label = { Text("Local Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = state.scientificName,
                    onValueChange = { viewModel.updateScientificName(it) },
                    label = { Text("Scientific Name *") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Type Dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedType,
                    onExpandedChange = { expandedType = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = state.type,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Type *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedType) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedType,
                        onDismissRequest = { expandedType = false }
                    ) {
                        types.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    viewModel.updateType(type)
                                    expandedType = false
                                }
                            )
                        }
                    }
                }

                // Conservation Status Dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedStatus,
                    onExpandedChange = { expandedStatus = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = state.conservationStatus,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Conservation Status") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStatus) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedStatus,
                        onDismissRequest = { expandedStatus = false }
                    ) {
                        conservationStatuses.forEach { status ->
                            DropdownMenuItem(
                                text = { Text(status) },
                                onClick = {
                                    viewModel.updateConservationStatus(status)
                                    expandedStatus = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = state.origin,
                    onValueChange = { viewModel.updateOrigin(it) },
                    label = { Text("Origin") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = state.habitat,
                    onValueChange = { viewModel.updateHabitat(it) },
                    label = { Text("Habitat") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = state.impact,
                    onValueChange = { viewModel.updateImpact(it) },
                    label = { Text("Impact") },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    maxLines = 5
                )

                OutlinedTextField(
                    value = state.description,
                    onValueChange = { viewModel.updateDescription(it) },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth().height(150.dp)
                )

                OutlinedTextField(
                    value = state.diet,
                    onValueChange = { viewModel.updateDiet(it) },
                    label = { Text("Diet") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Removed Morphological Details Section (Head, Body, Tail)

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { viewModel.submitUpdate() },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = !state.isUpdating && !state.isDeleting
                ) {
                    if (state.isUpdating) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("Update Species")
                    }
                }

                // Removed Delete Species Button and its Dialog logic

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}