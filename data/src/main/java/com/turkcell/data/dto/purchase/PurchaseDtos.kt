package com.turkcell.data.dto.purchase

import kotlinx.serialization.Serializable

@Serializable
data class CreatePurchaseRequestDto(
    val items: List<PurchaseItemRequestDto>,
)

@Serializable
data class PurchaseItemRequestDto(
    val ticketTypeId: String,
    val quantity: Int,
)

@Serializable
data class PurchaseDto(
    val id: String,
    val userId: String,
    val status: String,
    val totalCents: Long,
    val createdAt: String,
    val paidAt: String? = null,
    val items: List<PurchaseItemDto> = emptyList(),
    val tickets: List<TicketDto> = emptyList(),
)

@Serializable
data class PurchaseItemDto(
    val id: String,
    val ticketTypeId: String,
    val quantity: Int,
    val unitPriceCents: Long,
)

@Serializable
data class TicketDto(
    val id: String,
    val qrCode: String,
    val status: String,
    val usedAt: String? = null,
    val checkedInBy: String? = null,
    val ticketType: TicketTypeDetailDto? = null,
)

@Serializable
data class TicketTypeDetailDto(
    val id: String,
    val name: String,
    val priceCents: Long,
    val event: EventSummaryDto? = null,
)

@Serializable
data class EventSummaryDto(
    val id: String,
    val name: String,
    val venue: String,
    val startsAt: String,
)
