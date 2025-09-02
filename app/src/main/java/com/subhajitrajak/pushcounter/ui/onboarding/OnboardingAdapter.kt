package com.subhajitrajak.pushcounter.ui.onboarding

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.subhajitrajak.pushcounter.databinding.ItemOnboardingScreenBinding

class OnboardingAdapter(
    private val screens: List<OnboardingScreen>
) : RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder>() {

    inner class OnboardingViewHolder(
        private val binding: ItemOnboardingScreenBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(screen: OnboardingScreen) {
            binding.onboardingTitle.text = screen.title
            binding.onboardingBody.text = screen.body
            binding.onboardingImage.setImageResource(screen.imageResId)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnboardingViewHolder {
        val binding = ItemOnboardingScreenBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return OnboardingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OnboardingViewHolder, position: Int) {
        holder.bind(screens[position])
    }

    override fun getItemCount(): Int = screens.size
}
