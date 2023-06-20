package com.example.wepost.chat.models

data class Message (
    val uid: String = "",
    val message: String = "",
    val time: Long = 0L,
    var messageId: String = ""
)