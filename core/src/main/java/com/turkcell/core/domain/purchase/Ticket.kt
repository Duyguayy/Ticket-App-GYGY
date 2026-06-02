package com.turkcell.core.domain.purchase

data class Ticket(
    val id: String,
    val qrCode: String,
    val status: TicketStatus,
    val usedAt: String?,
    val ticketTypeId: String,
    val ticketTypeName: String,
    val priceCents: Long,
    val eventId: String,
    val eventName: String,
    val eventVenue: String,
    val eventStartsAt: String,
)
