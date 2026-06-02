package com.turkcell.core.domain.purchase

data class PurchaseItemRequest(
    val ticketTypeId: String,
    val quantity: Int,
)
