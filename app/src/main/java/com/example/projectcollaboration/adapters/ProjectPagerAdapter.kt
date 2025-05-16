package com.example.projectcollaboration.adapters

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.projectcollaboration.activities.AutomationsFragment
import com.example.projectcollaboration.activities.MembersFragment
import com.example.projectcollaboration.activities.TasksFragment

class ProjectPagerAdapter(
    activity: FragmentActivity,
    private val projectId: String
) : FragmentStateAdapter(activity) {

    init {
        Log.d("ProjectPagerAdapter", "Initialized with projectId: $projectId")
    }

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        Log.d("ProjectPagerAdapter", "Creating fragment at position $position with projectId: $projectId")

        return when (position) {
            0 -> TasksFragment.newInstance(projectId)
            1 -> MembersFragment.newInstance(projectId)
            2 -> AutomationsFragment.newInstance(projectId)
            else -> throw IllegalArgumentException("Invalid position: $position")
        }
    }
}
