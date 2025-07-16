package com.kashmir.bislei.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.kashmir.bislei.navigation.screenroutes.AuthScreens
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    navController: NavHostController
) {
    LaunchedEffect(Unit) {
        delay(2500)

        val isUserLoggedIn = FirebaseAuth.getInstance().currentUser != null

        if (isUserLoggedIn) {
            navController.navigate(AuthScreens.Home.route) {
                popUpTo(AuthScreens.Splash.route) { inclusive = true }
            }
        } else {
            navController.navigate(AuthScreens.Login.route) {
                popUpTo(AuthScreens.Splash.route) { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(20.dp))
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Bislei", style = MaterialTheme.typography.headlineSmall)
                Text(text = "From Lakes to Likes", style = MaterialTheme.typography.headlineSmall)
            }
        }
    }
}
