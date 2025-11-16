package com.subhajitrajak.durare.ui.weightSetup

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.subhajitrajak.durare.data.repositories.WeightSetupRepository

class WeightSetupViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WeightSetupViewModel::class.java)) {
            val repository = WeightSetupRepository(context)
            @Suppress("UNCHECKED_CAST")
            return WeightSetupViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}