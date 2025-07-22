package com.kashmir.bislei.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.kashmir.bislei.navigation.BottomNavGraph
import com.kashmir.bislei.navigation.BottomNavigationBar
import com.kashmir.bislei.ui.theme.Dimensions
import com.kashmir.bislei.viewModels.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onLogout: () -> Unit
) {
    val navController = rememberNavController()
    val profileViewModel: ProfileViewModel = viewModel()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Scaffold(
            bottomBar = {
                BottomNavigationBar(navController = navController)
            },
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = WindowInsets(0) // Remove default window insets
        ) { paddingValues ->
            // Content with proper padding to avoid bottom nav overlap
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues) // This ensures content doesn't hide behind bottom nav
            ) {
                BottomNavGraph(
                    navController = navController,
                    viewModel = profileViewModel,
                    onLogout = onLogout
                )
            }
        }
    }
}