package com.example.projectcollaboration.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.projectcollaboration.databinding.ItemBadgeBinding

class BadgeAdapter(
    private val badges: List<String>
) : RecyclerView.Adapter<BadgeAdapter.BadgeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BadgeViewHolder {
        val binding = ItemBadgeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BadgeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BadgeViewHolder, position: Int) {
        holder.bind(badges[position])
    }

    override fun getItemCount() = badges.size

    inner class BadgeViewHolder(private val binding: ItemBadgeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(badge: String) {
            binding.tvBadgeName.text = badge
        }
    }
}
