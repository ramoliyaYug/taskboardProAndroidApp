package com.example.projectcollaboration.models

data class Automation(
    var id: String = "",
    val projectId: String = "",
    val triggerType: String = "", // task_moved, task_assigned, due_date_passed
    val triggerValue: String = "", // status value, userId, etc.
    val actionType: String = "", // assign_badge, move_task, send_notification
    val actionValue: String = "" // badge name, status value, etc.
)
