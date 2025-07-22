package com.kashmir.bislei.screens.profileScreens

import android.content.Intent
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import coil.compose.AsyncImage
import com.kashmir.bislei.model.Post
import com.kashmir.bislei.ui.theme.Dimensions
import com.kashmir.bislei.viewModels.ProfileViewModel
import java.text.SimpleDateFormat
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.google.firebase.auth.FirebaseAuth
import com.kashmir.bislei.components.BisleiTopAppBar
import java.util.*

@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit = {},
    onEditProfile: () -> Unit,
    onPostClick: (Post) -> Unit,
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current
    val profile by viewModel.userProfile.collectAsState()
    val posts by viewModel.userPosts.collectAsState()

    val imageUrl = profile.profileImageUrl.ifEmpty {
        "https://dummyimage.com/120x120/cccccc/ffffff&text=No+Img"
    }

    var isRefreshing by remember { mutableStateOf(false) }

    val insets = WindowInsets.systemBars.asPaddingValues()

    fun formatDate(timestamp: Long): String {
        return try {
            val sdf = SimpleDateFormat("MMM yyyy", Locale.getDefault())
            sdf.format(Date(timestamp))
        } catch (e: Exception) {
            ""
        }
    }

    Scaffold(
        topBar = {
            BisleiTopAppBar(
                title = "Profile",
                onNavigateBack = onNavigateBack
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        SwipeRefresh(
            state = rememberSwipeRefreshState(isRefreshing),
            onRefresh = {
                isRefreshing = true
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                viewModel.forceRefreshUserPosts {
                    isRefreshing = false
                }
            },
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(paddingValues),
                contentPadding = PaddingValues(
                    horizontal = Dimensions.screenPadding,
                    vertical = Dimensions.spaceL
                ),
                verticalArrangement = Arrangement.spacedBy(Dimensions.spaceS),
                horizontalArrangement = Arrangement.spacedBy(Dimensions.spaceS)
            ) {
                item(span = { GridItemSpan(3) }) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = Dimensions.cardElevation
                        ),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Column(
                            modifier = Modifier.padding(Dimensions.contentPadding)
                        ) {
                            // Profile Header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Card(
                                        shape = MaterialTheme.shapes.medium,
                                        elevation = CardDefaults.cardElevation(
                                            defaultElevation = 2.dp
                                        )
                                    ) {
                                        val painter = rememberAsyncImagePainter(model = imageUrl)
                                        Image(
                                            painter = painter,
                                            contentDescription = "Profile Image",
                                            modifier = Modifier
                                                .size(80.dp)
                                                .clip(MaterialTheme.shapes.medium)
                                                .border(
                                                    width = 3.dp,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    shape = MaterialTheme.shapes.medium
                                                ),
                                            contentScale = ContentScale.Crop
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(Dimensions.spaceM))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = profile.name ?: "No Name",
                                            style = MaterialTheme.typography.headlineSmall,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        Spacer(modifier = Modifier.height(Dimensions.spaceXS))

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Schedule,
                                                contentDescription = null,
                                                modifier = Modifier.size(Dimensions.iconSizeSmall),
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(modifier = Modifier.width(Dimensions.spaceXS))
                                            Text(
                                                text = "Member since ${formatDate(profile.joinedDate ?: 0)}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }

                                // Menu Button
                                var expanded by remember { mutableStateOf(false) }

                                Box {
                                    FilledTonalIconButton(
                                        onClick = {
                                            expanded = !expanded
                                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.MoreVert,
                                            contentDescription = "Menu",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    DropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false },
                                        modifier = Modifier.width(200.dp)
                                    ) {
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    "Settings",
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                            },
                                            onClick = {
                                                expanded = false
                                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    Icons.Outlined.Settings,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        )

                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    "Help",
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                            },
                                            onClick = {
                                                expanded = false
                                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    Icons.AutoMirrored.Outlined.HelpOutline,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        )

                                        HorizontalDivider(
                                            modifier = Modifier.padding(vertical = Dimensions.spaceXS)
                                        )

                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    "Logout",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.error
                                                )
                                            },
                                            onClick = {
                                                expanded = false
                                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                                FirebaseAuth.getInstance().signOut()
                                                onLogout()
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    Icons.AutoMirrored.Outlined.ExitToApp,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.error
                                                )
                                            }
                                        )
                                    }
                                }
                            }

                            // Bio Section
                            if (!profile.bio.isNullOrEmpty()) {
                                Spacer(modifier = Modifier.height(Dimensions.spaceM))

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                    ),
                                    shape = MaterialTheme.shapes.medium
                                ) {
                                    Text(
                                        text = profile.bio ?: "",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(Dimensions.spaceM)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(Dimensions.spaceL))

                            // Action Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(Dimensions.spaceM)
                            ) {
                                FilledTonalButton(
                                    onClick = {
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                        onEditProfile()
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = MaterialTheme.shapes.medium
                                ) {
                                    Icon(
                                        Icons.Outlined.Edit,
                                        contentDescription = null,
                                        modifier = Modifier.size(Dimensions.iconSizeSmall)
                                    )
                                    Spacer(Modifier.width(Dimensions.spaceS))
                                    Text(
                                        "Edit Profile",
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }

                                OutlinedButton(
                                    onClick = {
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                        val message = "Check out my fishing profile on Bislei: ${profile.name}"
                                        val intent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, message)
                                        }
                                        context.startActivity(Intent.createChooser(intent, "Share via"))
                                    },
                                    shape = MaterialTheme.shapes.medium
                                ) {
                                    Icon(
                                        Icons.Outlined.Share,
                                        contentDescription = null,
                                        modifier = Modifier.size(Dimensions.iconSizeSmall)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(Dimensions.spaceL))

                            // Posts Section Header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.GridOn,
                                    contentDescription = null,
                                    modifier = Modifier.size(Dimensions.iconSize),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(Dimensions.spaceS))
                                Text(
                                    text = "Posts (${posts.size})",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(Dimensions.spaceM))
                }

                // Posts Grid
                if (posts.isEmpty()) {
                    item(span = { GridItemSpan(3) }) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = Dimensions.spaceXL),
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
                                Icon(
                                    imageVector = Icons.Outlined.PhotoLibrary,
                                    contentDescription = null,
                                    modifier = Modifier.size(Dimensions.iconSizeXL),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(Dimensions.spaceM))
                                Text(
                                    text = "No posts yet",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(Dimensions.spaceS))
                                Text(
                                    text = "Share your fishing adventures!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(posts) { post ->
                        Card(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clickable {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onPostClick(post)
                                },
                            shape = MaterialTheme.shapes.medium,
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = 2.dp
                            )
                        ) {
                            AsyncImage(
                                model = post.imageUrl ?: "",
                                contentDescription = "Post image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }
        }
    }
}