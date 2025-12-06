package com.subhajitrajak.durare.ui.analytics

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.subhajitrajak.durare.data.models.DailyPushStats
import com.subhajitrajak.durare.data.repositories.AnalyticsRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class AnalyticsViewModel(private val repository: AnalyticsRepository): ViewModel() {
    val modelProducer = CartesianChartModelProducer()

    private val _dailyStats = MutableLiveData<List<DailyPushStats>>()
    val dailyStats: LiveData<List<DailyPushStats>> get() = _dailyStats

    private val _pushupCounts = MutableLiveData<List<Int>>()
    val pushupCounts: LiveData<List<Int>> get() = _pushupCounts

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    fun loadDailyStats() {
        viewModelScope.launch {
            if (_dailyStats.value.isNullOrEmpty()) _loading.value = true

            repository.fetchAllDailyStats()
                .catch { e ->
                    _error.value = e.message
                    _loading.value = false
                }
                .collect { stats ->
                    _dailyStats.value = stats
                    _loading.value = false
                    _error.value = null
                }
        }
    }

    fun loadThisMonthPushups() {
        viewModelScope.launch {
            repository.fetchThisMonthPushupCounts()
                .catch { e ->
                    e.printStackTrace()
                }
                .collect { counts ->
                    _pushupCounts.value = counts

                    // Update the Vico chart model
                    modelProducer.runTransaction {
                        lineSeries {
                            series(
                                x = (0 until counts.size).map { it.toFloat() },
                                y = counts.map { it.toFloat() }
                            )
                        }
                    }
                }
        }
    }
}