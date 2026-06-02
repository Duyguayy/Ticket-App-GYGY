package com.turkcell.core.domain.purchase

interface PurchaseRepository {
    suspend fun createPurchase(items: List<PurchaseItemRequest>): Result<Purchase>
    suspend fun pay(purchaseId: String): Result<Purchase>
    suspend fun getPurchase(purchaseId: String): Result<Purchase>
    suspend fun getMyTickets(): Result<List<Ticket>>
    suspend fun getTicket(ticketId: String): Result<Ticket>
}
