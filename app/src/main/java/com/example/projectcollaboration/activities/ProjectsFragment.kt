package com.example.projectcollaboration.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projectcollaboration.adapters.ProjectAdapter
import com.example.projectcollaboration.databinding.FragmentProjectsBinding
import com.example.projectcollaboration.models.Project
import com.example.projectcollaboration.utils.FirebaseUtils

class ProjectsFragment : Fragment() {

    private var _binding: FragmentProjectsBinding? = null
    private val binding get() = _binding!!

    private lateinit var projectAdapter: ProjectAdapter
    private val projects = mutableListOf<Project>()

    private var isActive = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProjectsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        isActive = true
        Log.d("ProjectsFragment", "onViewCreated")

        projectAdapter = ProjectAdapter(projects) { project ->
            val intent = Intent(requireContext(), ProjectDetailActivity::class.java).apply {
                putExtra("PROJECT_ID", project.id)
                putExtra("PROJECT_TITLE", project.title)
            }
            startActivity(intent)
        }

        binding.recyclerProjects.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = projectAdapter
        }

        binding.fabAddProject.setOnClickListener {
            showCreateProjectDialog()
        }

        loadProjects()
    }

    private fun loadProjects() {
        if (_binding == null) return

        binding.progressBar.visibility = View.VISIBLE
        binding.tvEmptyProjects.visibility = View.GONE

        Log.d("ProjectsFragment", "Loading projects...")

        FirebaseUtils.getUserProjects { projectsList ->
            if (!isActive || _binding == null) {
                Log.d("ProjectsFragment", "Fragment no longer active, ignoring callback")
                return@getUserProjects
            }

            try {
                binding.progressBar.visibility = View.GONE

                Log.d("ProjectsFragment", "Loaded ${projectsList.size} projects")

                projects.clear()
                projects.addAll(projectsList)
                projectAdapter.notifyDataSetChanged()

                if (projects.isEmpty()) {
                    binding.tvEmptyProjects.visibility = View.VISIBLE
                    Log.d("ProjectsFragment", "No projects to display")
                } else {
                    binding.tvEmptyProjects.visibility = View.GONE
                    for (project in projects) {
                        Log.d("ProjectsFragment", "Project in list: ${project.id}, ${project.title}")
                    }
                }
            } catch (e: Exception) {
                Log.e("ProjectsFragment", "Error updating UI: ${e.message}")
            }
        }
    }

    private fun showCreateProjectDialog() {
        val dialog = CreateProjectDialogFragment()
        dialog.setOnProjectCreatedListener {
            Toast.makeText(context, "Project created successfully! Refreshing list...", Toast.LENGTH_SHORT).show()
            loadProjects()
        }
        dialog.show(parentFragmentManager, "CreateProjectDialog")
    }

    override fun onResume() {
        super.onResume()
        isActive = true
        loadProjects()
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
}
