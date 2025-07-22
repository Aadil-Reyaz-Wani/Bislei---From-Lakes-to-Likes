package com.kashmir.bislei.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.google.firebase.auth.FirebaseAuth
import com.kashmir.bislei.navigation.screenroutes.AuthScreens
import com.kashmir.bislei.screens.*
import com.kashmir.bislei.screens.authScreens.LoginScreen
import com.kashmir.bislei.screens.authScreens.RegisterScreen
import com.kashmir.bislei.screens.authScreens.ResetPasswordScreen

@Composable
fun NavigationGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = AuthScreens.Splash.route) {

        composable(AuthScreens.Splash.route) {
            SplashScreen(
                onSplashComplete = {
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
            )
        }

        composable(AuthScreens.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(AuthScreens.Home.route) {
                        popUpTo(AuthScreens.Login.route) { inclusive = true }
                    }
                },
                onRegisterClick = {
                    navController.navigate(AuthScreens.Register.route)
                },
                onForgotPassword = {
                    navController.navigate(AuthScreens.ResetPassword.route)
                }
            )
        }

        composable(AuthScreens.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(AuthScreens.Login.route) {
                        popUpTo(AuthScreens.Register.route) { inclusive = true }
                    }
                },
                onLoginClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(AuthScreens.Home.route) {
            MainScreen(
                onLogout = {
                    navController.navigate(AuthScreens.Login.route) {
                        popUpTo(AuthScreens.Home.route) { inclusive = true }
                    }
                }
            )
        }

        composable(AuthScreens.ResetPassword.route) {
            ResetPasswordScreen(
                onBackToLogin = {
                    navController.popBackStack()
                }
            )
        }
    }
}