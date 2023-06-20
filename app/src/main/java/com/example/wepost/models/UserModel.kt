package com.example.wepost.models

data class UserModel (
    var uid: String = "",
    var name: String = "",
    var profession: String = "",
    val email: String = "",
    val password: String = "",
    var coverPhoto: String = "",
    var profilePhoto: String = "",
    var followerCount: Int = 0,
    var lastMessage: String = "",
    var fcmToken: String = ""
): java.io.Serializable