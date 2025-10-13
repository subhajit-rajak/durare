package com.subhajitrajak.durare.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subhajitrajak.durare.data.models.DashboardStats
import com.subhajitrajak.durare.data.models.User
import com.subhajitrajak.durare.data.repositories.DashboardRepository
import kotlinx.coroutines.launch

class DashboardViewModel(private val repository: DashboardRepository) : ViewModel() {

    private val _dashboardStats = MutableLiveData<DashboardStats>()
    val dashboardStats: LiveData<DashboardStats> get() = _dashboardStats

    private val _thisMonthPushupCounts = MutableLiveData<List<Int>>()
    val thisMonthPushupCounts: LiveData<List<Int>> get() = _thisMonthPushupCounts

    private val _currentStreak = MutableLiveData<Pair<Int, Int>>()
    val currentStreak: LiveData<Pair<Int, Int>> get() = _currentStreak

    private val _leaderboard = MutableLiveData<List<User>>()
    val leaderboard: LiveData<List<User>> get() = _leaderboard

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

    fun loadThisMonthPushupCounts() {
        viewModelScope.launch {
            try {
                val counts = repository.fetchThisMonthPushupCounts()
                _thisMonthPushupCounts.value = counts
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun loadCurrentStreak() {
        viewModelScope.launch {
            try {
                val streak = repository.fetchStreak()
                _currentStreak.value = streak
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun loadLeaderboard() {
        viewModelScope.launch {
            try {
                val users = repository.fetchLeaderboard()
                _leaderboard.value = users
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
}
