package com.kashmir.bislei.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.kashmir.bislei.navigation.screenroutes.BottomNavItem
import com.kashmir.bislei.screens.detailsScreens.PostFeedScreen
import com.kashmir.bislei.screens.exploreScreens.ExploreScreen
import com.kashmir.bislei.screens.feedScreens.HomeScreen
import com.kashmir.bislei.screens.identificationScreens.FishIdentifierScreen
import com.kashmir.bislei.screens.profileScreens.EditProfileFieldsScreen
import com.kashmir.bislei.screens.profileScreens.ProfileScreen
import com.kashmir.bislei.screens.uploadScreens.UploadPostScreen
import com.kashmir.bislei.viewModels.FeedViewModel
import com.kashmir.bislei.viewModels.FishIdentifierViewModel
import com.kashmir.bislei.viewModels.PostInteractionViewModel
import com.kashmir.bislei.viewModels.ProfileViewModel

@Composable
fun BottomNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel,
    onLogout: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = BottomNavItem.Home.route,
        modifier = modifier
    ) {
        composable(BottomNavItem.Home.route) {
            HomeScreen(
                feedViewModel = FeedViewModel(),
                profileViewModel = ProfileViewModel(),
                postInteractionViewModel = PostInteractionViewModel()
            )
        }

        composable(BottomNavItem.Explore.route) {
            ExploreScreen()
        }

        composable(BottomNavItem.Upload.route) {
            UploadPostScreen(
                onNavigateBack = { navController.popBackStack() },
                onUploadSuccess = {
                    navController.navigate(BottomNavItem.Home.route) {
                        popUpTo(BottomNavItem.Upload.route) { inclusive = true }
                    }
                }
            )
        }

        composable(BottomNavItem.Identify.route) {
            FishIdentifierScreen(viewModel = FishIdentifierViewModel())
        }

        composable(BottomNavItem.Profile.route) {
            ProfileScreen(
                onNavigateBack = { /* Not needed for bottom nav */ },
                onEditProfile = { navController.navigate("edit_profile") },
                onPostClick = { post ->
                    val posts = (viewModel.userPosts.value)
                    navController.currentBackStackEntry?.savedStateHandle?.set("posts", posts)
                    navController.currentBackStackEntry?.savedStateHandle?.set(
                        "startIndex",
                        posts.indexOf(post)
                    )
                    navController.navigate("post_feed")
                },
                onLogout = onLogout,
                viewModel = viewModel
            )
        }

        composable("post_feed") {
            PostFeedScreen(navController = navController)
        }

        composable("edit_profile") {
            EditProfileFieldsScreen(
                onNavigateBack = { navController.popBackStack() },
                onProfileSaved = {
                    navController.popBackStack()
                }
            )
        }
    }
}
