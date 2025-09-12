package com.subhajitrajak.pushcounter.ui.leaderboard

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.subhajitrajak.pushcounter.R
import com.subhajitrajak.pushcounter.data.models.User
import com.subhajitrajak.pushcounter.databinding.ItemLeaderboardBinding

class LeaderboardAdapter(
    private val context: Context
) : ListAdapter<User, LeaderboardAdapter.CategoryViewHolder>(DiffCallback) {

    class CategoryViewHolder(val binding: ItemLeaderboardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(model: User, context: Context, position: Int) {
            binding.apply {
                rankTextView.text = (position + 1).toString()
                userNameTextView.text = model.userData.username
                countTextView.text = model.pushups.toString()

                Glide.with(context)
                    .load(model.userData.profilePictureUrl)
                    .into(userImageView)

                val uid = FirebaseAuth.getInstance().currentUser?.uid
                if (model.userData.userId == uid) {
                    root.setCardBackgroundColor(context.getColorStateList(R.color.very_light_grey))
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        return CategoryViewHolder(
            ItemLeaderboardBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(getItem(position), context, position)
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<User>() {
            override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
                return oldItem.userData.userId == newItem.userData.userId
            }

            override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
                return oldItem == newItem
            }
        }
    }
}