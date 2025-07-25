package com.kashmir.bislei.screens.feedScreens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.size.Dimension
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.kashmir.bislei.screens.profileScreens.PostCard
import com.kashmir.bislei.components.BisleiTopAppBar
import com.kashmir.bislei.ui.theme.Dimensions
import com.kashmir.bislei.viewModels.FeedViewModel
import com.kashmir.bislei.viewModels.PostInteractionViewModel
import com.kashmir.bislei.viewModels.ProfileViewModel


@Composable
fun HomeScreen(
    feedViewModel: FeedViewModel,
    profileViewModel: ProfileViewModel,
    postInteractionViewModel: PostInteractionViewModel,
    modifier: Modifier = Modifier
) {
    val posts by feedViewModel.feedPosts.collectAsState()
    var refreshing by remember { mutableStateOf(false) }
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = false)

    Scaffold(
        topBar = {
            BisleiTopAppBar(
                title = "Bislei",
                showLogo = true
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = Dimensions.spaceS)
        ) {
            SwipeRefresh(
                state = swipeRefreshState,
                onRefresh = {
                    refreshing = true
                    refreshing = false
                },

            ) {
                LazyColumn(
                    contentPadding = it,
                    verticalArrangement = Arrangement.spacedBy(8.dp),

                ) {
                    items(posts) { post ->
                        PostCard(
                            post = post,
                            profileViewModel = profileViewModel,
                            postInteractionViewModel = postInteractionViewModel,
                            onPostDeleted = {}, // No delete action in feed
                            showDeleteOption = false
                        )
                    }
                }
            }
        }
    }
}