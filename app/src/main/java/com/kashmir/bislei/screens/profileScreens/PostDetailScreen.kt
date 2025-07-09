package com.kashmir.bislei.screens.detailScreens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.kashmir.bislei.model.Post

@Composable
fun PostDetailScreen(navController: NavHostController) {
    val post = navController.previousBackStackEntry
        ?.savedStateHandle
        ?.get<Post>("selectedPost")

    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }

    post?.let {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .wrapContentHeight()
                    .padding(top = 24.dp),
                elevation = CardDefaults.cardElevation(6.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // 🧭 Top bar with location and menu
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
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
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
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
                            }
                        }
                    }

                    // 🖼️ Image area
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(10.dp))
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

                    // ❤️ Likes & 💬 Comments
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
        }
    } ?: run {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Post not found")
        }
    }
}
