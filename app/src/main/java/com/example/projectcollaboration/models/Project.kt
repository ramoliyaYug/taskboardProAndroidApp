package com.example.projectcollaboration.models

import com.google.firebase.database.Exclude

data class Project(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val createdBy: String = "",
    val members: Map<String, Boolean> = HashMap()
) {
    constructor() : this("", "", "", "", HashMap())

    @Exclude
    fun getMemberIds(): List<String> {
        return members.keys.toList()
    }

    @Exclude
    fun isMember(userId: String): Boolean {
        return members.containsKey(userId)
    }
}
