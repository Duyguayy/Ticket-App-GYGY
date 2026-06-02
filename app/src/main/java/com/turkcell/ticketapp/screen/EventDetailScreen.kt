package com.turkcell.ticketapp.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.core.domain.event.TicketType
import com.turkcell.ticketapp.R
import com.turkcell.ticketapp.viewmodel.EventDetailViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    viewModel: EventDetailViewModel = koinViewModel(),
    onBack: () -> Unit,
    onNavigateToTickets: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.navigateToTickets) {
        if (state.navigateToTickets) {
            viewModel.onNavigatedToTickets()
            onNavigateToTickets()
        }
    }

    LaunchedEffect(state.purchaseError) {
        state.purchaseError?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumePurchaseError()
        }
    }

    // Ödeme onayı diyaloğu
    if (state.pendingPurchaseId != null) {
        AlertDialog(
            onDismissRequest = viewModel::dismissPaymentDialog,
            title = { Text(stringResource(R.string.payment_confirm_title)) },
            text = {
                Text(
                    stringResource(
                        R.string.payment_confirm_message,
                        "₺%.2f".format(state.totalCents / 100.0)
                    )
                )
            },
            confirmButton = {
                Button(onClick = viewModel::confirmPayment) {
                    Text(stringResource(R.string.payment_confirm_button))
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissPaymentDialog) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.event?.name ?: stringResource(R.string.event_detail_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (state.event != null) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Divider()
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(R.string.total_price, "₺%.2f".format(state.totalCents / 100.0)),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Button(
                            onClick = viewModel::startPurchase,
                            enabled = state.canBuy,
                        ) {
                            if (state.isPurchasing || state.isPaying) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            } else {
                                Text(stringResource(R.string.buy_button))
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when {
                state.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                state.error != null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(state.error!!, color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(onClick = viewModel::loadEvent) {
                            Text(stringResource(R.string.retry))
                        }
                    }
                }
                state.event != null -> {
                    val event = state.event!!
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        Text(event.name, style = MaterialTheme.typography.headlineMedium)
                        Spacer(Modifier.height(4.dp))
                        Text(event.venue, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(4.dp))
                        Text(event.startsAt, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(12.dp))
                        Text(event.description, style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(24.dp))
                        Text(stringResource(R.string.ticket_types_title), style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        event.ticketTypes.forEach { tt ->
                            TicketTypeRow(
                                ticketType = tt,
                                quantity = state.quantities[tt.id] ?: 0,
                                onIncrement = { viewModel.increment(tt.id) },
                                onDecrement = { viewModel.decrement(tt.id) },
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                        // Bottom bar yüksekliği kadar boşluk
                        Spacer(Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun TicketTypeRow(
    ticketType: TicketType,
    quantity: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(ticketType.name, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = "${ticketType.remaining}/${ticketType.capacity} kalan · ₺%.2f".format(ticketType.priceCents / 100.0),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        FilledTonalIconButton(
            onClick = onDecrement,
            enabled = quantity > 0,
        ) {
            Text("−", style = MaterialTheme.typography.titleLarge)
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = quantity.toString(),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.width(28.dp),
        )
        Spacer(Modifier.width(8.dp))
        FilledTonalIconButton(
            onClick = onIncrement,
            enabled = quantity < minOf(20, ticketType.remaining.toInt()),
        ) {
            Text("+", style = MaterialTheme.typography.titleLarge)
        }
    }
}
