package com.example.projectcollaboration.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.projectcollaboration.databinding.DialogInviteUserBinding
import com.example.projectcollaboration.utils.FirebaseUtils
import com.example.projectcollaboration.R

class InviteUserDialogFragment : DialogFragment() {

    private var _binding: DialogInviteUserBinding? = null
    private val binding get() = _binding!!

    private var projectId: String = ""

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
        _binding = DialogInviteUserBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnInvite.setOnClickListener {
            inviteUser()
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }
    }

    private fun inviteUser() {
        val email = binding.etEmail.text.toString().trim()

        if (email.isEmpty()) {
            binding.etEmail.error = "Email is required"
            return
        }

        binding.progressBar.visibility = View.VISIBLE

        FirebaseUtils.inviteUserToProject(projectId, email) { success ->
            binding.progressBar.visibility = View.GONE

            if (success) {
                Toast.makeText(context, "User invited successfully", Toast.LENGTH_SHORT).show()
                dismiss()
            } else {
                Toast.makeText(context, "Failed to invite user. Check if the email exists.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_PROJECT_ID = "project_id"

        fun newInstance(projectId: String) = InviteUserDialogFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_PROJECT_ID, projectId)
            }
        }
    }
}
