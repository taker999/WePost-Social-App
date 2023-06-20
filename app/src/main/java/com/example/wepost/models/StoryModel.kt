package com.example.wepost.models

data class StoryModel (
    val storyBy: String = "",
    var storyAt: Long = 0L,
    val stories: ArrayList<UserStoriesModel> = ArrayList()
)