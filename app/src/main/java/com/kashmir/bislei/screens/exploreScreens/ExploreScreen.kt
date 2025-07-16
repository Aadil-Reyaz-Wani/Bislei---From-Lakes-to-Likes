package com.kashmir.bislei.screens.exploreScreens

import android.Manifest
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
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
import com.kashmir.bislei.components.locationMarkerComponents.CustomMarkerWithBadge
import com.kashmir.bislei.components.locationMarkerComponents.MarkerInfoCard
import com.kashmir.bislei.model.FishingSpot
import com.kashmir.bislei.components.BisleiTopAppBar
import com.kashmir.bislei.ui.theme.Dimensions
import com.kashmir.bislei.viewModels.LocationViewModel
import com.kashmir.bislei.viewModels.FishingSpotsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    onNavigateBack: () -> Unit = {},
    fishingSpotsViewModel: FishingSpotsViewModel = viewModel(),
    locationViewModel: LocationViewModel = viewModel(),
    onIdentifyFishClick: (LatLng) -> Unit = {}
) {
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current

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
        topBar = {
            BisleiTopAppBar(
                title = "Explore",
                onNavigateBack = onNavigateBack
            )
        },
        floatingActionButton = {
            if (permissionsState.allPermissionsGranted) {
                FloatingActionButton(
                    onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        currentLocation?.let {
                            coroutineScope.launch {
                                cameraPositionState.animate(
                                    CameraUpdateFactory.newLatLngZoom(it, 15f)
                                )
                            }
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = MaterialTheme.shapes.large
                ) {
                    Icon(
                        Icons.Default.MyLocation,
                        contentDescription = "My Location",
                        modifier = Modifier.size(Dimensions.iconSize)
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
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
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
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
                                    .padding(Dimensions.screenPadding)
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
                                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                            showBottomSheet = true
                                            coroutineScope.launch {
                                                bottomSheetState.show()
                                            }
                                        },
                                        onDismiss = {
                                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                            showCard = false
                                        }
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
                sheetState = bottomSheetState,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                shape = RoundedCornerShape(
                    topStart = Dimensions.cardCornerRadiusLarge,
                    topEnd = Dimensions.cardCornerRadiusLarge
                )
            ) {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(Dimensions.contentPadding)
                ) {
                    // Header with location name
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        ),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Column(
                            modifier = Modifier.padding(Dimensions.spaceM)
                        ) {
                            Text(
                                selectedSpot!!.name,
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(Dimensions.spaceXS))

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.LocationOn,
                                    contentDescription = null,
                                    modifier = Modifier.size(Dimensions.iconSizeSmall),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(Dimensions.spaceXS))
                                Text(
                                    selectedSpot!!.locationName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(Dimensions.spaceM))

                    // Description
                    if (selectedSpot!!.description.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            ),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text(
                                selectedSpot!!.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(Dimensions.spaceM)
                            )
                        }

                        Spacer(modifier = Modifier.height(Dimensions.spaceM))
                    }

                    // Fish Types
                    if (selectedSpot!!.fishTypes.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = Dimensions.cardElevation
                            ),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Column(
                                modifier = Modifier.padding(Dimensions.spaceM)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Pets,
                                        contentDescription = null,
                                        modifier = Modifier.size(Dimensions.iconSizeSmall),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(Dimensions.spaceS))
                                    Text(
                                        "Fish Types:",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Spacer(modifier = Modifier.height(Dimensions.spaceS))

                                selectedSpot!!.fishTypes.forEach { fishType ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(vertical = Dimensions.spaceXS)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Circle,
                                            contentDescription = null,
                                            modifier = Modifier.size(6.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(Dimensions.spaceS))
                                        Text(
                                            fishType,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(Dimensions.spaceM))
                    }

                    // Nearby Locations
                    if (selectedSpot!!.bestFishingLocationsNearby.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = Dimensions.cardElevation
                            ),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Column(
                                modifier = Modifier.padding(Dimensions.spaceM)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.NearMe,
                                        contentDescription = null,
                                        modifier = Modifier.size(Dimensions.iconSizeSmall),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(Dimensions.spaceS))
                                    Text(
                                        "Nearby Fishing Locations:",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Spacer(modifier = Modifier.height(Dimensions.spaceS))

                                selectedSpot!!.bestFishingLocationsNearby.forEach { location ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(vertical = Dimensions.spaceXS)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Circle,
                                            contentDescription = null,
                                            modifier = Modifier.size(6.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(Dimensions.spaceS))
                                        Text(
                                            location,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(Dimensions.spaceM))
                    }

                    // Images
                    if (selectedSpot!!.imageUrls.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = Dimensions.cardElevation
                            ),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Column(
                                modifier = Modifier.padding(Dimensions.spaceM)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.PhotoLibrary,
                                        contentDescription = null,
                                        modifier = Modifier.size(Dimensions.iconSizeSmall),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(Dimensions.spaceS))
                                    Text(
                                        "Photos:",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Spacer(modifier = Modifier.height(Dimensions.spaceS))

                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(Dimensions.spaceS)
                                ) {
                                    items(selectedSpot!!.imageUrls) { url ->
                                        Card(
                                            shape = MaterialTheme.shapes.medium,
                                            elevation = CardDefaults.cardElevation(
                                                defaultElevation = 2.dp
                                            )
                                        ) {
                                            Image(
                                                painter = rememberAsyncImagePainter(url),
                                                contentDescription = "Fishing spot photo",
                                                modifier = Modifier
                                                    .size(120.dp)
                                                    .clip(MaterialTheme.shapes.medium),
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(Dimensions.spaceM))
                    }

                    // Directions Button
                    FilledTonalButton(
                        onClick = {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            val gmmIntentUri =
                                Uri.parse("google.navigation:q=${selectedSpot!!.latitude},${selectedSpot!!.longitude}")
                            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                            mapIntent.setPackage("com.google.android.apps.maps")
                            context.startActivity(mapIntent)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(Dimensions.buttonHeight),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Icon(
                            Icons.Default.Directions,
                            contentDescription = "Get Directions",
                            modifier = Modifier.size(Dimensions.iconSize)
                        )
                        Spacer(modifier = Modifier.width(Dimensions.spaceS))
                        Text(
                            "Get Directions",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }

                    Spacer(modifier = Modifier.height(Dimensions.spaceXL))
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionDeniedUI(permissionsState: MultiplePermissionsState) {
    val hapticFeedback = LocalHapticFeedback.current

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.contentPadding),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = Dimensions.cardElevation
            ),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimensions.spaceXXL),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.LocationOff,
                    contentDescription = null,
                    modifier = Modifier.size(Dimensions.iconSizeXL),
                    tint = MaterialTheme.colorScheme.error
                )

                Spacer(modifier = Modifier.height(Dimensions.spaceM))

                Text(
                    "Location Permission Required",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(Dimensions.spaceS))

                Text(
                    "We need location access to show fishing spots near you and provide navigation.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(Dimensions.spaceL))

                FilledTonalButton(
                    onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        permissionsState.launchMultiplePermissionRequest()
                    },
                    modifier = Modifier.height(Dimensions.buttonHeight),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(Dimensions.iconSizeSmall)
                    )
                    Spacer(modifier = Modifier.width(Dimensions.spaceS))
                    Text(
                        "Grant Permission",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}