package com.example.projectcollaboration.models

data class Comment(
    var id: String = "",
    val taskId: String = "",
    val userId: String = "",
    val text: String = "",
    val timestamp: Long = 0
)
