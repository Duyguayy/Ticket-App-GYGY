package com.turkcell.data.remote

import com.turkcell.data.dto.event.MyTicketDto
import retrofit2.http.GET
import retrofit2.http.Path

interface MeApi {
    @GET("/me/tickets")
    suspend fun getMyTickets(): List<MyTicketDto>

    @GET("/me/tickets/{id}")
    suspend fun getTicketById(@Path("id") id: String): MyTicketDto
}