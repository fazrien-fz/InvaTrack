package com.psm2.inva.presentation.features.edit_blog

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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBlogScreen(
    viewModel: EditBlogViewModel,
    onNavigateUp: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    // Removed "Recipe" from categories
    val categories = listOf("Identification", "Risk Alert", "Native Spotlight")
    var expanded by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri -> viewModel.updateImageUri(uri) }
    )

    LaunchedEffect(state.updateSuccess) {
        if (state.updateSuccess) {
            Toast.makeText(context, "Article updated successfully!", Toast.LENGTH_SHORT).show()
            viewModel.clearMessages()
            onNavigateUp()
        }
    }

    LaunchedEffect(state.updateError) {
        state.updateError?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Article") },
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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Cover Image Picker
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
                    if (imageToShow is android.net.Uri || (imageToShow is String && imageToShow.isNotBlank())) {
                        Image(
                            painter = rememberAsyncImagePainter(imageToShow),
                            contentDescription = "Cover Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AddPhotoAlternate, null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                            Text("Change Cover Image", color = MaterialTheme.colorScheme.onSecondaryContainer)
                        }
                    }
                }

                // 2. Title
                OutlinedTextField(
                    value = state.title,
                    onValueChange = { viewModel.updateTitle(it) },
                    label = { Text("Title *") },
                    modifier = Modifier.fillMaxWidth()
                )

                // 3. Category Dropdown
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = state.category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category) },
                                onClick = {
                                    viewModel.updateCategory(category)
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                // 4. Summary
                OutlinedTextField(
                    value = state.summary,
                    onValueChange = { viewModel.updateSummary(it) },
                    label = { Text("Summary (Max 150 chars)") },
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = { Text("${state.summary.length}/150") },
                    maxLines = 3
                )

                // 5. Content
                OutlinedTextField(
                    value = state.content,
                    onValueChange = { viewModel.updateContent(it) },
                    label = { Text("Article Content *") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    textStyle = TextStyle(textAlign = TextAlign.Start)
                )

                Button(
                    onClick = { viewModel.submitUpdate() },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = !state.isUpdating
                ) {
                    if (state.isUpdating) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("Update Article")
                    }
                }
            }
        }
    }
}