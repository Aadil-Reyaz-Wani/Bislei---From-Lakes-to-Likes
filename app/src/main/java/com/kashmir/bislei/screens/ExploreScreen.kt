package com.kashmir.bislei.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.google.accompanist.permissions.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.kashmir.bislei.additionals.CustomMarkerWithBadge
import com.kashmir.bislei.model.FishingSpot
import com.kashmir.bislei.viewModels.LocationViewModel
import com.kashmir.bislei.viewmodel.FishingSpotsViewModel
import kotlinx.coroutines.launch
import com.kashmir.bislei.components.MarkerInfoCard

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    fishingSpotsViewModel: FishingSpotsViewModel = viewModel(),
    locationViewModel: LocationViewModel = viewModel(),
    onIdentifyFishClick: (LatLng) -> Unit = {} // Placeholder to avoid compilation errors
) {
    val context = LocalContext.current
    val permissionsState = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(34.1106, 74.8683), 12f)
    }

    // This effect will trigger whenever the camera position changes
    LaunchedEffect(cameraPositionState.position) {
        // This forces a recomposition when the camera moves
        // which will update the position of any displayed card
    }

    val fishingSpots by fishingSpotsViewModel.fishingSpots.collectAsState()
    val currentLocation by locationViewModel.currentLocation.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    var selectedSpot by remember { mutableStateOf<FishingSpot?>(null) }
    var showCard by remember { mutableStateOf(true) }
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // New state to track whether to show the bottom sheet
    var showBottomSheet by remember { mutableStateOf(false) }

    var isMapLoaded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        permissionsState.launchMultiplePermissionRequest()
        if (permissionsState.allPermissionsGranted) {
            locationViewModel.fetchCurrentLocation()
        }
    }

    Scaffold(
        floatingActionButton = {
            if (permissionsState.allPermissionsGranted) {
                FloatingActionButton(
                    onClick = {
                        currentLocation?.let {
                            coroutineScope.launch {
                                cameraPositionState.animate(
                                    CameraUpdateFactory.newLatLngZoom(it, 15f)
                                )
                            }
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = "My Location")
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (permissionsState.allPermissionsGranted) {
                GoogleMap(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 80.dp),
                    cameraPositionState = cameraPositionState,
                    onMapLoaded = { isMapLoaded = true }
                ) {
                    fishingSpots.forEach { spot ->
                        CustomMarkerWithBadge(
                            spot = spot,
                            onClick = {
                                selectedSpot = spot
                                showCard = true // reset card visibility
                                showBottomSheet = false
                            },
                            onIdentifyFishClick = onIdentifyFishClick
                        )
                    }
                }

                // Only show additional marker info if map is loaded and a spot is selected
                if (isMapLoaded && selectedSpot != null) {
                    // Create a key that depends on the camera position to force recomposition
                    val cameraKey = cameraPositionState.position.toString()

                    // Add null safety check for projection
                    cameraPositionState.projection?.let { safeProjection ->
                        val latLng = LatLng(selectedSpot!!.latitude, selectedSpot!!.longitude)
                        val screenPosition = safeProjection.toScreenLocation(latLng)

                        // Only show the card if the marker is currently visible on screen
                        val visibleRegion = safeProjection.visibleRegion.latLngBounds


                        if (visibleRegion.contains(latLng)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                            ) {
                                // Google Maps style info card directly above the marker
                                if (showCard) {
                                    MarkerInfoCard(
                                        name = selectedSpot!!.name,
                                        location = selectedSpot!!.locationName,
                                        hotspotCount = selectedSpot!!.hotspotCount,
                                        modifier = Modifier.offset {
                                            IntOffset(
                                                screenPosition.x - 462,
                                                screenPosition.y - 510
                                            )
                                        },
                                        onClick = {
                                            showBottomSheet = true
                                            coroutineScope.launch {
                                                bottomSheetState.show()
                                            }
                                        },
                                        onDismiss = { showCard = false }
                                    )
                                }

                            }
                        }
                    }
                }
            } else {
                PermissionDeniedUI(permissionsState)
            }
        }

        // Show bottom sheet only if a spot is selected AND showBottomSheet is true
        if (selectedSpot != null && showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    // Only hide the bottom sheet, keep the card visible
                    showBottomSheet = false
                },
                sheetState = bottomSheetState
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        selectedSpot!!.name,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(selectedSpot!!.description, modifier = Modifier.padding(vertical = 4.dp))

                    Text("Location:", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Text(selectedSpot!!.locationName, modifier = Modifier.padding(bottom = 4.dp))

                    if (selectedSpot!!.fishTypes.isNotEmpty()) {
                        Text("Fish Types:", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        selectedSpot!!.fishTypes.forEach {
                            Text("- $it")
                        }
                    }

                    if (selectedSpot!!.bestFishingLocationsNearby.isNotEmpty()) {
                        Text("Nearby Fishing Locations:", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        selectedSpot!!.bestFishingLocationsNearby.forEach {
                            Text("- $it")
                        }
                    }

                    if (selectedSpot!!.imageUrls.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow {
                            items(selectedSpot!!.imageUrls) { url ->
                                Image(
                                    painter = rememberAsyncImagePainter(url),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(200.dp)
                                        .padding(end = 8.dp),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    FloatingActionButton(
                        onClick = {
                            val gmmIntentUri =
                                Uri.parse("google.navigation:q=${selectedSpot!!.latitude},${selectedSpot!!.longitude}")
                            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                            mapIntent.setPackage("com.google.android.apps.maps")
                            context.startActivity(mapIntent)
                        },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White,
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Icon(Icons.Default.Directions, contentDescription = "Directions")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionDeniedUI(permissionsState: MultiplePermissionsState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Location permission is required to use the map", color = Color.Red)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { permissionsState.launchMultiplePermissionRequest() }) {
            Text("Grant Permission")
        }
    }
}