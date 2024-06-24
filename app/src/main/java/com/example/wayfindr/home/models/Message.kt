package com.example.wayfindr.home.models

data class Message(
    val senderId: String = "",
    val receiverId: String = "",
    val message: String = "",
    val timestamp: Any? = null
)