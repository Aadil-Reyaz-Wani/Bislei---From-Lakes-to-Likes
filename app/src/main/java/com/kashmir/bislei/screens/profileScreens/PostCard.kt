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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip
import com.kashmir.bislei.model.Post
import com.kashmir.bislei.viewModels.ProfileViewModel

@Composable
fun PostCard(
    post: Post,
    viewModel: ProfileViewModel,
    onPostDeleted: () -> Unit
) {
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
//            .wrapContentHeight()
        ,
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
                    text = "Dal Lake, Srinagar", // Optional: Make dynamic
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
                                viewModel.deletePost(post) { success ->
                                    if (success) {
                                        Toast.makeText(context, "Post deleted", Toast.LENGTH_SHORT).show()
                                        onPostDeleted()
                                    } else {
                                        Toast.makeText(context, "Failed to delete", Toast.LENGTH_SHORT).show()
                                    }
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

            // Likes & Comments
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Icon(Icons.Default.FavoriteBorder, contentDescription = "Like")
                Spacer(Modifier.width(4.dp))
                Text("45K")

                Spacer(modifier = Modifier.width(16.dp))

                Icon(Icons.Default.ChatBubbleOutline, contentDescription = "Comment")
                Spacer(Modifier.width(4.dp))
                Text("507")
            }
        }
    }
    Divider(
        thickness = 0.6.dp,
        color = Color.DarkGray,
        modifier = Modifier.padding(horizontal = 10.dp)
    )
}
