package com.turkcell.data.dto.event

import kotlinx.serialization.Serializable

@Serializable
data class MyTicketDto(
    val id: String,
    val qrCode: String,
    val status: String,
    val ticketTypeId: String,
)