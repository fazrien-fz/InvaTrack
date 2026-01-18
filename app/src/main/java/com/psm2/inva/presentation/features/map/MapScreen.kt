package com.psm2.inva.presentation.features.map

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.TileOverlay
import com.google.maps.android.heatmaps.HeatmapTileProvider
import com.psm2.inva.model.Sighting
import kotlinx.coroutines.launch
import androidx.compose.runtime.snapshotFlow
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun MapScreen(
    state: MapState,
    viewModel: MapViewModel,
    onNavigateToReport: () -> Unit,
    onNavigateToCatchDetail: (String) -> Unit // Added callback for card tap
) {
    var hasLocationPermission by remember { mutableStateOf(false) }
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            val isGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            hasLocationPermission = isGranted
            if (isGranted) {
                viewModel.getUserLocation()
            }
        }
    )

    LaunchedEffect(key1 = true) {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    MapContent(
        state = state,
        viewModel = viewModel,
        hasPermission = hasLocationPermission,
        onNavigateToReport = onNavigateToReport,
        onNavigateToCatchDetail = onNavigateToCatchDetail,
        onMapIdle = { target ->
            viewModel.updateMapCenter(target)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MapContent(
    state: MapState,
    viewModel: MapViewModel,
    hasPermission: Boolean,
    onNavigateToReport: () -> Unit,
    onNavigateToCatchDetail: (String) -> Unit,
    onMapIdle: (LatLng) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    // Bottom Sheet State
    var showBottomSheet by remember { mutableStateOf(false) }
    var selectedSighting by remember { mutableStateOf<Sighting?>(null) }
    val sheetState = rememberModalBottomSheetState()

    // Toggle Menu State
    var showLayerMenu by remember { mutableStateOf(false) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(state.defaultLocation, state.defaultZoom)
    }

    // Listener for Camera Idle
    LaunchedEffect(cameraPositionState) {
        snapshotFlow { cameraPositionState.isMoving }
            .collect { isMoving ->
                if (!isMoving) {
                    onMapIdle(cameraPositionState.position.target)
                }
            }
    }

    // Initial User Location Move
    LaunchedEffect(state.userLocation) {
        state.userLocation?.let { userLocation ->
            if (cameraPositionState.position.target.latitude == state.defaultLocation.latitude &&
                cameraPositionState.position.target.longitude == state.defaultLocation.longitude
            ) {
                coroutineScope.launch {
                    cameraPositionState.animate(
                        CameraUpdateFactory.newLatLngZoom(
                            userLocation,
                            state.userLocationZoom
                        ),
                        1000
                    )
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = hasPermission),
            uiSettings = MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = false) // Disable default button
        ) {
            // --- Render Logic Based on Active Layers ---

            // 1. Heatmap Layer (Invasive) - Bottom Layer
            if (state.showInvasiveHeatmap) {
                // Collect all invasive locations
                val heatmapPoints = remember(state.sightings, state.searchQuery) {
                    state.sightings.filter {
                        it.category.equals("Invasive", ignoreCase = true) &&
                                (state.searchQuery.isEmpty() || it.speciesName.contains(state.searchQuery, ignoreCase = true))
                    }.map { LatLng(it.location.latitude, it.location.longitude) }
                }

                if (heatmapPoints.isNotEmpty()) {
                    // Initialize the HeatmapTileProvider
                    val tileProvider = remember(heatmapPoints) {
                        HeatmapTileProvider.Builder()
                            .data(heatmapPoints)
                            .radius(50) // Adjust radius (20-50 usually works well)
                            .opacity(0.7)
                            .build()
                    }

                    // Render the Tile Overlay
                    TileOverlay(tileProvider = tileProvider)
                }
            }

            // 2. Public Spots & My Spots - Top Layers (Pins)
            state.filteredSightings.forEach { sighting ->
                val isMySpot = sighting.userId == state.currentUserId
                val isPublic = sighting.locationPrivacy == "Share Spot"

                // We only render a PIN if it matches Public or Private criteria explicitly.
                if ((state.showPublicSpots && isPublic) || (state.showMySpots && isMySpot)) {
                    val markerHue = if (sighting.category.equals("Invasive", ignoreCase = true)) {
                        BitmapDescriptorFactory.HUE_RED
                    } else if (sighting.category.equals("Native", ignoreCase = true)) {
                        BitmapDescriptorFactory.HUE_GREEN
                    } else {
                        BitmapDescriptorFactory.HUE_AZURE
                    }

                    Marker(
                        state = MarkerState(
                            position = LatLng(
                                sighting.location.latitude,
                                sighting.location.longitude
                            )
                        ),
                        title = sighting.speciesName,
                        icon = BitmapDescriptorFactory.defaultMarker(markerHue),
                        onClick = {
                            selectedSighting = sighting
                            showBottomSheet = true
                            true
                        }
                    )
                }
            }
        }

        // --- Top UI Layer: Search & Layers ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Search Bar
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    label = { Text("Search") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon") },
                    trailingIcon = if (state.searchQuery.isNotEmpty()) {
                        {
                            IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    } else null,
                    modifier = Modifier
                        .weight(1f)
                        .background(Color.White, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Layers Button
                Surface(
                    onClick = { showLayerMenu = true },
                    shape = RoundedCornerShape(12.dp), // Match search bar shape
                    color = Color.White,
                    shadowElevation = 0.dp, // OutlinedTextField has its own border, button mimics it roughly or uses elevation
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = "Map Layers",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Dropdown for Layers
                    DropdownMenu(
                        expanded = showLayerMenu,
                        onDismissRequest = { showLayerMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Switch(
                                        checked = state.showPublicSpots,
                                        onCheckedChange = { viewModel.togglePublicSpots(it) },
                                        modifier = Modifier.scale(0.8f)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text("Public Spots")
                                }
                            },
                            onClick = { viewModel.togglePublicSpots(!state.showPublicSpots) }
                        )
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Switch(
                                        checked = state.showMySpots,
                                        onCheckedChange = { viewModel.toggleMySpots(it) },
                                        modifier = Modifier.scale(0.8f)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text("My Spots")
                                }
                            },
                            onClick = { viewModel.toggleMySpots(!state.showMySpots) }
                        )
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Switch(
                                        checked = state.showInvasiveHeatmap,
                                        onCheckedChange = { viewModel.toggleInvasiveHeatmap(it) },
                                        modifier = Modifier.scale(0.8f)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text("Invasive Heatmap")
                                }
                            },
                            onClick = { viewModel.toggleInvasiveHeatmap(!state.showInvasiveHeatmap) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // My Location Button (Moved Here)
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                FloatingActionButton(
                    onClick = {
                        viewModel.getUserLocation()
                        state.userLocation?.let { location ->
                            coroutineScope.launch {
                                cameraPositionState.animate(
                                    CameraUpdateFactory.newLatLngZoom(
                                        location,
                                        15f // Or use state.userLocationZoom
                                    ),
                                    1000
                                )
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    containerColor = Color.White,
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = "My Location")
                }
            }
        }

        // --- Bottom Sheet ---
        if (showBottomSheet && selectedSighting != null) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState
            ) {
                SightingDetailsSheet(
                    sighting = selectedSighting!!,
                    onViewDetails = {
                        showBottomSheet = false
                        onNavigateToCatchDetail(it)
                    }
                )
            }
        }
    }
}

// Helper to scale switch
fun Modifier.scale(scale: Float): Modifier = this.then(
    Modifier.requiredSize(50.dp * scale, 30.dp * scale)
)


@Composable
fun SightingDetailsSheet(
    sighting: Sighting,
    onViewDetails: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .padding(bottom = 24.dp) // Extra padding for safety
            .height(IntrinsicSize.Min)
            .clickable { onViewDetails(sighting.id) },
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Left Side: Large Thumbnail Image
        if (sighting.imageUrl.isNotBlank()) {
            AsyncImage(
                model = sighting.imageUrl,
                contentDescription = sighting.speciesName,
                modifier = Modifier
                    .size(110.dp) // Square crop
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.ImageNotSupported, contentDescription = null, tint = Color.White)
            }
        }

        // 2. Right Side: Key Data Points
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Title: Species Name (Bold)
            Text(
                text = sighting.speciesName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Timestamp: Date & Time (e.g., "12 Oct • 4:30 PM")
            val sdf = remember { SimpleDateFormat("dd MMM • h:mm a", Locale.getDefault()) }
            Text(
                text = sdf.format(sighting.date.toDate()),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            // Status Badge: REMOVED (Red) or RELEASED (Green)
            val isRemoved = sighting.actionTaken.equals("Removed", ignoreCase = true)
            val isReleased = sighting.actionTaken.equals("Released", ignoreCase = true)
            val badgeText = if (isRemoved) "REMOVED" else if (isReleased) "RELEASED" else sighting.actionTaken.uppercase()
            val badgeColor = if (isRemoved) Color(0xFFD32F2F) else if (isReleased) Color(0xFF2E7D32) else Color.Gray
            val containerColor = if (isRemoved) Color(0xFFFFEBEE) else if (isReleased) Color(0xFFE8F5E9) else Color.LightGray.copy(alpha = 0.3f)

            Surface(
                color = containerColor,
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = badgeText,
                    color = badgeColor,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            // Quick Stats: Length | Weight
            // Example: "42 cm | 1.5 kg" (Handle 0.0 case gracefully)
            val lenStr = if (sighting.length > 0) "${sighting.length} cm" else "-"
            val wgtStr = if (sighting.weight > 0) "${sighting.weight} kg" else "-"
            if (lenStr != "-" || wgtStr != "-") {
                Text(
                    text = "$lenStr | $wgtStr",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Location Name
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = sighting.locationDescription.ifBlank { "Unknown Location" },
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // 3. View Arrow (Rightmost)
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "View Details",
            tint = Color.Gray,
            modifier = Modifier.size(24.dp)
        )
    }
}