package com.example.projectcollaboration.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.projectcollaboration.R
import com.example.projectcollaboration.databinding.DialogCreateAutomationBinding
import com.example.projectcollaboration.utils.FirebaseUtils

class CreateAutomationDialogFragment : DialogFragment() {

    private var _binding: DialogCreateAutomationBinding? = null
    private val binding get() = _binding!!

    private var projectId: String = ""
    private var onAutomationCreatedListener: (() -> Unit)? = null

    fun setOnAutomationCreatedListener(listener: () -> Unit) {
        onAutomationCreatedListener = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            projectId = it.getString(ARG_PROJECT_ID) ?: ""
        }
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullWidthDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogCreateAutomationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupTriggerSpinner()

        binding.btnCreate.setOnClickListener {
            createAutomation()
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }
    }

    private fun setupTriggerSpinner() {
        val triggerTypes = listOf(
            "Select trigger type",
            "When task is moved to status",
            "When task is assigned to user",
            "When due date passes"
        )

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            triggerTypes
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.spinnerTriggerType.adapter = adapter

        binding.spinnerTriggerType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when (position) {
                    0 -> {
                        binding.layoutTriggerValue.visibility = View.GONE
                        binding.layoutActionType.visibility = View.GONE
                    }
                    1 -> {
                        binding.layoutTriggerValue.visibility = View.VISIBLE
                        binding.tvTriggerValueLabel.text = "Status:"
                        binding.etTriggerValue.hint = "e.g., Done"
                        setupActionTypeSpinner(position)
                        binding.layoutActionType.visibility = View.VISIBLE
                    }
                    2 -> {
                        binding.layoutTriggerValue.visibility = View.GONE
                        setupActionTypeSpinner(position)
                        binding.layoutActionType.visibility = View.VISIBLE
                    }
                    3 -> {
                        binding.layoutTriggerValue.visibility = View.GONE
                        setupActionTypeSpinner(position)
                        binding.layoutActionType.visibility = View.VISIBLE
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                binding.layoutTriggerValue.visibility = View.GONE
                binding.layoutActionType.visibility = View.GONE
            }
        }
    }

    private fun setupActionTypeSpinner(triggerPosition: Int) {
        val actionTypes = when (triggerPosition) {
            1 -> {
                listOf(
                    "Select action type",
                    "Assign badge to user",
                    "Send notification"
                )
            }
            2 -> {
                listOf(
                    "Select action type",
                    "Move task to status",
                    "Send notification"
                )
            }
            3 -> {
                listOf(
                    "Select action type",
                    "Move task to status",
                    "Send notification"
                )
            }
            else -> listOf("Select action type")
        }

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            actionTypes
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.spinnerActionType.adapter = adapter

        binding.spinnerActionType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position == 0) {
                    binding.layoutActionValue.visibility = View.GONE
                } else {
                    binding.layoutActionValue.visibility = View.VISIBLE

                    when (position) {
                        1 -> {
                            if (triggerPosition == 1) {
                                binding.tvActionValueLabel.text = "Badge name:"
                                binding.etActionValue.hint = "e.g., Completer"
                            } else {
                                binding.tvActionValueLabel.text = "Status:"
                                binding.etActionValue.hint = "e.g., In Progress"
                            }
                        }
                        2 -> {
                            binding.tvActionValueLabel.text = "Notification message:"
                            binding.etActionValue.hint = "e.g., Task needs attention"
                        }
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                binding.layoutActionValue.visibility = View.GONE
            }
        }
    }

    private fun createAutomation() {
        val triggerTypePosition = binding.spinnerTriggerType.selectedItemPosition
        val actionTypePosition = binding.spinnerActionType.selectedItemPosition

        if (triggerTypePosition == 0) {
            Toast.makeText(context, "Please select a trigger type", Toast.LENGTH_SHORT).show()
            return
        }

        if (actionTypePosition == 0) {
            Toast.makeText(context, "Please select an action type", Toast.LENGTH_SHORT).show()
            return
        }

        val triggerType = when (triggerTypePosition) {
            1 -> "task_moved"
            2 -> "task_assigned"
            3 -> "due_date_passed"
            else -> return
        }

        val triggerValue = if (triggerTypePosition == 1) {
            val value = binding.etTriggerValue.text.toString().trim()
            if (value.isEmpty()) {
                binding.etTriggerValue.error = "This field is required"
                return
            }
            value
        } else {
            ""
        }

        val actionType = when {
            triggerTypePosition == 1 && actionTypePosition == 1 -> "assign_badge"
            (triggerTypePosition == 2 || triggerTypePosition == 3) && actionTypePosition == 1 -> "move_task"
            actionTypePosition == 2 -> "send_notification"
            else -> return
        }

        val actionValue = binding.etActionValue.text.toString().trim()
        if (actionValue.isEmpty()) {
            binding.etActionValue.error = "This field is required"
            return
        }

        binding.progressBar.visibility = View.VISIBLE

        FirebaseUtils.createAutomation(
            projectId,
            triggerType,
            triggerValue,
            actionType,
            actionValue
        ) { automationId ->
            binding.progressBar.visibility = View.GONE

            if (automationId != null) {
                Toast.makeText(context, "Automation created successfully", Toast.LENGTH_SHORT).show()
                onAutomationCreatedListener?.invoke()
                dismiss()
            } else {
                Toast.makeText(context, "Failed to create automation", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_PROJECT_ID = "project_id"

        @JvmStatic
        fun newInstance(projectId: String): CreateAutomationDialogFragment {
            return CreateAutomationDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PROJECT_ID, projectId)
                }
            }
        }
    }
}
