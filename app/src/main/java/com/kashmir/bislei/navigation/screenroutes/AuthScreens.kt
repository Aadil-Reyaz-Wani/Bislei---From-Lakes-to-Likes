package com.kashmir.bislei.navigation.screenroutes

sealed class AuthScreens(val route: String) {
    data object Splash : AuthScreens("splash_screen")
    data object Login : AuthScreens("login_screen")
    data object Register : AuthScreens("register_screen")
    data object Home : AuthScreens("home_screen")
    data object ResetPassword : AuthScreens("reset_password_screen")
}
