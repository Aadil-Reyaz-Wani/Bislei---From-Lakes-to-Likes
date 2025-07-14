package com.kashmir.bislei.screens.feedScreens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.kashmir.bislei.screens.profileScreens.PostCard
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
            BisleiTopAppBar()
        }
    ) {
        Column(
            modifier = Modifier
        ) {
            SwipeRefresh(
                state = swipeRefreshState,
                onRefresh = {
                    refreshing = true
                    // Optional manual refresh logic if needed
                    refreshing = false
                }
            ) {
                LazyColumn(
                    contentPadding = it,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(posts) { post ->
                        PostCard(
                            post = post,
                            profileViewModel = profileViewModel,
                            postInteractionViewModel = postInteractionViewModel,
                            onPostDeleted = {} // No delete action in feed
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BisleiTopAppBar(modifier: Modifier = Modifier) {
    TopAppBar(
        title = {
            Row {
                Text(
                    text = "Bislei",
//                    style = MaterialTheme.typography.displayMedium
                )
            }
        },
        modifier = modifier
    )
}