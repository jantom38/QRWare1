package com.qrware.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.qrware.app.data.repository.QRCodeRepository

class QRCodeViewModelFactory(
    private val qrCodeRepository: QRCodeRepository
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(QRCodeViewModel::class.java)) {
            return QRCodeViewModel(qrCodeRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}