package com.example.projectcollaboration.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.projectcollaboration.databinding.ItemAutomationBinding
import com.example.projectcollaboration.models.Automation

class AutomationAdapter(
    private val automations: List<Automation>
) : RecyclerView.Adapter<AutomationAdapter.AutomationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AutomationViewHolder {
        val binding = ItemAutomationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AutomationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AutomationViewHolder, position: Int) {
        holder.bind(automations[position])
    }

    override fun getItemCount() = automations.size

    inner class AutomationViewHolder(private val binding: ItemAutomationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(automation: Automation) {
            val triggerDesc = when (automation.triggerType) {
                "task_moved" -> "When task is moved to '${automation.triggerValue}'"
                "task_assigned" -> "When task is assigned to a user"
                "due_date_passed" -> "When due date passes"
                else -> automation.triggerType
            }

            val actionDesc = when (automation.actionType) {
                "assign_badge" -> "Assign badge '${automation.actionValue}'"
                "move_task" -> "Move task to '${automation.actionValue}'"
                "send_notification" -> "Send notification"
                else -> automation.actionType
            }

            binding.tvTrigger.text = triggerDesc
            binding.tvAction.text = actionDesc
        }
    }
}
