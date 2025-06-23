package com.kashmir.bislei.screens.profileScreens

import android.content.Intent
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.IosShare
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
import androidx.compose.ui.text.font.FontStyle
import coil.compose.AsyncImage
import com.kashmir.bislei.model.Post
import com.kashmir.bislei.viewModels.ProfileViewModel
import java.text.SimpleDateFormat
import java.util.*


@Composable
fun ProfileScreen(
    onEditProfile: () -> Unit,
    onPostClick: (Post) -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val context = LocalContext.current
    val profile by viewModel.userProfile.collectAsState()
    val posts by viewModel.userPosts.collectAsState()
    val imageUrl = if (profile.profileImageUrl.isEmpty()) {
        "https://dummyimage.com/80x80/cccccc/ffffff&text=No+Img"
    } else {
        profile.profileImageUrl
    }

    fun formatDate(timestamp: Long): String {
        return try {
            val sdf = SimpleDateFormat("MMM yyyy", Locale.getDefault())
            sdf.format(Date(timestamp))
        } catch (e: Exception) {
            ""
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp, top = 60.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item(span = { GridItemSpan(3) }) {
            Column {
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

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = profile.bio ?: "",
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(onClick = onEditProfile, modifier = Modifier
                        .weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.Black)
                        Spacer(Modifier.width(4.dp))
                        Text("Edit Profile", color = Color.Black)
                    }
                    Spacer(Modifier.padding(horizontal = 10.dp))
                    OutlinedButton(onClick = {
                        val message = "Check out my fishing profile on Bislei: ${profile.name}"
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, message)
                        }
                        context.startActivity(Intent.createChooser(intent, "Share via"))
                    },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.IosShare, contentDescription = "Share", tint = Color.Black)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Posts", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        items(posts) { post ->
            AsyncImage(
                model = post.imageUrl ?: "",
                contentDescription = null,
                modifier = Modifier
                    .aspectRatio(1f)
                    .clickable { onPostClick(post) },
                contentScale = ContentScale.Crop
            )
        }
    }
}
