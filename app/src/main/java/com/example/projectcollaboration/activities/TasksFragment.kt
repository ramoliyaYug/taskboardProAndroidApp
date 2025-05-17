package com.example.projectcollaboration.activities

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projectcollaboration.adapters.TaskAdapter
import com.example.projectcollaboration.databinding.FragmentTasksBinding
import com.example.projectcollaboration.models.Task
import com.example.projectcollaboration.utils.FirebaseUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class TasksFragment : Fragment() {

    private var _binding: FragmentTasksBinding? = null
    private val binding get() = _binding!!

    private var projectId: String = ""
    private var isMyTasksMode: Boolean = false
    private lateinit var taskAdapter: TaskAdapter
    private val tasks = mutableListOf<Task>()

    private var isActive = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            projectId = it.getString(ARG_PROJECT_ID, "")
            isMyTasksMode = it.getBoolean(ARG_MY_TASKS_MODE, false)
            Log.d("TasksFragment", "onCreate with projectId: $projectId, isMyTasksMode: $isMyTasksMode")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTasksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        isActive = true
        Log.d("TasksFragment", "onViewCreated with projectId: $projectId, isMyTasksMode: $isMyTasksMode")

        taskAdapter = TaskAdapter(tasks,
            onTaskClick = { task ->
                val dialog = TaskDetailDialogFragment.newInstance(task.id)
                dialog.show(parentFragmentManager, "TaskDetailDialog")
            },
            onStatusChange = { taskId, newStatus ->
                FirebaseUtils.updateTaskStatus(taskId, newStatus) { success ->
                    if (!success) {
                        Log.e("TasksFragment", "Failed to update task status")
                    }
                }
            }
        )

        binding.recyclerTasks.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = taskAdapter
        }

        if (!isMyTasksMode && projectId.isNotEmpty()) {
            binding.fabAddTask.visibility = View.VISIBLE
            binding.fabAddTask.setOnClickListener {
                showCreateTaskDialog()
            }
        } else {
            binding.fabAddTask.visibility = View.GONE
        }

        if (isMyTasksMode) {
            loadMyTasks()
        } else if (projectId.isNotEmpty()) {
            loadProjectTasks()
        } else {
            Log.e("TasksFragment", "Invalid state: not in My Tasks mode and no project ID")
            binding.tvEmptyTasks.text = "Error: Invalid configuration"
            binding.tvEmptyTasks.visibility = View.VISIBLE
            binding.progressBar.visibility = View.GONE
        }
    }

    private fun loadProjectTasks() {
        if (_binding == null) return

        binding.progressBar.visibility = View.VISIBLE
        binding.tvEmptyTasks.visibility = View.GONE

        Log.d("TasksFragment", "Loading tasks for project: $projectId")

        FirebaseUtils.getProjectTasks(projectId) { tasksList ->
            if (!isActive || _binding == null) {
                Log.d("TasksFragment", "Fragment no longer active, ignoring callback")
                return@getProjectTasks
            }

            try {
                binding.progressBar.visibility = View.GONE

                Log.d("TasksFragment", "Loaded ${tasksList.size} tasks")

                tasks.clear()
                tasks.addAll(tasksList)
                taskAdapter.notifyDataSetChanged()

                if (tasks.isEmpty()) {
                    binding.tvEmptyTasks.text = "No tasks in this project yet"
                    binding.tvEmptyTasks.visibility = View.VISIBLE
                } else {
                    binding.tvEmptyTasks.visibility = View.GONE
                }
            } catch (e: Exception) {
                Log.e("TasksFragment", "Error updating UI: ${e.message}")
                binding.tvEmptyTasks.text = "Error loading tasks: ${e.message}"
                binding.tvEmptyTasks.visibility = View.VISIBLE
            }
        }
    }

    private fun loadMyTasks() {
        if (_binding == null) return

        binding.progressBar.visibility = View.VISIBLE
        binding.tvEmptyTasks.visibility = View.GONE

        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserId == null) {
            binding.progressBar.visibility = View.GONE
            binding.tvEmptyTasks.text = "Error: User not logged in"
            binding.tvEmptyTasks.visibility = View.VISIBLE
            return
        }

        Log.d("TasksFragment", "Loading tasks assigned to user: $currentUserId")

        val database = FirebaseDatabase.getInstance().reference
        database.child("tasks").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isActive || _binding == null) {
                    Log.d("TasksFragment", "Fragment no longer active, ignoring callback")
                    return
                }

                try {
                    binding.progressBar.visibility = View.GONE

                    val assignedTasks = mutableListOf<Task>()

                    for (taskSnapshot in snapshot.children) {
                        val task = taskSnapshot.getValue(Task::class.java)
                        if (task != null && task.assigneeId == currentUserId) {
                            if (task.id.isEmpty()) {
                                task.id = taskSnapshot.key ?: ""
                            }
                            assignedTasks.add(task)
                            Log.d("TasksFragment", "Found assigned task: ${task.title}")
                        }
                    }

                    Log.d("TasksFragment", "Loaded ${assignedTasks.size} tasks assigned to user")

                    tasks.clear()
                    tasks.addAll(assignedTasks)
                    taskAdapter.notifyDataSetChanged()

                    if (tasks.isEmpty()) {
                        binding.tvEmptyTasks.text = "No tasks assigned to you yet"
                        binding.tvEmptyTasks.visibility = View.VISIBLE
                    } else {
                        binding.tvEmptyTasks.visibility = View.GONE
                    }
                } catch (e: Exception) {
                    Log.e("TasksFragment", "Error updating UI: ${e.message}")
                    binding.tvEmptyTasks.text = "Error loading tasks: ${e.message}"
                    binding.tvEmptyTasks.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                if (!isActive || _binding == null) return

                Log.e("TasksFragment", "Database error: ${error.message}")
                binding.progressBar.visibility = View.GONE
                binding.tvEmptyTasks.text = "Error: ${error.message}"
                binding.tvEmptyTasks.visibility = View.VISIBLE
            }
        })
    }

    private fun showCreateTaskDialog() {
        val dialog = CreateTaskDialogFragment.newInstance(projectId)
        dialog.setOnTaskCreatedListener {
            loadProjectTasks()
        }
        dialog.show(parentFragmentManager, "CreateTaskDialog")
    }

    override fun onResume() {
        super.onResume()
        isActive = true
        if (isMyTasksMode) {
            loadMyTasks()
        } else if (projectId.isNotEmpty()) {
            loadProjectTasks()
        }
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
        private const val ARG_PROJECT_ID = "PROJECT_ID"
        private const val ARG_MY_TASKS_MODE = "MY_TASKS_MODE"

        @JvmStatic
        fun newInstance(projectId: String): TasksFragment {
            Log.d("TasksFragment", "Creating new instance with projectId: $projectId")
            return TasksFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PROJECT_ID, projectId)
                    putBoolean(ARG_MY_TASKS_MODE, false)
                }
            }
        }

        @JvmStatic
        fun newInstanceForMyTasks(): TasksFragment {
            Log.d("TasksFragment", "Creating new instance for My Tasks")
            return TasksFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(ARG_MY_TASKS_MODE, true)
                }
            }
        }
    }
}
