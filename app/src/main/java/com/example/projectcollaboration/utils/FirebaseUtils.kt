package com.example.projectcollaboration.utils

import android.util.Log
import com.example.projectcollaboration.models.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.*

object FirebaseUtils {
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference

    // Authentication
    fun getCurrentUserId(): String? = auth.currentUser?.uid

    fun getCurrentUserEmail(): String? = auth.currentUser?.email

    fun signOut() = auth.signOut()

    // User Operations
    // Update the createUserProfile method to store badges as a map
    fun createUserProfile(userId: String, name: String, email: String, callback: (Boolean) -> Unit) {
        // Initialize with empty badges map
        val badgesMap = HashMap<String, Boolean>()
        val user = User(userId, name, email, badgesMap)

        database.child("users").child(userId).setValue(user)
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }

    fun getUserById(userId: String, callback: (User?) -> Unit) {
        database.child("users").child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                callback(user)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(null)
            }
        })
    }

    // Project Operations
    fun createProject(title: String, description: String, callback: (String?) -> Unit) {
        val userId = getCurrentUserId() ?: return callback(null)
        val projectId = database.child("projects").push().key ?: return callback(null)

        Log.d("FirebaseUtils", "Creating project with ID: $projectId")

        // Create a map for members with the current user
        val membersMap = HashMap<String, Boolean>()
        membersMap[userId] = true

        // Create project with members as a map
        val project = Project(
            id = projectId,
            title = title,
            description = description,
            createdBy = userId,
            members = membersMap
        )

        // Save the project
        database.child("projects").child(projectId).setValue(project)
            .addOnSuccessListener {
                Log.d("FirebaseUtils", "Project created successfully with ID: $projectId")
                callback(projectId)
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseUtils", "Failed to create project: ${e.message}")
                callback(null)
            }
    }

    fun getUserProjects(callback: (List<Project>) -> Unit) {
        val userId = getCurrentUserId() ?: return callback(emptyList())

        Log.d("FirebaseUtils", "Getting projects for user: $userId")

        // Query projects where the current user is a member
        database.child("projects").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val projects = mutableListOf<Project>()

                Log.d("FirebaseUtils", "Total projects in database: ${snapshot.childrenCount}")

                for (projectSnapshot in snapshot.children) {
                    try {
                        val project = projectSnapshot.getValue(Project::class.java)

                        if (project != null) {
                            // Ensure the ID is set
                            val projectWithId = if (project.id.isEmpty()) {
                                project.copy(id = projectSnapshot.key ?: "")
                            } else {
                                project
                            }

                            // Check if this user is a member
                            if (projectWithId.isMember(userId)) {
                                projects.add(projectWithId)
                                Log.d("FirebaseUtils", "Added project: ${projectWithId.id}, ${projectWithId.title}")
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("FirebaseUtils", "Error deserializing project: ${e.message}")
                    }
                }

                Log.d("FirebaseUtils", "Returning ${projects.size} projects")
                callback(projects)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseUtils", "Error getting projects: ${error.message}")
                callback(emptyList())
            }
        })
    }

    fun inviteUserToProject(projectId: String, email: String, callback: (Boolean) -> Unit) {
        // First find user by email
        database.child("users").orderByChild("email").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val userId = snapshot.children.first().key ?: return callback(false)

                        // Add user to project members
                        database.child("projects").child(projectId).child("members")
                            .child(userId).setValue(true)
                            .addOnSuccessListener { callback(true) }
                            .addOnFailureListener { callback(false) }
                    } else {
                        callback(false)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(false)
                }
            })
    }

    // Task Operations
    fun createTask(projectId: String, title: String, description: String, dueDate: Long, callback: (String?) -> Unit) {
        val taskId = database.child("tasks").push().key ?: return callback(null)

        val task = Task(
            id = taskId,
            projectId = projectId,
            title = title,
            description = description,
            dueDate = dueDate,
            status = "To Do"
        )

        database.child("tasks").child(taskId).setValue(task)
            .addOnSuccessListener { callback(taskId) }
            .addOnFailureListener { callback(null) }
    }

    fun getProjectTasks(projectId: String, callback: (List<Task>) -> Unit) {
        database.child("tasks").orderByChild("projectId").equalTo(projectId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val tasks = mutableListOf<Task>()
                    for (taskSnapshot in snapshot.children) {
                        val task = taskSnapshot.getValue(Task::class.java)
                        task?.let { tasks.add(it) }
                    }
                    callback(tasks)
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(emptyList())
                }
            })
    }

    fun getTasksAssignedToUser(userId: String, callback: (List<Task>) -> Unit) {
        database.child("tasks").orderByChild("assigneeId").equalTo(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val tasks = mutableListOf<Task>()
                    for (taskSnapshot in snapshot.children) {
                        val task = taskSnapshot.getValue(Task::class.java)
                        task?.let { tasks.add(it) }
                    }
                    callback(tasks)
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(emptyList())
                }
            })
    }

    fun getTaskById(taskId: String, callback: (Task?) -> Unit) {
        database.child("tasks").child(taskId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val task = snapshot.getValue(Task::class.java)
                callback(task)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(null)
            }
        })
    }

    fun updateTaskStatus(taskId: String, newStatus: String, callback: (Boolean) -> Unit) {
        database.child("tasks").child(taskId).child("status").setValue(newStatus)
            .addOnSuccessListener {
                callback(true)
                // Check for automations
                checkAndExecuteAutomations(taskId, "task_moved", newStatus)
            }
            .addOnFailureListener { callback(false) }
    }

    fun assignTask(taskId: String, assigneeId: String, callback: (Boolean) -> Unit) {
        database.child("tasks").child(taskId).child("assigneeId").setValue(assigneeId)
            .addOnSuccessListener {
                callback(true)
                // Check for automations
                checkAndExecuteAutomations(taskId, "task_assigned", assigneeId)
            }
            .addOnFailureListener { callback(false) }
    }

    // Automation Operations
    fun createAutomation(
        projectId: String,
        triggerType: String,
        triggerValue: String,
        actionType: String,
        actionValue: String,
        callback: (String?) -> Unit
    ) {
        val automationId = database.child("automations").push().key ?: return callback(null)

        val automation = Automation(
            id = automationId,
            projectId = projectId,
            triggerType = triggerType,
            triggerValue = triggerValue,
            actionType = actionType,
            actionValue = actionValue
        )

        database.child("automations").child(automationId).setValue(automation)
            .addOnSuccessListener { callback(automationId) }
            .addOnFailureListener { callback(null) }
    }

    // Update the getProjectAutomations method to properly handle automation IDs
    fun getProjectAutomations(projectId: String, callback: (List<Automation>) -> Unit) {
        database.child("automations").orderByChild("projectId").equalTo(projectId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val automations = mutableListOf<Automation>()
                    for (automationSnapshot in snapshot.children) {
                        val automation = automationSnapshot.getValue(Automation::class.java)
                        if (automation != null) {
                            // Ensure the ID is set
                            if (automation.id.isEmpty()) {
                                automation.id = automationSnapshot.key ?: ""
                            }
                            automations.add(automation)
                            Log.d("FirebaseUtils", "Loaded automation: ${automation.id}, ${automation.triggerType}")
                        }
                    }
                    Log.d("FirebaseUtils", "Loaded ${automations.size} automations for project $projectId")
                    callback(automations)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseUtils", "Error loading automations: ${error.message}")
                    callback(emptyList())
                }
            })
    }

    private fun checkAndExecuteAutomations(taskId: String, triggerType: String, triggerValue: String) {
        // Get the task to find its project
        database.child("tasks").child(taskId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val task = snapshot.getValue(Task::class.java) ?: return

                // Find automations for this project with matching trigger
                database.child("automations")
                    .orderByChild("projectId")
                    .equalTo(task.projectId)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(automationsSnapshot: DataSnapshot) {
                            for (automationSnapshot in automationsSnapshot.children) {
                                val automation = automationSnapshot.getValue(Automation::class.java) ?: continue

                                if (automation.triggerType == triggerType && automation.triggerValue == triggerValue) {
                                    executeAutomation(automation, task)
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {}
                    })
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // Update the executeAutomation method to store badges as a map entry
    private fun executeAutomation(automation: Automation, task: Task) {
        when (automation.actionType) {
            "assign_badge" -> {
                // Assign badge to task assignee
                if (task.assigneeId.isNotEmpty()) {
                    // Store badge as a map entry with value true
                    database.child("users").child(task.assigneeId)
                        .child("badges").child(automation.actionValue).setValue(true)

                    Log.d("FirebaseUtils", "Assigned badge '${automation.actionValue}' to user ${task.assigneeId}")
                }
            }
            "move_task" -> {
                // Move task to new status
                database.child("tasks").child(task.id).child("status")
                    .setValue(automation.actionValue)

                Log.d("FirebaseUtils", "Moved task ${task.id} to status '${automation.actionValue}'")
            }
            "send_notification" -> {
                // In a real app, this would trigger a notification
                // For now, we'll just log it in a notifications collection
                val notificationData = hashMapOf(
                    "userId" to task.assigneeId,
                    "message" to "Task '${task.title}' needs attention: ${automation.actionValue}",
                    "timestamp" to ServerValue.TIMESTAMP
                )
                database.child("notifications").push().setValue(notificationData)

                Log.d("FirebaseUtils", "Sent notification for task ${task.id}")
            }
        }
    }

    // Comments
    fun addComment(taskId: String, text: String, callback: (Boolean) -> Unit) {
        val userId = getCurrentUserId() ?: return callback(false)
        val commentId = database.child("comments").push().key ?: return callback(false)

        val comment = Comment(
            id = commentId,
            taskId = taskId,
            userId = userId,
            text = text,
            timestamp = System.currentTimeMillis()
        )

        database.child("comments").child(commentId).setValue(comment)
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }

    fun getTaskComments(taskId: String, callback: (List<Comment>) -> Unit) {
        database.child("comments").orderByChild("taskId").equalTo(taskId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val comments = mutableListOf<Comment>()
                    for (commentSnapshot in snapshot.children) {
                        val comment = commentSnapshot.getValue(Comment::class.java)
                        comment?.let { comments.add(it) }
                    }
                    callback(comments)
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(emptyList())
                }
            })
    }
}
