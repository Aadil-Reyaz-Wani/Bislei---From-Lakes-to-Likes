package com.kashmir.bislei.components.locationMarkerComponents

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberMarkerState
import com.kashmir.bislei.model.FishingSpot
import com.kashmir.bislei.R

@Composable
fun CustomMarkerWithBadge(
    spot: FishingSpot,
    onClick: () -> Unit,
    onIdentifyFishClick: (LatLng) -> Unit
) {
    val context = LocalContext.current
    val markerState = rememberMarkerState(position = LatLng(spot.latitude, spot.longitude))

    // Create the marker icon with the hotspot badge
    val markerIcon = createHotspotMarker(context, R.drawable.location_marker, spot.hotspotCount)

    // Use simple Marker with custom icon and no info window
    Marker(
        state = markerState,
        icon = markerIcon,
        title = spot.name,
        snippet = spot.locationName,
        onClick = {
            onClick()
            true
        }
    )
}