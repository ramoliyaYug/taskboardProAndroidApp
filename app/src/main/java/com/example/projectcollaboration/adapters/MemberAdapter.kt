package com.example.projectcollaboration.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.projectcollaboration.databinding.ItemMemberBinding
import com.example.projectcollaboration.models.User

class MemberAdapter(
    private val members: List<User>
) : RecyclerView.Adapter<MemberAdapter.MemberViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        val binding = ItemMemberBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MemberViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        holder.bind(members[position])
    }

    override fun getItemCount() = members.size

    inner class MemberViewHolder(private val binding: ItemMemberBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User) {
            binding.tvMemberName.text = user.name
            binding.tvMemberEmail.text = user.email

            // Set badge count using the helper method
            val badgeCount = user.getBadgesList().size
            if (badgeCount > 0) {
                binding.tvBadgeCount.text = "$badgeCount badges"
            } else {
                binding.tvBadgeCount.text = "No badges"
            }
        }
    }
}
