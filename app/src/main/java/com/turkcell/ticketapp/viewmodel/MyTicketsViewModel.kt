package com.turkcell.ticketapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turkcell.core.domain.purchase.PurchaseRepository
import com.turkcell.core.domain.purchase.Ticket
import com.turkcell.ticketapp.util.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MyTicketsUiState(
    val isLoading: Boolean = false,
    val tickets: List<Ticket> = emptyList(),
    val error: String? = null,
)

class MyTicketsViewModel(
    private val purchaseRepository: PurchaseRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(MyTicketsUiState())
    val state: StateFlow<MyTicketsUiState> = _state.asStateFlow()

    init {
        loadTickets()
    }

    fun loadTickets() {
        if (_state.value.isLoading) return
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            purchaseRepository.getMyTickets().fold(
                onSuccess = { tickets -> _state.update { it.copy(isLoading = false, tickets = tickets) } },
                onFailure = { e -> _state.update { it.copy(isLoading = false, error = e.toUserMessage()) } }
            )
        }
    }
}
