package com.example.wepost.models

data class NotificationModel (
    val notificationBy: String = "",
    val notificationAt: Long = 0L,
    val type: String = "",
    val postId: String = "",
    val postedBy: String = "",
    var isOpened: Boolean = false,
    var notificationId: String = ""
)