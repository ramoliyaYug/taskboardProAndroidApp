package com.example.projectcollaboration.models

import com.google.firebase.database.Exclude

data class User(
    var id: String = "",
    val name: String = "",
    val email: String = "",
    val badges: Map<String, Boolean> = HashMap()
) {
    // Helper function to get badges as a list of strings
    @Exclude
    fun getBadgesList(): List<String> {
        return badges.keys.toList()
    }
}
