package com.kashmir.bislei.components.locationMarkerComponents

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.toArgb
import androidx.compose.material3.MaterialTheme
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberMarkerState
import com.kashmir.bislei.model.FishingSpot


@Composable
fun CustomMarkerWithBadge(
    spot: FishingSpot,
    onClick: () -> Unit,
    onIdentifyFishClick: (LatLng) -> Unit
) {
    val context = LocalContext.current
    val markerState = rememberMarkerState(position = LatLng(spot.latitude, spot.longitude))

        // Get theme colors for consistent styling
        val primaryColor = MaterialTheme.colorScheme.primary.toArgb()
        val surfaceColor = MaterialTheme.colorScheme.surface.toArgb()

       // Create enhanced marker with modern design
       val markerIcon = createEnhancedFishingMarker(
               context = context,
               hotspotCount = spot.hotspotCount,
               primaryColor = primaryColor,
               surfaceColor = surfaceColor
                   )

    Marker(
        state = markerState,
        icon = markerIcon,
        title = spot.name,
        snippet = spot.locationName,
        anchor = androidx.compose.ui.geometry.Offset(0.5f, 1.0f), // Anchor at bottom center
        onClick = {
            onClick()
              true
        }
    )
}