package com.turkcell.data.mapper

import com.turkcell.core.domain.event.Event
import com.turkcell.core.domain.event.MyTicket
import com.turkcell.core.domain.event.TicketType
import com.turkcell.data.dto.event.EventDto
import com.turkcell.data.dto.event.MyTicketDto
import com.turkcell.data.dto.event.TicketTypeDto

internal fun EventDto.toDomain(): Event = Event(
    id = id,
    name = name,
    description = description.orEmpty(),
    venue = place.orEmpty(),
    startsAt = startsAt.orEmpty(),
    endsAt = endsAt.orEmpty(),
    ticketTypes = ticketTypes.map { it.toDomain() },
)

internal fun TicketTypeDto.toDomain(): TicketType = TicketType(
    id = id,
    name = name,
    priceCents = priceCents,
    capacity = capacity,
    soldCount = soldCount,
    remaining = remaining,
)

internal fun MyTicketDto.toDomain(): MyTicket = MyTicket(
    id = id,
    qrCode = qrCode,
    status = status,
    ticketTypeId = ticketTypeId,
)