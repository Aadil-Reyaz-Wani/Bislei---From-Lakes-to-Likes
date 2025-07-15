package com.kashmir.bislei.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.kashmir.bislei.model.Comment
import com.kashmir.bislei.model.UserProfile
import com.kashmir.bislei.viewModels.PostInteractionViewModel
import com.kashmir.bislei.viewModels.ProfileViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CommentBottomSheet(
    postId: String,
    onDismissRequest: () -> Unit,
    postInteractionViewModel: PostInteractionViewModel,
    profileViewModel: ProfileViewModel
) {
    val commentText = remember { mutableStateOf(TextFieldValue()) }
    val userProfile by profileViewModel.userProfile.collectAsState()
    val commentsMap by postInteractionViewModel.commentsMap.collectAsState()
    val userProfilesMap by postInteractionViewModel.userProfilesMap.collectAsState()

    val comments = commentsMap[postId] ?: emptyList()

    Surface(
        modifier = Modifier.fillMaxHeight(0.85f),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(bottom = 12.dp)
            ) {
                items(comments) { comment ->
                    val commenter = userProfilesMap[comment.userId] ?: UserProfile()
                    CommentItem(comment = comment, profile = commenter)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(userProfile.profileImageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "User Profile",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                )

                Spacer(modifier = Modifier.width(8.dp))

                TextField(
                    value = commentText.value,
                    onValueChange = { commentText.value = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Add a comment...") },
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color(0xFFF0F0F0),
                        focusedContainerColor = Color(0xFFF0F0F0),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.width(8.dp))

                Button(onClick = {
                    val trimmed = commentText.value.text.trim()
                    if (trimmed.isNotEmpty()) {
                        postInteractionViewModel.addComment(postId, trimmed)
                        commentText.value = TextFieldValue("")
                    }
                }) {
                    Text("Comment")
                }
            }
        }
    }
}


@Composable
fun CommentItem(comment: Comment, profile: UserProfile) {
    val timestampText = remember(comment.timestamp) {
        val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
        sdf.format(Date(comment.timestamp))
    }

    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxWidth()
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(profile.profileImageUrl)
                .crossfade(true)
                .build(),
            contentDescription = "Comment Profile",
            contentScale = ContentScale.Crop ,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color.Gray)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = profile.name.ifBlank { "User" },
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = timestampText,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }

            Text(
                text = comment.content,
                style = MaterialTheme.typography.bodySmall,
                color = Color.DarkGray,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}
