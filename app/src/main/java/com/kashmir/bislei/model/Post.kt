package com.kashmir.bislei.model

data class Post(
    val id: String = "",
    val userId: String = "",
    val imageUrl: String = "",
    val caption: String = "",
    val timestamp: Long = 0L
)
