package com.turkcell.core.domain.purchase

enum class TicketStatus {
    VALID,
    USED;

    companion object {
        fun fromApi(value: String?): TicketStatus = when (value?.uppercase()) {
            "USED" -> USED
            else -> VALID
        }
    }
}