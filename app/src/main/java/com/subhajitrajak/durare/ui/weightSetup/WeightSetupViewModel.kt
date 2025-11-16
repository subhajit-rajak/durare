package com.subhajitrajak.durare.ui.weightSetup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.subhajitrajak.durare.data.repositories.WeightSetupRepository
import kotlinx.coroutines.launch

class WeightSetupViewModel(
    val repository: WeightSetupRepository
) : ViewModel() {

    private val _isDataSaved = MutableLiveData<Boolean>()
    val isDataSaved: LiveData<Boolean> = _isDataSaved

    fun saveWeightData(value: Double) {
        viewModelScope.launch {
            repository.saveData(value)
        }
    }

    fun checkSavedData() {
        viewModelScope.launch {
            _isDataSaved.value = repository.isDataSaved()
        }
    }
}