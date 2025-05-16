package com.example.projectcollaboration.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projectcollaboration.adapters.MemberAdapter
import com.example.projectcollaboration.databinding.FragmentMembersBinding
import com.example.projectcollaboration.models.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MembersFragment : Fragment() {

    private var _binding: FragmentMembersBinding? = null
    private val binding get() = _binding!!

    private lateinit var projectId: String
    private lateinit var memberAdapter: MemberAdapter
    private val members = mutableListOf<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            projectId = it.getString("PROJECT_ID") ?: ""
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMembersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up RecyclerView
        memberAdapter = MemberAdapter(members)
        binding.recyclerMembers.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = memberAdapter
        }

        // Load members
        loadMembers()
    }

    private fun loadMembers() {
        binding.progressBar.visibility = View.VISIBLE

        // Get project members
        FirebaseDatabase.getInstance().reference
            .child("projects").child(projectId).child("members")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val memberIds = mutableListOf<String>()

                    for (memberSnapshot in snapshot.children) {
                        memberSnapshot.key?.let { memberIds.add(it) }
                    }

                    if (memberIds.isEmpty()) {
                        binding.progressBar.visibility = View.GONE
                        binding.tvEmptyMembers.visibility = View.VISIBLE
                        return
                    }

                    // Load user details for each member
                    members.clear()
                    var loadedCount = 0

                    for (userId in memberIds) {
                        FirebaseDatabase.getInstance().reference
                            .child("users").child(userId)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(userSnapshot: DataSnapshot) {
                                    val user = userSnapshot.getValue(User::class.java)
                                    user?.let { members.add(it) }

                                    loadedCount++
                                    if (loadedCount == memberIds.size) {
                                        // All members loaded
                                        binding.progressBar.visibility = View.GONE
                                        memberAdapter.notifyDataSetChanged()

                                        if (members.isEmpty()) {
                                            binding.tvEmptyMembers.visibility = View.VISIBLE
                                        } else {
                                            binding.tvEmptyMembers.visibility = View.GONE
                                        }
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    loadedCount++
                                    if (loadedCount == memberIds.size) {
                                        binding.progressBar.visibility = View.GONE
                                    }
                                }
                            })
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    binding.progressBar.visibility = View.GONE
                    binding.tvEmptyMembers.visibility = View.VISIBLE
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(projectId: String) = MembersFragment().apply {
            arguments = Bundle().apply {
                putString("PROJECT_ID", projectId)
            }
        }
    }
}
