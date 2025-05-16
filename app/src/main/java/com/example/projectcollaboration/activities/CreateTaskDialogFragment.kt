package com.example.projectcollaboration.activities

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.projectcollaboration.databinding.DialogCreateTaskBinding
import com.example.projectcollaboration.utils.FirebaseUtils
import java.text.SimpleDateFormat
import java.util.*
import com.example.projectcollaboration.R // Import the R file

class CreateTaskDialogFragment : DialogFragment() {

    private var _binding: DialogCreateTaskBinding? = null
    private val binding get() = _binding!!

    private var projectId: String = ""
    private var dueDate: Long = 0
    private var onTaskCreatedListener: (() -> Unit)? = null

    fun setOnTaskCreatedListener(listener: () -> Unit) {
        onTaskCreatedListener = listener
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
        _binding = DialogCreateTaskBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSelectDate.setOnClickListener {
            showDatePicker()
        }

        binding.btnCreate.setOnClickListener {
            createTask()
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                dueDate = calendar.timeInMillis

                // Update button text with selected date
                val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                binding.btnSelectDate.text = dateFormat.format(calendar.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.show()
    }

    private fun createTask() {
        val title = binding.etTaskTitle.text.toString().trim()
        val description = binding.etTaskDescription.text.toString().trim()

        if (title.isEmpty()) {
            binding.etTaskTitle.error = "Title is required"
            return
        }

        binding.progressBar.visibility = View.VISIBLE

        FirebaseUtils.createTask(projectId, title, description, dueDate) { taskId ->
            binding.progressBar.visibility = View.GONE

            if (taskId != null) {
                Toast.makeText(context, "Task created successfully", Toast.LENGTH_SHORT).show()
                onTaskCreatedListener?.invoke()
                dismiss()
            } else {
                Toast.makeText(context, "Failed to create task", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_PROJECT_ID = "project_id"

        fun newInstance(projectId: String) = CreateTaskDialogFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_PROJECT_ID, projectId)
            }
        }
    }
}
