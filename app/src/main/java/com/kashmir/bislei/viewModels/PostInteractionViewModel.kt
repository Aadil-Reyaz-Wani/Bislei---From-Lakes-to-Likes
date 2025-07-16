package com.kashmir.bislei.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.kashmir.bislei.model.Comment
import com.kashmir.bislei.model.UserProfile
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

class PostInteractionViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _likedPosts = MutableStateFlow<Set<String>>(emptySet())
    private val likedPosts: StateFlow<Set<String>> = _likedPosts

    private val _likeCounts = MutableStateFlow<Map<String, Int>>(emptyMap())
    private val _commentCounts = MutableStateFlow<Map<String, Int>>(emptyMap())

    private val _commentsMap = MutableStateFlow<Map<String, List<Comment>>>(emptyMap())
    val commentsMap: StateFlow<Map<String, List<Comment>>> = _commentsMap

    private val _userProfilesMap = MutableStateFlow<Map<String, UserProfile>>(emptyMap())
    val userProfilesMap: StateFlow<Map<String, UserProfile>> = _userProfilesMap

    init {
        fetchLikedPosts()
    }

    private fun fetchLikedPosts() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("likes")
            .whereEqualTo("userId", uid)
            .addSnapshotListener { snapshot, _ ->
                val liked = snapshot?.documents?.mapNotNull { it.getString("postId") }?.toSet() ?: emptySet()
                _likedPosts.value = liked
            }
    }

    fun toggleLike(postId: String) {
        val uid = auth.currentUser?.uid ?: return
        val likeDoc = db.collection("likes").document("$uid-$postId")
        val postRef = db.collection("posts").document(postId)

        viewModelScope.launch {
            try {
                val isLiked = _likedPosts.value.contains(postId)

                db.runTransaction { transaction ->
                    val snapshot = transaction.get(postRef)
                    val currentLikes = snapshot.getLong("likesCount") ?: 0

                    if (isLiked) {
                        transaction.delete(likeDoc)
                        transaction.update(postRef, "likesCount", (currentLikes - 1).coerceAtLeast(0))
                    } else {
                        val likeData = mapOf("userId" to uid, "postId" to postId)
                        transaction.set(likeDoc, likeData)
                        transaction.update(postRef, "likesCount", currentLikes + 1)
                    }
                }.await()

                fetchLikeCount(postId)

            } catch (e: Exception) {
                Log.e("PostInteraction", "Toggle like failed: ${e.message}")
            }
        }
    }

    fun fetchCommentsForPost(postId: String) {
        db.collection("posts")
            .document(postId)
            .collection("comments")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FetchComments", "Error: ${error.message}")
                    return@addSnapshotListener
                }

                val comments = snapshot?.documents?.mapNotNull {
                    try {
                        it.toObject(Comment::class.java)
                    } catch (e: Exception) {
                        Log.e("CommentParse", "Failed to parse comment: ${e.message}")
                        null
                    }
                } ?: emptyList()

                _commentsMap.value = _commentsMap.value.toMutableMap().apply {
                    put(postId, comments)
                }

                // Fetch profile for each commenter
                comments.map { it.userId }.distinct().forEach { uid ->
                    fetchUserProfileIfMissing(uid)
                }
            }
    }

    fun fetchUserProfileIfMissing(userId: String) {
        if (_userProfilesMap.value.containsKey(userId)) return

        db.collection("users").document(userId).get()
            .addOnSuccessListener { doc ->
                doc?.toObject(UserProfile::class.java)?.let { profile ->
                    _userProfilesMap.value = _userProfilesMap.value.toMutableMap().apply {
                        put(userId, profile)
                    }
                }
            }
    }

    fun addComment(postId: String, content: String) {
        val uid = auth.currentUser?.uid ?: return
        if (content.isBlank()) return

        val commentId = UUID.randomUUID().toString()
        val comment = Comment(
            id = commentId,
            userId = uid,
            content = content.trim(),
            timestamp = System.currentTimeMillis()
        )

        viewModelScope.launch {
            try {
                val postRef = db.collection("posts").document(postId)
                val commentRef = postRef.collection("comments").document(commentId)

                commentRef.set(comment).await()

                db.runTransaction { transaction ->
                    val snapshot = transaction.get(postRef)
                    val currentCount = snapshot.getLong("commentsCount") ?: 0
                    transaction.update(postRef, "commentsCount", currentCount + 1)
                }.await()

                fetchCommentCount(postId)
                fetchCommentsForPost(postId)

            } catch (e: Exception) {
                Log.e("AddComment", "Failed to add comment: ${e.message}")
            }
        }
    }

    fun fetchLikeStatus(postId: String) {
        fetchLikeCount(postId)
        fetchCommentCount(postId)
        fetchCommentsForPost(postId)
    }

    private fun fetchLikeCount(postId: String) {
        val postRef = db.collection("posts").document(postId)
        postRef.addSnapshotListener { snapshot, _ ->
            val count = snapshot?.getLong("likesCount")?.toInt() ?: 0
            _likeCounts.value = _likeCounts.value.toMutableMap().apply {
                put(postId, count)
            }
        }
    }

    private fun fetchCommentCount(postId: String) {
        val postRef = db.collection("posts").document(postId)
        postRef.addSnapshotListener { snapshot, _ ->
            val count = snapshot?.getLong("commentsCount")?.toInt() ?: 0
            _commentCounts.value = _commentCounts.value.toMutableMap().apply {
                put(postId, count)
            }
        }
    }

    fun isPostLiked(postId: String): StateFlow<Boolean> {
        return likedPosts.map { it.contains(postId) }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            false
        )
    }

    fun getLikeCount(postId: String): StateFlow<Int> {
        return _likeCounts.map { it[postId] ?: 0 }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            0
        )
    }

    fun getCommentCount(postId: String): StateFlow<Int> {
        return _commentCounts.map { it[postId] ?: 0 }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            0
        )
    }
}
