package com.subhajitrajak.durare.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.subhajitrajak.durare.data.repositories.AnalyticsRepository

class AnalyticsViewModelFactory() : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AnalyticsViewModel::class.java)) {
            val repository = AnalyticsRepository()
            @Suppress("UNCHECKED_CAST")
            return AnalyticsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}