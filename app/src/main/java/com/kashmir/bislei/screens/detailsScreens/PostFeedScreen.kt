package com.kashmir.bislei.screens.detailsScreens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.kashmir.bislei.model.Post
import com.kashmir.bislei.screens.profileScreens.PostCard
import com.kashmir.bislei.components.BisleiTopAppBar
import com.kashmir.bislei.ui.theme.Dimensions
import com.kashmir.bislei.viewModels.PostInteractionViewModel
import com.kashmir.bislei.viewModels.ProfileViewModel
import kotlinx.coroutines.delay


@Composable
fun PostFeedScreen(
    navController: NavHostController,
    onNavigateBack: () -> Unit = { navController.popBackStack() }
) {
    val hapticFeedback = LocalHapticFeedback.current

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

    Scaffold(
        topBar = {
            BisleiTopAppBar(
                title = "Posts",
                onNavigateBack = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    onNavigateBack()
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        if (posts.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimensions.contentPadding),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    shape = MaterialTheme.shapes.large
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimensions.spaceXXL),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No posts available",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(Dimensions.spaceS))
                        Text(
                            text = "Posts will appear here when available",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                state = listState,
                contentPadding = PaddingValues(
                    horizontal = Dimensions.screenPadding,
                    vertical = Dimensions.spaceL
                ),
                verticalArrangement = Arrangement.spacedBy(Dimensions.spaceM),
                horizontalAlignment = Alignment.CenterHorizontally
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
                        },
                        showDeleteOption = true
                    )
                }
            }
        }
    }
}