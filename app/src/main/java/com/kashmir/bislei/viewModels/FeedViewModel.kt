package com.kashmir.bislei.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.kashmir.bislei.model.Post
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FeedViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    private val _feedPosts = MutableStateFlow<List<Post>>(emptyList())
    val feedPosts: StateFlow<List<Post>> = _feedPosts

    init {
        listenToFeedPosts()
    }

    private fun listenToFeedPosts() {
        db.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FeedViewModel", "Error: ${error.message}")
                    return@addSnapshotListener
                }

                val posts = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        doc.toObject(Post::class.java)
                    } catch (e: Exception) {
                        Log.e("PostParse", "Error: ${e.message}")
                        null
                    }
                } ?: emptyList()

                _feedPosts.value = posts
            }
    }
}
