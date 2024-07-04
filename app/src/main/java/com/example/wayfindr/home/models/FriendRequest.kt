package com.example.wayfindr.home.models

data class FriendRequest(
    var id: String = "",
    val from: String = "",
    val fromName: String = "",
    val to: String = "",
    var status: String = "",
    val timestamp: Long = 0L
)