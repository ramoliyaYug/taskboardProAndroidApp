package com.example.projectcollaboration.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projectcollaboration.adapters.BadgeAdapter
import com.example.projectcollaboration.databinding.FragmentProfileBinding
import com.example.projectcollaboration.utils.FirebaseUtils
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var badgeAdapter: BadgeAdapter
    private val badges = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        badgeAdapter = BadgeAdapter(badges)
        binding.recyclerBadges.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = badgeAdapter
        }

        loadUserProfile()
    }

    private fun loadUserProfile() {
        binding.progressBar.visibility = View.VISIBLE

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            binding.progressBar.visibility = View.GONE
            return
        }

        binding.tvEmail.text = currentUser.email

        FirebaseUtils.getUserById(currentUser.uid) { user ->
            binding.progressBar.visibility = View.GONE

            if (user != null) {
                binding.tvName.text = user.name

                badges.clear()
                badges.addAll(user.getBadgesList())
                badgeAdapter.notifyDataSetChanged()

                if (badges.isEmpty()) {
                    binding.tvEmptyBadges.visibility = View.VISIBLE
                } else {
                    binding.tvEmptyBadges.visibility = View.GONE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
