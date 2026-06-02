package com.turkcell.ticketapp.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.turkcell.core.domain.purchase.PurchaseRepository
import com.turkcell.core.domain.purchase.Ticket
import com.turkcell.ticketapp.navigation.TicketDetail
import com.turkcell.ticketapp.util.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TicketDetailUiState(
    val isLoading: Boolean = false,
    val ticket: Ticket? = null,
    val error: String? = null,
)

class TicketDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val purchaseRepository: PurchaseRepository,
) : ViewModel() {

    private val ticketId: String = savedStateHandle.toRoute<TicketDetail>().id

    private val _state = MutableStateFlow(TicketDetailUiState())
    val state: StateFlow<TicketDetailUiState> = _state.asStateFlow()

    init {
        loadTicket()
    }

    fun loadTicket() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            purchaseRepository.getTicket(ticketId).fold(
                onSuccess = { ticket -> _state.update { it.copy(isLoading = false, ticket = ticket) } },
                onFailure = { e -> _state.update { it.copy(isLoading = false, error = e.toUserMessage()) } }
            )
        }
    }
}
