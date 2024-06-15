package com.example.wayfindr.home.models

data class FriendRequest(
    val id: String,
    val fromUserId: String,
    val toUserId: String,
    var status: String
)