package com.subhajitrajak.pushcounter.ui.dashboard

import androidx.lifecycle.*
import com.subhajitrajak.pushcounter.data.models.DailyPushStats
import com.subhajitrajak.pushcounter.data.models.DashboardStats
import com.subhajitrajak.pushcounter.data.repositories.DashboardRepository
import com.subhajitrajak.pushcounter.utils.Event
import kotlinx.coroutines.launch

class DashboardViewModel(private val repository: DashboardRepository) : ViewModel() {

    private val _dashboardStats = MutableLiveData<DashboardStats>()
    val dashboardStats: LiveData<DashboardStats> get() = _dashboardStats

    private val _dailyStats = MutableLiveData<Event<DailyPushStats>>()
    val dailyStats: LiveData<Event<DailyPushStats>> get() = _dailyStats

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    fun loadDashboardStats() {
        viewModelScope.launch {
            _loading.value = true
            try {
                val stats = repository.fetchDashboardStats()
                _dashboardStats.value = stats
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun loadDailyStats() {
        viewModelScope.launch {
            try {
                val stats = repository.fetchDailyStats()
                _dailyStats.value = Event(stats)
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
}
