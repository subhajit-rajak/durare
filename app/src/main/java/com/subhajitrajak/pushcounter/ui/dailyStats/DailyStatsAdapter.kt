package com.subhajitrajak.pushcounter.ui.dailyStats

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.subhajitrajak.pushcounter.data.models.DailyPushStats
import com.subhajitrajak.pushcounter.databinding.ItemRecordsBinding

class DailyStatsAdapter(
    private val navigateToShareStats: (DailyPushStats) -> Unit
) : ListAdapter<DailyPushStats, DailyStatsAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(val binding: ItemRecordsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(model: DailyPushStats) {
            binding.apply {
                val timeMinutes = model.totalActiveTimeMs / 1000 / 60
                val timeSeconds = (model.totalActiveTimeMs / 1000) % 60
                val time = if (timeMinutes > 0) {
                    "${timeMinutes}m ${timeSeconds}s"
                } else {
                    "${timeSeconds}s"
                }

                dateTextView.text = model.date
                timeTextView.text = time
                countTextView.text = model.totalPushups.toString()

                root.setOnClickListener {
                    navigateToShareStats(model)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemRecordsBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<DailyPushStats>() {
            override fun areItemsTheSame(oldItem: DailyPushStats, newItem: DailyPushStats): Boolean {
                return oldItem.date == newItem.date
            }

            override fun areContentsTheSame(oldItem: DailyPushStats, newItem: DailyPushStats): Boolean {
                return oldItem == newItem
            }
        }
    }
}