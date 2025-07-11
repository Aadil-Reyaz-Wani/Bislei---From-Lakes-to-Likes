package com.kashmir.bislei.viewModels

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.kashmir.bislei.model.Post
import com.kashmir.bislei.model.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.*

class ProfileViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile

    private val _userPosts = MutableStateFlow<List<Post>>(emptyList())
    val userPosts: StateFlow<List<Post>> = _userPosts

    init {
        fetchUserProfile()
        listenToUserPosts()
    }

    private fun fetchUserProfile() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).addSnapshotListener { snapshot, _ ->
            snapshot?.toObject(UserProfile::class.java)?.let {
                _userProfile.value = it
            }
        }
    }

    private fun listenToUserPosts() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("posts")
            .whereEqualTo("userId", uid)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("Firestore", "Listen failed: ${error.message}")
                    return@addSnapshotListener
                }

                snapshot?.let {
                    val posts = it.documents.mapNotNull { doc ->
                        try {
                            doc.toObject(Post::class.java)
                        } catch (e: Exception) {
                            Log.e("PostParse", "Failed to parse post: ${doc.id} - ${e.message}")
                            null
                        }
                    }
                    Log.d("PostsFetched", "Fetched ${posts.size} posts")
                    _userPosts.value = posts
                }
            }
    }

    suspend fun updateUserProfile(
        name: String,
        bio: String,
        imageUri: Uri?,
        email: String,
        phone: String
    ): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        val userDoc = db.collection("users").document(uid)

        return try {
            val updatedData = mutableMapOf<String, Any>()

            if (name.isNotBlank()) updatedData["name"] = name
            if (bio.isNotBlank()) updatedData["bio"] = bio
            if (email.isNotBlank()) updatedData["email"] = email
            if (phone.isNotBlank()) updatedData["phone"] = phone

            if (imageUri != null) {
                val profileImageRef = storage.reference.child("profile_pictures/$uid.jpg")
                profileImageRef.putFile(imageUri).await()
                val downloadUrl = profileImageRef.downloadUrl.await().toString()
                updatedData["profileImageUrl"] = downloadUrl
            }

            if (updatedData.isNotEmpty()) {
                userDoc.set(updatedData, SetOptions.merge()).await()
            }

            fetchUserProfile()
            true
        } catch (e: Exception) {
            Log.e("ProfileUpdate", "Error: ${e.message}")
            false
        }
    }

    fun uploadProfileData(profile: UserProfile) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).set(profile)
    }

    suspend fun uploadPost(imageUri: Uri, caption: String = ""): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        val postId = UUID.randomUUID().toString()
        val storageRef = storage.reference.child("posts/$uid/$postId.jpg")

        return try {
            storageRef.putFile(imageUri).await()
            val url = storageRef.downloadUrl.await().toString()

            val postMap = mapOf(
                "id" to postId,
                "userId" to uid,
                "imageUrl" to url,
                "caption" to caption.ifBlank { "" },
                "timestamp" to System.currentTimeMillis(),
                "likesCount" to 0,
                "commentsCount" to 0
            )

            db.collection("posts").document(postId).set(postMap).await()
            db.collection("users").document(uid)
                .collection("posts").document(postId).set(postMap).await()

            return true
        } catch (e: Exception) {
            Log.e("UploadPost", "Failed: ${e.message}")
            return false
        }
    }

    fun deletePost(post: Post, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (post.imageUrl.isNotEmpty()) {
                    try {
                        val imageRef = storage.getReferenceFromUrl(post.imageUrl)
                        imageRef.delete().await()
                    } catch (e: Exception) {
                        Log.w("DeletePost", "Image not found: ${e.message}")
                    }
                }

                db.collection("posts").document(post.id).delete().await()
                db.collection("users").document(post.userId)
                    .collection("posts").document(post.id).delete().await()

                delay(300)

                withContext(Dispatchers.Main) {
                    forceRefreshUserPosts()
                    onComplete(true)
                }
            } catch (e: Exception) {
                Log.e("DeletePost", "Error: ${e.message}")
                withContext(Dispatchers.Main) {
                    onComplete(false)
                }
            }
        }
    }

    fun refreshUserPosts(onFinished: () -> Unit) {
        viewModelScope.launch {
            forceRefreshUserPosts(onFinished)
        }
    }

    fun forceRefreshUserPosts(onFinished: () -> Unit = {}) {
        val uid = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                val snapshot = db.collection("posts")
                    .whereEqualTo("userId", uid)
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get()
                    .await()

                val refreshedPosts = snapshot.documents.mapNotNull { it.toObject(Post::class.java) }
                _userPosts.value = refreshedPosts
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "forceRefresh failed: ${e.message}")
            } finally {
                onFinished()
            }
        }
    }
}
