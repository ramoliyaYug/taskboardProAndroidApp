package com.example.projectcollaboration.activities

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.projectcollaboration.databinding.DialogCreateProjectBinding
import com.example.projectcollaboration.utils.FirebaseUtils
import com.example.projectcollaboration.R

class CreateProjectDialogFragment : DialogFragment() {

    private var _binding: DialogCreateProjectBinding? = null
    private val binding get() = _binding!!

    private var onProjectCreatedListener: (() -> Unit)? = null

    fun setOnProjectCreatedListener(listener: () -> Unit) {
        onProjectCreatedListener = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullWidthDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogCreateProjectBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnCreate.setOnClickListener {
            createProject()
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }
    }

    private fun createProject() {
        val title = binding.etProjectTitle.text.toString().trim()
        val description = binding.etProjectDescription.text.toString().trim()

        if (title.isEmpty()) {
            binding.etProjectTitle.error = "Title is required"
            return
        }

        binding.progressBar.visibility = View.VISIBLE

        Log.d("CreateProjectDialog", "Creating project: $title")

        FirebaseUtils.createProject(title, description) { projectId ->
            binding.progressBar.visibility = View.GONE

            if (projectId != null) {
                Log.d("CreateProjectDialog", "Project created with ID: $projectId")
                Toast.makeText(context, "Project created successfully", Toast.LENGTH_SHORT).show()
                onProjectCreatedListener?.invoke()
                dismiss()
            } else {
                Log.e("CreateProjectDialog", "Failed to create project")
                Toast.makeText(context, "Failed to create project", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
