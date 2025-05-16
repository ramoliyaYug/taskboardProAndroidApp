package com.example.projectcollaboration.models

data class Task(
    var id: String = "",
    val projectId: String = "",
    val title: String = "",
    val description: String = "",
    val dueDate: Long = 0,
    val status: String = "To Do",
    val assigneeId: String = ""
)
