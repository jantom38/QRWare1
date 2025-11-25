package com.qrware.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.qrware.app.data.repository.MovementHistoryRepository

class MovementHistoryViewModelFactory(
    private val movementHistoryRepository: MovementHistoryRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MovementHistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MovementHistoryViewModel(movementHistoryRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}