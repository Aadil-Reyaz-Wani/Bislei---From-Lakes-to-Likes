package com.kashmir.bislei.navigation.screenroutes

sealed class AuthScreens(val route: String) {
    object Splash : AuthScreens("splash_screen")
    object Login : AuthScreens("login_screen")
    object Register : AuthScreens("register_screen")
    object Home : AuthScreens("home_screen")
    object ResetPassword : AuthScreens("reset_password_screen")
}
