package com.subhajitrajak.durare.ui.settings.general.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.subhajitrajak.durare.data.repositories.AccountRepository

class AccountViewModelFactory(
    private val repository: AccountRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AccountViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AccountViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}