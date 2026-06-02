package com.turkcell.ticketapp.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.turkcell.core.domain.event.Event
import com.turkcell.core.domain.event.EventRepository
import com.turkcell.core.domain.purchase.PurchaseItemRequest
import com.turkcell.core.domain.purchase.PurchaseRepository
import com.turkcell.ticketapp.navigation.EventDetail
import com.turkcell.ticketapp.util.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EventDetailUiState(
    val isLoading: Boolean = false,
    val event: Event? = null,
    val error: String? = null,
    // ticketTypeId -> quantity
    val quantities: Map<String, Int> = emptyMap(),
    val isPurchasing: Boolean = false,
    val purchaseError: String? = null,
    // null = yok, non-null = ödeme onayı diyaloğu göster
    val pendingPurchaseId: String? = null,
    val isPaying: Boolean = false,
    val navigateToTickets: Boolean = false,
) {
    val totalCents: Long
        get() {
            val event = event ?: return 0L
            return event.ticketTypes.sumOf { tt ->
                tt.priceCents * (quantities[tt.id] ?: 0)
            }
        }
    val canBuy: Boolean get() = totalCents > 0 && !isPurchasing
}

class EventDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val eventRepository: EventRepository,
    private val purchaseRepository: PurchaseRepository,
) : ViewModel() {

    private val eventId: String = savedStateHandle.toRoute<EventDetail>().id

    private val _state = MutableStateFlow(EventDetailUiState())
    val state: StateFlow<EventDetailUiState> = _state.asStateFlow()

    init {
        loadEvent()
    }

    fun loadEvent() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            eventRepository.getEvent(eventId).fold(
                onSuccess = { event ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            event = event,
                            quantities = event.ticketTypes.associate { tt -> tt.id to 0 },
                        )
                    }
                },
                onFailure = { e ->
                    _state.update { it.copy(isLoading = false, error = e.message ?: "Etkinlik yüklenemedi") }
                }
            )
        }
    }

    fun increment(ticketTypeId: String) {
        val event = _state.value.event ?: return
        val tt = event.ticketTypes.find { it.id == ticketTypeId } ?: return
        val current = _state.value.quantities[ticketTypeId] ?: 0
        val max = minOf(20, tt.remaining.toInt())
        if (current < max) {
            _state.update { it.copy(quantities = it.quantities + (ticketTypeId to current + 1)) }
        }
    }

    fun decrement(ticketTypeId: String) {
        val current = _state.value.quantities[ticketTypeId] ?: 0
        if (current > 0) {
            _state.update { it.copy(quantities = it.quantities + (ticketTypeId to current - 1)) }
        }
    }

    fun startPurchase() {
        if (!_state.value.canBuy) return
        val items = _state.value.quantities
            .filter { it.value > 0 }
            .map { PurchaseItemRequest(ticketTypeId = it.key, quantity = it.value) }

        _state.update { it.copy(isPurchasing = true, purchaseError = null) }
        viewModelScope.launch {
            purchaseRepository.createPurchase(items).fold(
                onSuccess = { purchase ->
                    _state.update { it.copy(isPurchasing = false, pendingPurchaseId = purchase.id) }
                },
                onFailure = { e ->
                    val msg = e.toUserMessage()
                    if (msg.contains("stok", ignoreCase = true)) {
                        loadEvent() // remaining'i güncelle
                    }
                    _state.update { it.copy(isPurchasing = false, purchaseError = msg) }
                }
            )
        }
    }

    fun confirmPayment() {
        val purchaseId = _state.value.pendingPurchaseId ?: return
        _state.update { it.copy(isPaying = true, pendingPurchaseId = null) }
        viewModelScope.launch {
            purchaseRepository.pay(purchaseId).fold(
                onSuccess = { _state.update { it.copy(isPaying = false, navigateToTickets = true) } },
                onFailure = { e ->
                    _state.update { it.copy(isPaying = false, purchaseError = e.toUserMessage()) }
                }
            )
        }
    }

    fun dismissPaymentDialog() {
        _state.update { it.copy(pendingPurchaseId = null) }
    }

    fun consumePurchaseError() {
        _state.update { it.copy(purchaseError = null) }
    }

    fun onNavigatedToTickets() {
        _state.update { it.copy(navigateToTickets = false) }
    }
}
