package com.kashmir.bislei.screens.detailScreens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.kashmir.bislei.model.Post
import com.kashmir.bislei.screens.profileScreens.PostCard
import com.kashmir.bislei.viewModels.PostInteractionViewModel
import com.kashmir.bislei.viewModels.ProfileViewModel
import kotlinx.coroutines.delay

@Composable
fun PostFeedScreen(navController: NavHostController) {
    val posts = navController.previousBackStackEntry
        ?.savedStateHandle
        ?.get<List<Post>>("posts") ?: emptyList()

    val startIndex = navController.previousBackStackEntry
        ?.savedStateHandle
        ?.get<Int>("startIndex") ?: 0

    val listState = rememberLazyListState()

    val profileViewModel: ProfileViewModel = viewModel()
    val postInteractionViewModel: PostInteractionViewModel = viewModel()

    LaunchedEffect(posts) {
        delay(300)
        if (startIndex in posts.indices) {
            listState.scrollToItem(startIndex)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
//            contentPadding = PaddingValues(vertical = 100.dp)
        ) {
            itemsIndexed(posts) { _, post ->
                // Start fetching real-time data for each post
                LaunchedEffect(post.id) {
                    postInteractionViewModel.fetchLikeStatus(post.id)
                }

                PostCard(
                    post = post,
                    profileViewModel = profileViewModel,
                    postInteractionViewModel = postInteractionViewModel,
                    onPostDeleted = {
                        // Optional: Handle after delete
                    }
                )
            }
        }
    }
}
