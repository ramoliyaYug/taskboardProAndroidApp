package com.example.projectcollaboration.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.projectcollaboration.R
import com.example.projectcollaboration.adapters.ProjectPagerAdapter
import com.example.projectcollaboration.databinding.ActivityProjectDetailBinding
import com.example.projectcollaboration.utils.FirebaseUtils
import com.google.android.material.tabs.TabLayoutMediator

class ProjectDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProjectDetailBinding
    private lateinit var projectId: String
    private lateinit var projectTitle: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProjectDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get project details from intent
        projectId = intent.getStringExtra("PROJECT_ID") ?: ""
        projectTitle = intent.getStringExtra("PROJECT_TITLE") ?: "Project"

        Log.d("ProjectDetailActivity", "Opened project: $projectId, $projectTitle")

        if (projectId.isEmpty()) {
            finish()
            return
        }

        // Set up toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = projectTitle
            setDisplayHomeAsUpEnabled(true)
        }

        // Set up ViewPager and TabLayout
        setupViewPager()
    }

    private fun setupViewPager() {
        val pagerAdapter = ProjectPagerAdapter(this, projectId)
        binding.viewPager.adapter = pagerAdapter

        // Connect TabLayout with ViewPager
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Tasks"
                1 -> "Members"
                2 -> "Automations"
                else -> "Unknown"
            }
        }.attach()

        // Handle page changes
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                // You can handle page selection here if needed
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.project_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.menu_invite -> {
                showInviteDialog()
                true
            }
            R.id.action_logout -> {
                logout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showInviteDialog() {
        val dialog = InviteUserDialogFragment.newInstance(projectId)
        dialog.show(supportFragmentManager, "InviteUserDialog")
    }

    private fun logout() {
        FirebaseUtils.signOut()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
