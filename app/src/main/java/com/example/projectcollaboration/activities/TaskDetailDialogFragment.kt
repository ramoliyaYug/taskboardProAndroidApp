package com.example.projectcollaboration.activities

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projectcollaboration.R
import com.example.projectcollaboration.adapters.CommentAdapter
import com.example.projectcollaboration.databinding.DialogTaskDetailBinding
import com.example.projectcollaboration.models.Comment
import com.example.projectcollaboration.models.Task
import com.example.projectcollaboration.models.User
import com.example.projectcollaboration.utils.FirebaseUtils
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TaskDetailDialogFragment : DialogFragment() {

    private var _binding: DialogTaskDetailBinding? = null
    private val binding get() = _binding!!

    private var taskId: String = ""
    private lateinit var commentAdapter: CommentAdapter
    private val comments = mutableListOf<Comment>()
    private val projectMembers = mutableListOf<User>()
    private val memberIds = mutableListOf<String>()

    private var isActive = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            taskId = it.getString(ARG_TASK_ID) ?: ""
            Log.d("TaskDetailDialog", "onCreate with taskId: $taskId")
        }

        setStyle(STYLE_NORMAL, R.style.FullWidthDialog)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogTaskDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        isActive = true

        binding.btnClose.setOnClickListener {
            dismiss()
        }

        commentAdapter = CommentAdapter(comments)
        binding.recyclerComments.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = commentAdapter
        }

        binding.btnSendComment.setOnClickListener {
            val commentText = binding.etComment.text.toString().trim()
            if (commentText.isNotEmpty()) {
                addComment(commentText)
            }
        }

        loadTaskDetails()
    }

    private fun loadTaskDetails() {
        if (_binding == null) return

        binding.progressBar.visibility = View.VISIBLE

        FirebaseDatabase.getInstance().reference
            .child("tasks").child(taskId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!isActive || _binding == null) {
                        Log.d("TaskDetailDialog", "Dialog no longer active, ignoring callback")
                        return
                    }

                    try {
                        binding.progressBar.visibility = View.GONE

                        val task = snapshot.getValue(Task::class.java)
                        if (task != null) {
                            task.id = snapshot.key ?: ""
                            updateUI(task)
                            loadComments()
                            loadProjectMembers(task.projectId)
                        } else {
                            Toast.makeText(context, "Task not found", Toast.LENGTH_SHORT).show()
                            dismiss()
                        }
                    } catch (e: Exception) {
                        Log.e("TaskDetailDialog", "Error updating UI: ${e.message}")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    if (!isActive || _binding == null) return

                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(context, "Failed to load task details", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun updateUI(task: Task) {
        if (_binding == null) return

        binding.tvTaskTitle.text = task.title
        binding.tvTaskDescription.text = task.description
        binding.tvTaskStatus.text = "Status: ${task.status}"

        if (task.dueDate > 0) {
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            binding.tvDueDate.text = "Due: ${dateFormat.format(Date(task.dueDate))}"
            binding.tvDueDate.visibility = View.VISIBLE
        } else {
            binding.tvDueDate.visibility = View.GONE
        }

        if (task.assigneeId.isNotEmpty()) {
            FirebaseUtils.getUserById(task.assigneeId) { user ->
                if (!isActive || _binding == null) return@getUserById

                try {
                    binding.tvAssignee.text = "Assigned to: ${user?.name ?: "Unknown"}"
                    binding.tvAssignee.visibility = View.VISIBLE
                } catch (e: Exception) {
                    Log.e("TaskDetailDialog", "Error updating assignee: ${e.message}")
                }
            }
        } else {
            binding.tvAssignee.visibility = View.GONE
        }
    }

    private fun loadProjectMembers(projectId: String) {
        FirebaseDatabase.getInstance().reference
            .child("projects").child(projectId).child("members")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!isActive || _binding == null) return

                    memberIds.clear()
                    projectMembers.clear()

                    for (memberSnapshot in snapshot.children) {
                        memberSnapshot.key?.let { memberIds.add(it) }
                    }

                    if (memberIds.isEmpty()) {
                        return
                    }
                    var loadedCount = 0

                    for (userId in memberIds) {
                        FirebaseDatabase.getInstance().reference
                            .child("users").child(userId)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(userSnapshot: DataSnapshot) {
                                    if (!isActive || _binding == null) return

                                    val user = userSnapshot.getValue(User::class.java)
                                    user?.let {
                                        it.id = userSnapshot.key ?: ""
                                        projectMembers.add(it)
                                    }

                                    loadedCount++
                                    if (loadedCount == memberIds.size) {
                                        setupAssigneeSpinner()
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    loadedCount++
                                }
                            })
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun setupAssigneeSpinner() {
        if (!isActive || _binding == null) return

        val memberNames = projectMembers.map { it.name }.toMutableList()
        memberNames.add(0, "Select assignee")

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            memberNames
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.spinnerAssignee.adapter = adapter

        binding.btnAssign.setOnClickListener {
            val position = binding.spinnerAssignee.selectedItemPosition
            if (position > 0) {
                val selectedUserId = memberIds[position - 1]
                assignTask(selectedUserId)
            } else {
                Toast.makeText(context, "Please select a member", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun assignTask(assigneeId: String) {
        if (!isActive || _binding == null) return

        binding.progressAssign.visibility = View.VISIBLE

        FirebaseDatabase.getInstance().reference
            .child("tasks").child(taskId).child("assigneeId")
            .setValue(assigneeId)
            .addOnCompleteListener { task ->
                if (!isActive || _binding == null) return@addOnCompleteListener

                binding.progressAssign.visibility = View.GONE

                if (task.isSuccessful) {
                    Toast.makeText(context, "Task assigned successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Failed to assign task", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun loadComments() {
        if (!isActive || _binding == null) return

        FirebaseDatabase.getInstance().reference
            .child("comments")
            .orderByChild("taskId")
            .equalTo(taskId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!isActive || _binding == null) return

                    try {
                        comments.clear()

                        for (commentSnapshot in snapshot.children) {
                            val comment = commentSnapshot.getValue(Comment::class.java)
                            comment?.let {
                                it.id = commentSnapshot.key ?: ""
                                comments.add(it)
                            }
                        }

                        comments.sortBy { it.timestamp }

                        commentAdapter.notifyDataSetChanged()

                        if (comments.isNotEmpty()) {
                            binding.recyclerComments.scrollToPosition(comments.size - 1)
                        }

                        if (comments.isEmpty()) {
                            binding.tvEmptyComments.visibility = View.VISIBLE
                        } else {
                            binding.tvEmptyComments.visibility = View.GONE
                        }
                    } catch (e: Exception) {
                        Log.e("TaskDetailDialog", "Error updating comments: ${e.message}")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun addComment(text: String) {
        if (!isActive || _binding == null) return

        val currentUserId = FirebaseUtils.getCurrentUserId() ?: return
        binding.btnSendComment.isEnabled = false

        val commentRef = FirebaseDatabase.getInstance().reference.child("comments").push()

        val comment = Comment(
            id = commentRef.key ?: "",
            taskId = taskId,
            userId = currentUserId,
            text = text,
            timestamp = System.currentTimeMillis()
        )

        commentRef.setValue(comment)
            .addOnCompleteListener { task ->
                if (!isActive || _binding == null) return@addOnCompleteListener

                binding.btnSendComment.isEnabled = true

                if (task.isSuccessful) {
                    binding.etComment.text?.clear()
                } else {
                    Toast.makeText(context, "Failed to add comment", Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onResume() {
        super.onResume()
        isActive = true
    }

    override fun onPause() {
        super.onPause()
        isActive = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        isActive = false
        _binding = null
    }

    companion object {
        private const val ARG_TASK_ID = "TASK_ID"

        @JvmStatic
        fun newInstance(taskId: String): TaskDetailDialogFragment {
            return TaskDetailDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_TASK_ID, taskId)
                }
            }
        }
    }
}
