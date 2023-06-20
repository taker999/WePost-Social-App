package com.example.wepost.models

data class PostModel (
    val postedBy: String = "",
    val postDescription: String = "",
    val postedAt: Long = 0L,
    var postId: String ="",
    var postImage: String = "",
    val likedBy: ArrayList<String> = ArrayList(),
    var commentCount: Int = 0
)