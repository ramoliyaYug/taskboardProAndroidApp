package com.example.projectcollaboration.activities

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnticipateOvershootInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import com.example.projectcollaboration.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val splashDuration = 2500L // 2.5 seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Start animations
        startAnimations()

        // Navigate to login screen after delay
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, LoginActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }, splashDuration)
    }

    private fun startAnimations() {
        // Initially hide elements
        binding.ivLogo.alpha = 0f
        binding.tvAppName.alpha = 0f
        binding.tvTagline.alpha = 0f
        binding.progressBar.alpha = 0f
        binding.ivLogo.scaleX = 0.6f
        binding.ivLogo.scaleY = 0.6f

        // Create animations
        val logoScale = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(binding.ivLogo, "scaleX", 0.6f, 1f),
                ObjectAnimator.ofFloat(binding.ivLogo, "scaleY", 0.6f, 1f)
            )
            duration = 800
            interpolator = AnticipateOvershootInterpolator()
        }

        val logoFade = ObjectAnimator.ofFloat(binding.ivLogo, "alpha", 0f, 1f).apply {
            duration = 600
            interpolator = DecelerateInterpolator()
        }

        val titleFade = ObjectAnimator.ofFloat(binding.tvAppName, "alpha", 0f, 1f).apply {
            duration = 800
            startDelay = 300
            interpolator = DecelerateInterpolator()
        }

        val taglineFade = ObjectAnimator.ofFloat(binding.tvTagline, "alpha", 0f, 1f).apply {
            duration = 800
            startDelay = 500
            interpolator = DecelerateInterpolator()
        }

        val progressFade = ObjectAnimator.ofFloat(binding.progressBar, "alpha", 0f, 1f).apply {
            duration = 600
            startDelay = 800
            interpolator = AccelerateDecelerateInterpolator()
        }

        // Start animations
        AnimatorSet().apply {
            playTogether(logoScale, logoFade, titleFade, taglineFade, progressFade)
            start()
        }

        // Animate the background gradient
        animateBackground()
    }

    private fun animateBackground() {
        val animator = ObjectAnimator.ofFloat(binding.backgroundGradient, "rotation", 0f, 360f)
        animator.duration = 30000 // 30 seconds for a full rotation
        animator.repeatCount = ObjectAnimator.INFINITE
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.start()
    }
}
