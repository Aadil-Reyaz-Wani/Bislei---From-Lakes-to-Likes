package com.kashmir.bislei.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.kashmir.bislei.navigation.screenroutes.BottomNavItem
import com.kashmir.bislei.ui.theme.Dimensions

@Composable
fun BottomNavigationBar(navController: NavController) {
    val hapticFeedback = LocalHapticFeedback.current
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Explore,
        BottomNavItem.Upload,
        BottomNavItem.Identify,
        BottomNavItem.Profile
    )

    val navBackStackEntry = navController.currentBackStackEntryAsState().value
    val currentRoute = navBackStackEntry?.destination?.route

    // Enhanced navigation bar with modern styling
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = Dimensions.spaceXS,
//                vertical = Dimensions.spaceS
            )
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(Dimensions.cardCornerRadiusLarge),
                clip = false
            ),
        shape = RoundedCornerShape(Dimensions.cardCornerRadiusLarge),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp // Using shadow instead
        )
    ) {
        NavigationBar(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(Dimensions.cardCornerRadiusLarge))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                        )
                    )
                ),
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface,
            tonalElevation = 0.dp
        ) {
            items.forEach { item ->
                NavigationBarItem(
                    icon = {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title,
                            modifier = Modifier.size(
                                if (currentRoute == item.route)
                                    Dimensions.iconSize
                                else
                                    Dimensions.iconSizeSmall
                            )
                        )
                    },
                    label = {
                        Text(
                            text = item.title,
                            style = if (currentRoute == item.route)
                                MaterialTheme.typography.labelMedium
                            else
                                MaterialTheme.typography.labelSmall
                        )
                    },
                    selected = currentRoute == item.route,
                    onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        if (currentRoute != item.route) {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                )
            }
        }
    }
}
