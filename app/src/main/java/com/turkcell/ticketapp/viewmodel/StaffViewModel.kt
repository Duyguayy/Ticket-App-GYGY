package com.turkcell.ticketapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.core.domain.auth.AuthRepository
import kotlinx.coroutines.launch

class StaffViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    // QR tarama sonucunu API'ye gönder
    fun checkin(qrCode: String) {
        // TODO: checkinRepository.scan(qrCode)
    }

    fun logout() {
        viewModelScope.launch { authRepository.logout() }
        // isLoggedIn=false → AppNavHost otomatik Login'e düşer
    }
}
