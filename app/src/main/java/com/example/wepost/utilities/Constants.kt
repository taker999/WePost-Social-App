package com.example.wepost.utilities

class Constants {

    val KEY_COLLECTION_USERS = "users"
    val KEY_NAME = "name"
    val KEY_EMAIL = "email"
    val KEY_PASSWORD = "password"
    val KEY_PREFERENCE_NAME = "chatAppPreference"
    val KEY_IS_SIGNED_IN = "isSignedIn"
    val KEY_USER_ID = "userId"
    val KEY_IMAGE = "image"
    val KEY_FCM_TOKEN = "fcmToken"
    val KEY_USER = "user"
    val KEY_COLLECTION_CHATS = "chats"
    val KEY_SENDER_ID = "senderId"
    val KEY_RECEIVER_ID = "receiverId"
    val KEY_MESSAGE = "message"
    val KEY_TIMESTAMP = "timestamp"
    val KEY_COLLECTION_CONVERSATIONS = "conversations"
    val KEY_SENDER_NAME = "senderName"
    val KEY_RECEIVER_NAME = "receiverName"
    val KEY_SENDER_IMAGE = "senderImage"
    val KEY_RECEIVER_IMAGE = "receiverImage"
    val KEY_LAST_MESSAGE = "lastMessage"
    val KEY_AVAILABILITY = "availability"
    val REMOTE_MSG_AUTHORIZATION = "Authorization"
    val REMOTE_MSG_CONTENT_TYPE = "Content-Type"
    val REMOTE_MSG_DATA = "data"
    val REMOTE_MSG_REGISTRATION_IDS = "registration_ids"

    private var remoteMsgHeaders: HashMap<String, String>? = null
    fun getRemoteMsgHeaders(): HashMap<String, String> {
        if (remoteMsgHeaders == null) {
            remoteMsgHeaders = HashMap()
            remoteMsgHeaders!![REMOTE_MSG_AUTHORIZATION] = "key=AAAAWXarq2c:APA91bEAyY0vMsNpO0DZG-H_PJG-XIN0mOnBAMEQIOIlrKlwnnCiLOtM8v_E4Yr0SIZ5SwTub_s1Yez9WE4mEsIPOh4VtJgjJ70xz9IvmSrRD2oa97BNVrcHgJVTmZ9rcp8uFW_uP6kH"
            remoteMsgHeaders!![REMOTE_MSG_CONTENT_TYPE] = "application/json"
        }
        return remoteMsgHeaders as HashMap<String, String>
    }

}