package com.kashmir.bislei.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.kashmir.bislei.model.FishingSpot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FishingSpotsViewModel : ViewModel() {

    private val _fishingSpots = MutableStateFlow<List<FishingSpot>>(emptyList())
    val fishingSpots: StateFlow<List<FishingSpot>> = _fishingSpots

    init {
        fetchFishingSpots()
    }

    private fun fetchFishingSpots() {
        viewModelScope.launch {
            Firebase.firestore.collection("fishing_spots")
                .get()
                .addOnSuccessListener { result ->
                    val spots = result.documents.mapNotNull { it.toObject(FishingSpot::class.java) }
                        .map { it.copy(hotspotCount = (1..20).random()) } // add dummy hotspot count
                    _fishingSpots.value = spots
                }

                .addOnFailureListener {
                    // Optional: handle error (log or show toast if needed)
                    _fishingSpots.value = emptyList()
                }
        }
    }
}
