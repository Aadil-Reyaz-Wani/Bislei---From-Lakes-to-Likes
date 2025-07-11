package com.kashmir.bislei.screens.profileScreens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
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
        elevation = CardDefaults.cardElevation(0.dp),
        shape = RoundedCornerShape(0.dp),
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
        }
    }

    Divider(
        thickness = 0.6.dp,
        color = Color.DarkGray,
        modifier = Modifier.padding(horizontal = 10.dp)
    )
}
