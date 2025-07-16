package com.kashmir.bislei.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.kashmir.bislei.navigation.screenroutes.BottomNavItem
import com.kashmir.bislei.screens.detailsScreens.PostFeedScreen
import com.kashmir.bislei.screens.exploreScreens.ExploreScreen
import com.kashmir.bislei.screens.feedScreens.HomeScreen
import com.kashmir.bislei.screens.profileScreens.EditProfileFieldsScreen
import com.kashmir.bislei.screens.profileScreens.ProfileScreen
import com.kashmir.bislei.screens.uploadScreens.UploadPostScreen
import com.kashmir.bislei.viewModels.FeedViewModel
import com.kashmir.bislei.viewModels.PostInteractionViewModel
import com.kashmir.bislei.viewModels.ProfileViewModel

@Composable
fun BottomNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel,
    onLogout: () -> Unit
) {
    NavHost(navController, startDestination = BottomNavItem.Home.route) {
        composable(BottomNavItem.Home.route) {
            HomeScreen(
                feedViewModel = FeedViewModel(),
                profileViewModel = ProfileViewModel(),
                postInteractionViewModel = PostInteractionViewModel(),
                modifier = Modifier
            )
        }
        composable(BottomNavItem.Explore.route) { ExploreScreen() }
        composable(BottomNavItem.Upload.route) {
            UploadPostScreen(
                onUploadSuccess = {
                    navController.navigate(BottomNavItem.Home.route) {
                        popUpTo(BottomNavItem.Upload.route) { inclusive = true }
                    }
                }
            )
        }

        composable(BottomNavItem.Identify.route) { /* Identify Fish Screen */ }

        // Profile screen with navigation to Edit
        composable(BottomNavItem.Profile.route) {
            ProfileScreen(
                onEditProfile = { navController.navigate("edit_profile") },
                onPostClick = { post ->
                    val posts = (viewModel.userPosts.value) // get all posts from the ViewModel

                    navController.currentBackStackEntry?.savedStateHandle?.set("posts", posts)
                    navController.currentBackStackEntry?.savedStateHandle?.set(
                        "startIndex",
                        posts.indexOf(post)
                    )
                    navController.navigate("post_feed")
                },
                onLogout = onLogout
            )
        }

        composable("post_feed") {
            PostFeedScreen(navController = navController)
        }

        // Edit profile screen
        composable("edit_profile") {
            EditProfileFieldsScreen(
                onProfileSaved = {
                    navController.popBackStack()
                }
            )
        }
    }
}
