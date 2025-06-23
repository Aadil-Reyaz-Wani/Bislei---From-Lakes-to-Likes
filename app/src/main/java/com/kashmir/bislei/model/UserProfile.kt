package com.kashmir.bislei.model

data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val bio: String = "",
    val email: String = "",
    val phone: String = "",
    val joinedDate: Long = 0L,
    val profileImageUrl: String = ""
)
