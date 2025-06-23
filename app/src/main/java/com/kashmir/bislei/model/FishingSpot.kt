package com.kashmir.bislei.model

data class FishingSpot(
    val name: String = "",
    val locationName: String = "",
    val description: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val bestFishingLocationsNearby: List<String> = emptyList(),
    val fishTypes: List<String> = emptyList(),
    val imageUrls: List<String> = emptyList(),
    val hotspotCount: Int = 0
)


