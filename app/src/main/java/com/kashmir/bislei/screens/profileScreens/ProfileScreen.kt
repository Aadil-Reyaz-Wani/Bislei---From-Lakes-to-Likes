package com.kashmir.bislei.screens.profileScreens

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.RectangleShape
import coil.compose.AsyncImage
import com.kashmir.bislei.model.Post
import com.kashmir.bislei.viewModels.ProfileViewModel
import java.text.SimpleDateFormat
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import java.util.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onEditProfile: () -> Unit,
    onPostClick: (Post) -> Unit,
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val context = LocalContext.current
    val profile by viewModel.userProfile.collectAsState()
    val posts by viewModel.userPosts.collectAsState()

    val imageUrl = profile.profileImageUrl.ifEmpty {
        "https://dummyimage.com/80x80/cccccc/ffffff&text=No+Img"
    }

    var isRefreshing by remember { mutableStateOf(false) }

    fun formatDate(timestamp: Long): String {
        return try {
            val sdf = SimpleDateFormat("MMM yyyy", Locale.getDefault())
            sdf.format(Date(timestamp))
        } catch (e: Exception) {
            ""
        }
    }

    SwipeRefresh(
        state = rememberSwipeRefreshState(isRefreshing),
        onRefresh = {
            isRefreshing = true
            viewModel.forceRefreshUserPosts {
                isRefreshing = false
            }
        }
    ){
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 60.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item(span = { GridItemSpan(3) }) {
                Column {
                    // Profile Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val painter = rememberAsyncImagePainter(model = imageUrl)
                            Image(
                                painter = painter,
                                contentDescription = "Profile Image",
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, color = Color.Black, CircleShape),
                                contentScale = ContentScale.Crop
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            Column {
                                Text(
                                    text = profile.name ?: "No Name",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Member since: ${formatDate(profile.joinedDate ?: 0)}",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                                )
                            }
                        }

                        var expanded by remember { mutableStateOf(false) }

                        Box {
                            IconButton(onClick = { expanded = !expanded }) {
                                Icon(
                                    imageVector = if (expanded) Icons.Default.Close else Icons.Default.Menu,
                                    contentDescription = "Menu Toggle"
                                )
                            }

                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                modifier = Modifier.width(200.dp)
                                    .background(color = Color.White)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Settings") },
                                    onClick = {
                                        expanded = false
                                    },
                                    trailingIcon = {
                                        Icon(Icons.Default.Settings, contentDescription = null)
                                    }
                                )
                                Divider(thickness = 1.dp)

                                DropdownMenuItem(
                                    text = { Text("Help") },
                                    onClick = {
                                        expanded = false
                                    },
                                    trailingIcon = {
                                        Icon(Icons.Default.HelpOutline, contentDescription = null)
                                    }
                                )
                                Divider(thickness = 1.dp)

                                DropdownMenuItem(
                                    text = { Text("Logout") },
                                    onClick = {
                                        expanded = false
                                        onLogout()
                                    },
                                    trailingIcon = {
                                        Icon(Icons.Default.ExitToApp, contentDescription = null)
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(profile.bio ?: "", style = MaterialTheme.typography.bodyLarge)

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedButton(
                            onClick = onEditProfile,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, tint = Color.Black)
                            Spacer(Modifier.width(4.dp))
                            Text("Edit Profile", color = Color.Black)
                        }

                        Spacer(Modifier.width(10.dp))

                        OutlinedButton(
                            onClick = {
                                val message = "Check out my fishing profile on Bislei: ${profile.name}"
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, message)
                                }
                                context.startActivity(Intent.createChooser(intent, "Share via"))
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.IosShare, contentDescription = null, tint = Color.Black)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("Posts", style = MaterialTheme.typography.titleMedium)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // Grid Post
            items(posts) { post ->
                AsyncImage(
                    model = post.imageUrl ?: "",
                    contentDescription = null,
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clickable { onPostClick(post) }
                        .border(BorderStroke(0.6.dp, color = Color.Black), RectangleShape),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}
