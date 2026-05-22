package com.turkcell.core.domain.event

data class MyTicket(
    val id: String,
    val qrCode: String,
    val status: String,         // "VALID", "USED" vb.
    val ticketTypeId: String,
)