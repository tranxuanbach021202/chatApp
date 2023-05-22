package com.example.appchat.model

data class Message(
    val message: String? = "",
    val messageId: String? = "",
    val dateTime: String? = "",
    val senderId: String? = "",
    val imgUrl: String? = "")
