package com.kashmir.bislei.screens.profileScreens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.kashmir.bislei.model.Post
import com.kashmir.bislei.postComment.CommentBottomSheet
import com.kashmir.bislei.viewModels.PostInteractionViewModel
import com.kashmir.bislei.viewModels.ProfileViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostCard(
    post: Post,
    profileViewModel: ProfileViewModel,
    postInteractionViewModel: PostInteractionViewModel,
    onPostDeleted: () -> Unit
) {
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }

    val isLiked by postInteractionViewModel.isPostLiked(post.id).collectAsState()
    val likeCount by postInteractionViewModel.getLikeCount(post.id).collectAsState()
    val commentCount by postInteractionViewModel.getCommentCount(post.id).collectAsState()

    var showCommentSheet by remember { mutableStateOf(false) }
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()


    LaunchedEffect(post.id) {
        postInteractionViewModel.fetchLikeStatus(post.id)
        postInteractionViewModel.fetchCommentsForPost(post.id)
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
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Dal Lake, Srinagar",
                    style = MaterialTheme.typography.bodyMedium
                )

                Box {
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Copy URL") },
                            onClick = {
                                val clipboard =
                                    context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Post URL", post.imageUrl)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "URL copied", Toast.LENGTH_SHORT).show()
                                showMenu = false
                            },
                            trailingIcon = { Icon(Icons.Default.ContentCopy, null) }
                        )

                        DropdownMenuItem(
                            text = { Text("Save") },
                            onClick = { showMenu = false },
                            trailingIcon = { Icon(Icons.Default.Save, null) }
                        )

                        DropdownMenuItem(
                            text = { Text("Download") },
                            onClick = { showMenu = false },
                            trailingIcon = { Icon(Icons.Default.Download, null) }
                        )

                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = {
                                showMenu = false
                                profileViewModel.deletePost(post) { success ->
                                    Toast.makeText(
                                        context,
                                        if (success) "Post deleted" else "Failed to delete",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    if (success) onPostDeleted()
                                }
                            },
                            trailingIcon = { Icon(Icons.Default.Delete, null) }
                        )
                    }
                }
            }

            // Post Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(0.dp))
                    .background(Color.LightGray)
            ) {
                AsyncImage(
                    model = post.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Likes & Comments Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                IconButton(onClick = {
                    postInteractionViewModel.toggleLike(post.id)
                }) {
                    Icon(
                        imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (isLiked) Color.Red else Color.Black
                    )
                }
                Text(text = "$likeCount")

                Spacer(modifier = Modifier.width(16.dp))

                IconButton(onClick = {
                    showCommentSheet = true
                }) {
                    Icon(Icons.Default.ChatBubbleOutline, contentDescription = "Comment")
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text("$commentCount")
            }

            // Here

            if (!post.caption.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                ExpandableCaption(caption = post.caption!!)
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
                        color = Color.Gray,
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
                        color = Color.Gray,
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
        style = MaterialTheme.typography.bodySmall,
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
