package com.subhajitrajak.durare.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subhajitrajak.durare.data.models.DashboardStats
import com.subhajitrajak.durare.data.models.User
import com.subhajitrajak.durare.data.repositories.DashboardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class DashboardViewModel(private val repository: DashboardRepository) : ViewModel() {

    private val _dashboardStats = MutableStateFlow<DashboardStats?>(null)
    val dashboardStats: StateFlow<DashboardStats?> = _dashboardStats.asStateFlow()

    private val _monthlyPushupCounts = MutableStateFlow<List<Int>>(emptyList())
    val monthlyPushupCounts: StateFlow<List<Int>> = _monthlyPushupCounts.asStateFlow()

    private val _currentStreak = MutableStateFlow<Pair<Int, Int>>(0 to 0)
    val currentStreak: StateFlow<Pair<Int, Int>> = _currentStreak.asStateFlow()

    private val _leaderboard = MutableStateFlow<List<User>>(emptyList())
    val leaderboard: StateFlow<List<User>> = _leaderboard.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

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

    fun loadAll() {
        loadDashboardStats()
        fetchLast30DaysPushupCounts()
        loadCurrentStreak()
        loadLeaderboard()
    }
}
