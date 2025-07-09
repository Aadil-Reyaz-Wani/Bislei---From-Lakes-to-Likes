package com.kashmir.bislei.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.kashmir.bislei.navigation.BottomNavGraph
import com.kashmir.bislei.navigation.BottomNavigationBar
import com.kashmir.bislei.viewModels.ProfileViewModel

@Composable
fun MainScreen(onLogout: () -> Unit) {
    val navController = rememberNavController()
    Scaffold(bottomBar = {
        BottomNavigationBar(navController = navController)
    }) { innerPadding ->
        BottomNavGraph(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
            viewModel = ProfileViewModel(),
            onLogout = onLogout

        )
    }
}
