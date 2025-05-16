package com.example.projectcollaboration.models

import com.google.firebase.database.Exclude

data class Project(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val createdBy: String = "",

    // Change members from List to Map to match Firebase structure
    val members: Map<String, Boolean> = HashMap()
) {
    // Empty constructor required for Firebase
    constructor() : this("", "", "", "", HashMap())

    // Helper method to get member IDs as a list
    @Exclude
    fun getMemberIds(): List<String> {
        return members.keys.toList()
    }

    // Helper method to check if a user is a member
    @Exclude
    fun isMember(userId: String): Boolean {
        return members.containsKey(userId)
    }
}
