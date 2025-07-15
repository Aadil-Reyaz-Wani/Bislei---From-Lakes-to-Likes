package com.kashmir.bislei.screens.profileScreens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.kashmir.bislei.model.Post
import com.kashmir.bislei.components.CommentBottomSheet
import com.kashmir.bislei.ui.theme.Dimensions
import com.kashmir.bislei.viewModels.PostInteractionViewModel
import com.kashmir.bislei.viewModels.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostCard(
    post: Post,
    profileViewModel: ProfileViewModel,
    postInteractionViewModel: PostInteractionViewModel,
    onPostDeleted: () -> Unit,
    showDeleteOption: Boolean
) {
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current
    var showMenu by remember { mutableStateOf(false) }

    val isLiked by postInteractionViewModel.isPostLiked(post.id).collectAsState()
    val likeCount by postInteractionViewModel.getLikeCount(post.id).collectAsState()
    val commentCount by postInteractionViewModel.getCommentCount(post.id).collectAsState()

    var showCommentSheet by remember { mutableStateOf(false) }
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    val userProfilesMap by postInteractionViewModel.userProfilesMap.collectAsState()
    val uploaderProfile = userProfilesMap[post.userId]

    LaunchedEffect(post.id) {
        postInteractionViewModel.fetchLikeStatus(post.id)
        postInteractionViewModel.fetchCommentsForPost(post.id)
        postInteractionViewModel.fetchUserProfileIfMissing(post.userId)
    }

    if (showCommentSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCommentSheet = false },
            sheetState = bottomSheetState
        ) {
            CommentBottomSheet(
                postId = post.id,
                onDismissRequest = { showCommentSheet = false },
                postInteractionViewModel = postInteractionViewModel,
                profileViewModel = profileViewModel
            )
        }
    }

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
            // Uploader Info Section
            if (uploaderProfile != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = Dimensions.spaceS)
                ) {
                    AsyncImage(
                        model = uploaderProfile.profileImageUrl,
                        contentDescription = "Uploader Profile Image",
                        modifier = Modifier
                            .size(36.dp)
                            .clip(MaterialTheme.shapes.small),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(Dimensions.spaceS))
                    Text(
                        text = uploaderProfile.name,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(Dimensions.iconSizeSmall),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(Dimensions.spaceXS))
                    Text(
                        text = "Dal Lake, Srinagar",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Box {
                    FilledTonalIconButton(
                        onClick = {
                            showMenu = !showMenu
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                    ) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "Menu",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.width(180.dp)
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "Copy URL",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Post URL", post.imageUrl)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "URL copied", Toast.LENGTH_SHORT).show()
                                showMenu = false
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.ContentCopy,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        )

                        DropdownMenuItem(
                            text = {
                                Text(
                                    "Save",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            onClick = {
                                showMenu = false
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.BookmarkBorder,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        )

                        DropdownMenuItem(
                            text = {
                                Text(
                                    "Download",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            onClick = {
                                showMenu = false
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Outlined.Download,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        )

                        // Conditionally show Delete
                        if (showDeleteOption) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = Dimensions.spaceXS)
                            )

                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "Delete",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    profileViewModel.deletePost(post) { success ->
                                        Toast.makeText(
                                            context,
                                            if (success) "Post deleted" else "Failed to delete",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        if (success) onPostDeleted()
                                    }
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Outlined.Delete,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(Dimensions.spaceM))

            // Post Image
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                shape = MaterialTheme.shapes.medium,
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 2.dp
                )
            ) {
                AsyncImage(
                    model = post.imageUrl,
                    contentDescription = "Post image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(Dimensions.spaceM))

            // Interaction Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Like Button
                FilledTonalIconButton(
                    onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        postInteractionViewModel.toggleLike(post.id)
                    }
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (isLiked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(Dimensions.spaceXS))

                Text(
                    text = "$likeCount",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.width(Dimensions.spaceM))

                // Comment Button
                FilledTonalIconButton(
                    onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        showCommentSheet = true
                    }
                ) {
                    Icon(
                        Icons.Outlined.ChatBubbleOutline,
                        contentDescription = "Comment",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(Dimensions.spaceXS))

                Text(
                    text = "$commentCount",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Caption
            if (!post.caption.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(Dimensions.spaceM))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    shape = MaterialTheme.shapes.medium
                ) {
                    ExpandableCaption(
                        caption = post.caption!!,
                        modifier = Modifier.padding(Dimensions.spaceM)
                    )
                }
            }
        }
    }
}

@Composable
fun ExpandableCaption(
    caption: String,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    val maxChars = 100 // Approximate for 2 lines
    val primaryColor = MaterialTheme.colorScheme.primary

    val shouldTruncate = caption.length > maxChars

    val displayText = remember(caption, isExpanded) {
        if (!shouldTruncate) {
            AnnotatedString(caption)
        } else if (isExpanded) {
            buildAnnotatedString {
                append(caption)
                append("  ")
                pushStringAnnotation(tag = "see_less", annotation = "see_less")
                withStyle(
                    style = SpanStyle(
                        color = primaryColor,
                        fontWeight = FontWeight.Medium
                    )
                ) {
                    append("See less")
                }
                pop()
            }
        } else {
            buildAnnotatedString {
                val truncated = caption.take(maxChars).trimEnd()
                append(truncated)
                append("… ")
                pushStringAnnotation(tag = "see_more", annotation = "see_more")
                withStyle(
                    style = SpanStyle(
                        color = primaryColor,
                        fontWeight = FontWeight.Medium
                    )
                ) {
                    append("See more")
                }
                pop()
            }
        }
    }

    ClickableText(
        text = displayText,
        style = MaterialTheme.typography.bodyMedium.copy(
            color = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        modifier = modifier,
        onClick = { offset ->
            displayText.getStringAnnotations(tag = "see_more", start = offset, end = offset)
                .firstOrNull()?.let {
                    isExpanded = true
                }
            displayText.getStringAnnotations(tag = "see_less", start = offset, end = offset)
                .firstOrNull()?.let {
                    isExpanded = false
                }
        }
    )
}