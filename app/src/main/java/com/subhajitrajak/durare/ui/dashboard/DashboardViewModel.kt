package com.subhajitrajak.durare.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subhajitrajak.durare.data.models.DashboardStats
import com.subhajitrajak.durare.data.models.User
import com.subhajitrajak.durare.data.repositories.DashboardRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class DashboardViewModel(private val repository: DashboardRepository) : ViewModel() {

    private val _dashboardStats = MutableLiveData<DashboardStats>()
    val dashboardStats: LiveData<DashboardStats> get() = _dashboardStats

    private val _monthlyPushupCounts = MutableLiveData<List<Int>>()
    val monthlyPushupCounts: LiveData<List<Int>> get() = _monthlyPushupCounts

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
            if (_dashboardStats.value == null) _loading.value = true

            repository.fetchDashboardStats()
                .catch { e ->
                    _error.value = e.message
                    _loading.value = false
                }
                .collect { stats ->
                    _dashboardStats.value = stats
                    _loading.value = false
                    _error.value = null
                }
        }
    }

    fun fetchLast30DaysPushupCounts() {
        viewModelScope.launch {
            repository.fetchLast30DaysPushupCounts()
                .catch { e ->
                    // Log error but maybe don't show toast for chart failure to avoid annoyance
                    android.util.Log.e("DashboardVM", "Chart Error: ${e.message}")
                }
                .collect { counts ->
                    _monthlyPushupCounts.value = counts
                }
        }
    }

    fun loadCurrentStreak() {
        viewModelScope.launch {
            repository.fetchStreak()
                .catch { e ->
                    android.util.Log.e("DashboardVM", "Streak Error: ${e.message}")
                }
                .collect { streak ->
                    _currentStreak.value = streak
                }
        }
    }

    fun loadLeaderboard() {
        viewModelScope.launch {
            _loading.value = true
            repository.fetchLeaderboard()
                .catch { e ->
                    _error.value = e.message
                    _loading.value = false
                }
                .collect { users ->
                    _leaderboard.value = users
                    _loading.value = false
                    _error.value = null
                }
        }
    }
}
