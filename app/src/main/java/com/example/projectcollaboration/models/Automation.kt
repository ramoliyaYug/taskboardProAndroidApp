package com.example.projectcollaboration.models

data class Automation(
    var id: String = "",
    val projectId: String = "",
    val triggerType: String = "",
    val triggerValue: String = "",
    val actionType: String = "",
    val actionValue: String = ""
)
