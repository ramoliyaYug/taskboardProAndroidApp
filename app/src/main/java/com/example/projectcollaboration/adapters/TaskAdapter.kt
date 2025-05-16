package com.example.projectcollaboration.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.example.projectcollaboration.R
import com.example.projectcollaboration.databinding.ItemTaskBinding
import com.example.projectcollaboration.models.Task
import com.example.projectcollaboration.utils.FirebaseUtils
import java.text.SimpleDateFormat
import java.util.*

class TaskAdapter(
    private val tasks: List<Task>,
    private val onTaskClick: (Task) -> Unit,
    private val onStatusChange: (String, String) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(tasks[position])
    }

    override fun getItemCount() = tasks.size

    inner class TaskViewHolder(private val binding: ItemTaskBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(task: Task) {
            binding.tvTaskTitle.text = task.title
            binding.tvTaskStatus.text = task.status

            // Set due date
            if (task.dueDate > 0) {
                binding.tvDueDate.visibility = View.VISIBLE
                binding.tvDueDate.text = "Due: ${dateFormat.format(Date(task.dueDate))}"
            } else {
                binding.tvDueDate.visibility = View.GONE
            }

            // Set assignee
            if (task.assigneeId.isNotEmpty()) {
                binding.tvAssignee.visibility = View.VISIBLE
                FirebaseUtils.getUserById(task.assigneeId) { user ->
                    binding.tvAssignee.text = "Assigned to: ${user?.name ?: "Unknown"}"
                }
            } else {
                binding.tvAssignee.visibility = View.GONE
            }

            // Set status color
            val statusColor = when (task.status) {
                "To Do" -> R.color.status_todo
                "In Progress" -> R.color.status_in_progress
                "Done" -> R.color.status_done
                else -> R.color.status_todo
            }
            binding.tvTaskStatus.setBackgroundResource(statusColor)

            // Set click listener
            binding.root.setOnClickListener {
                onTaskClick(task)
            }

            // Set status change menu
            binding.btnChangeStatus.setOnClickListener { view ->
                showStatusMenu(view, task)
            }
        }

        private fun showStatusMenu(view: View, task: Task) {
            val popup = PopupMenu(view.context, view)
            popup.menuInflater.inflate(R.menu.status_menu, popup.menu)

            popup.setOnMenuItemClickListener { menuItem ->
                val newStatus = when (menuItem.itemId) {
                    R.id.status_todo -> "To Do"
                    R.id.status_in_progress -> "In Progress"
                    R.id.status_done -> "Done"
                    else -> return@setOnMenuItemClickListener false
                }

                if (newStatus != task.status) {
                    onStatusChange(task.id, newStatus)
                }

                true
            }

            popup.show()
        }
    }
}
