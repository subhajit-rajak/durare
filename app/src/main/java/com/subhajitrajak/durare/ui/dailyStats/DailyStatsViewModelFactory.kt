package com.subhajitrajak.durare.ui.dailyStats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.subhajitrajak.durare.data.repositories.DailyStatsRepository

class DailyStatsViewModelFactory() : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DailyStatsViewModel::class.java)) {
            val repository = DailyStatsRepository()
            @Suppress("UNCHECKED_CAST")
            return DailyStatsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}