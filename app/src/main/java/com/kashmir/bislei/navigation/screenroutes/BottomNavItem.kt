package com.kashmir.bislei.navigation.screenroutes

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(val route: String, val title: String, val icon: ImageVector) {
    data object Home : BottomNavItem("home", "Home", Icons.Filled.Home)
    data object Explore : BottomNavItem("explore", "Explore", Icons.Filled.Explore)
    data object Upload : BottomNavItem("upload", "Upload", Icons.Filled.AddCircle)
    data object Identify : BottomNavItem("identify", "Identify", Icons.Filled.CameraAlt)
    data object Profile : BottomNavItem("profile", "Profile", Icons.Filled.Person)
}