package com.kashmir.bislei.navigation.screenroutes

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(val route: String, val title: String, val icon: ImageVector) {
    object Home : BottomNavItem("home", "Home", Icons.Default.HomeMini)
    object Explore : BottomNavItem("explore", "Explore", Icons.Default.MyLocation)
    object Upload : BottomNavItem("upload", "Upload", Icons.Default.PostAdd)
    object Identify : BottomNavItem("identify", "Identify", Icons.Default.ImageSearch)
    object Profile : BottomNavItem("profile", "Profile", Icons.Default.PersonOutline)
}
