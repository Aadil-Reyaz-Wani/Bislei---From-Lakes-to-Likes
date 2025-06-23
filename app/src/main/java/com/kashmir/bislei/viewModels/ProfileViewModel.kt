package com.kashmir.bislei.viewModels

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.kashmir.bislei.model.Post
import com.kashmir.bislei.model.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

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
        fetchUserPosts()
    }

    private fun fetchUserProfile() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("users").document(uid).addSnapshotListener { snapshot, _ ->
            snapshot?.toObject(UserProfile::class.java)?.let {
                _userProfile.value = it
            }
        }
    }

    private fun fetchUserPosts() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("posts")
            .whereEqualTo("uid", uid)
            .addSnapshotListener { snapshot, _ ->
                snapshot?.let {
                    val posts = it.documents.mapNotNull { doc ->
                        doc.toObject(Post::class.java)
                    }
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
                val profileImageRef = storage.reference
                    .child("profile_pictures/$uid.jpg")
                profileImageRef.putFile(imageUri).await()
                val downloadUrl = profileImageRef.downloadUrl.await().toString()
                updatedData["profileImageUrl"] = downloadUrl
                println("Uploaded image: $downloadUrl")
            }

            if (updatedData.isNotEmpty()) {
                userDoc.set(updatedData, SetOptions.merge()).await()
            }
            fetchUserProfile()
            true
        } catch (e: Exception) {
            println("Error updating profile: ${e.message}")
            false
        }
    }

    fun uploadProfileData(profile: UserProfile) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).set(profile)
    }
}
