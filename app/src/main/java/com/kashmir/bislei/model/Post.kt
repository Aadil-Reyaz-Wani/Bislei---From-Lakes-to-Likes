package com.kashmir.bislei.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Post(
    val id: String = "",
    val userId: String = "",
    val imageUrl: String = "",
    val caption: String? = "",
    val timestamp: Long = 0L
) : Parcelable
