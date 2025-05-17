package com.example.projectcollaboration.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projectcollaboration.adapters.AutomationAdapter
import com.example.projectcollaboration.databinding.FragmentAutomationsBinding
import com.example.projectcollaboration.models.Automation
import com.example.projectcollaboration.utils.FirebaseUtils

class AutomationsFragment : Fragment() {

    private var _binding: FragmentAutomationsBinding? = null
    private val binding get() = _binding!!

    private lateinit var projectId: String
    private lateinit var automationAdapter: AutomationAdapter
    private val automations = mutableListOf<Automation>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            projectId = it.getString(ARG_PROJECT_ID) ?: ""
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAutomationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        automationAdapter = AutomationAdapter(automations)
        binding.recyclerAutomations.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = automationAdapter
        }

        binding.fabAddAutomation.setOnClickListener {
            showCreateAutomationDialog()
        }

        loadAutomations()
    }

    private fun loadAutomations() {
        binding.progressBar.visibility = View.VISIBLE
        binding.tvEmptyAutomations.visibility = View.GONE

        FirebaseUtils.getProjectAutomations(projectId) { automationsList ->
            if (_binding == null) return@getProjectAutomations

            binding.progressBar.visibility = View.GONE

            automations.clear()
            automations.addAll(automationsList)
            automationAdapter.notifyDataSetChanged()

            if (automations.isEmpty()) {
                binding.tvEmptyAutomations.visibility = View.VISIBLE
            } else {
                binding.tvEmptyAutomations.visibility = View.GONE
            }
        }
    }

    private fun showCreateAutomationDialog() {
        val dialog = CreateAutomationDialogFragment.newInstance(projectId)
        dialog.setOnAutomationCreatedListener {
            loadAutomations()
        }
        dialog.show(parentFragmentManager, "CreateAutomationDialog")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_PROJECT_ID = "project_id"

        @JvmStatic
        fun newInstance(projectId: String): AutomationsFragment {
            return AutomationsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PROJECT_ID, projectId)
                }
            }
        }
    }
}
