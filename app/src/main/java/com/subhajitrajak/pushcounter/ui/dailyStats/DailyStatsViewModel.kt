package com.subhajitrajak.pushcounter.ui.dailyStats

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subhajitrajak.pushcounter.data.models.DailyPushStats
import com.subhajitrajak.pushcounter.data.repositories.DailyStatsRepository
import kotlinx.coroutines.launch

class DailyStatsViewModel(private val repository: DailyStatsRepository): ViewModel() {
    private val _dailyStats = MutableLiveData<List<DailyPushStats>>()
    val dailyStats: LiveData<List<DailyPushStats>> get() = _dailyStats

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    fun loadDailyStats() {
        viewModelScope.launch {
            _loading.value = true
            try {
                val stats = repository.fetchAllDailyStats()
                _dailyStats.value = stats
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }
}