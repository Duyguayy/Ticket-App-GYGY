package com.turkcell.ticketapp.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.core.domain.event.Event
import com.turkcell.ticketapp.viewmodel.HomeViewModel
import com.turkcell.ticketapp.viewmodel.MyTicket
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Surface(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 24.dp),
        ) {
            // ── Etkinlikler Bölümü ──────────────────────────────────────
            item {
                SectionHeader(
                    title = "Yaklaşan Etkinlikler",
                    onRetry = if (state.eventsError != null) viewModel::loadEvents else null,
                )
                Spacer(Modifier.height(8.dp))
                EventsRow(
                    isLoading = state.isEventsLoading,
                    error = state.eventsError,
                    events = state.events,
                )
                Spacer(Modifier.height(24.dp))
            }

            // ── Biletlerim Bölümü ───────────────────────────────────────
            item {
                SectionHeader(
                    title = "Biletlerim",
                    onRetry = if (state.ticketsError != null) viewModel::loadTickets else null,
                )
                Spacer(Modifier.height(8.dp))
            }

            when {
                state.isTicketsLoading -> item {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(80.dp),
                        contentAlignment = Alignment.Center,
                    ) { CircularProgressIndicator() }
                }
                state.ticketsError != null -> item {
                    Text(
                        text = state.ticketsError!!,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 24.dp),
                    )
                }
                state.tickets.isEmpty() -> item {
                    Text(
                        text = "Henüz biletiniz yok.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 24.dp),
                    )
                }
                else -> items(items = state.tickets, key = { it.id }) { ticket ->
                    TicketItem(ticket = ticket)
                }
            }
        }
    }
}

// ── Section Header ───────────────────────────────────────────────────────────

@Composable
private fun SectionHeader(title: String, onRetry: (() -> Unit)?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        if (onRetry != null) {
            TextButton(onClick = onRetry) { Text("Yenile") }
        }
    }
}

// ── Etkinlikler ──────────────────────────────────────────────────────────────

@Composable
private fun EventsRow(
    isLoading: Boolean,
    error: String?,
    events: List<Event>,
) {
    when {
        isLoading -> Box(
            modifier = Modifier.fillMaxWidth().height(220.dp),
            contentAlignment = Alignment.Center,
        ) { CircularProgressIndicator() }

        error != null -> Text(
            text = error,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(horizontal = 24.dp),
        )

        events.isEmpty() -> Text(
            text = "Şimdilik hiç bir etkinlik yok.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 24.dp),
        )

        else -> LazyRow(contentPadding = PaddingValues(horizontal = 24.dp)) {
            items(items = events, key = { it.id }) { event -> EventCard(event) }
        }
    }
}

@Composable
private fun EventCard(event: Event) {
    Card(
        modifier = Modifier
            .width(260.dp)
            .height(280.dp)
            .padding(end = 12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = event.name.take(1).uppercase().ifBlank { "?" },
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = event.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = event.venue,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = event.startsAt,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    maxLines = 1,
                )
            }
        }
    }
}

// ── Biletlerim ───────────────────────────────────────────────────────────────

@Composable
private fun TicketItem(ticket: MyTicket) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 6.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (ticket.status == "VALID")
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (ticket.status == "VALID") "✓" else "✗",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (ticket.status == "VALID")
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = "Bilet #${ticket.id.take(8)}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = ticket.status,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (ticket.status == "VALID")
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.outline,
                )
            }
        }
    }
}