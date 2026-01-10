package com.qrware.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.qrware.app.data.repository.ZoneRepository

class AddZoneViewModelFactory(
    private val zoneRepository: ZoneRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddZoneViewModel::class.java)) {
            return AddZoneViewModel(zoneRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

class EditZoneViewModelFactory(
    private val zoneRepository: ZoneRepository,
    private val zoneId: Long
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditZoneViewModel::class.java)) {
            return EditZoneViewModel(zoneRepository, zoneId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}