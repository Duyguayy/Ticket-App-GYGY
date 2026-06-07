package com.turkcell.ticketapp.screen

import android.app.Activity
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.turkcell.core.domain.purchase.TicketStatus
import com.turkcell.ticketapp.R
import com.turkcell.ticketapp.util.generateQrBitmap
import com.turkcell.ticketapp.viewmodel.TicketDetailViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketDetailScreen(
    viewModel: TicketDetailViewModel = koinViewModel(),
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    DisposableEffect(Unit) {
        val window = (context as? Activity)?.window
        val originalBrightness = window?.attributes?.screenBrightness ?: -1f
        window?.attributes = window?.attributes?.also { it.screenBrightness = 1.0f }
        onDispose {
            window?.attributes = window?.attributes?.also { it.screenBrightness = originalBrightness }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.ticket?.ticketTypeName ?: stringResource(R.string.ticket_detail_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            when {
                state.isLoading -> CircularProgressIndicator()
                state.error != null -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(state.error!!, color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(onClick = viewModel::loadTicket) {
                            Text(stringResource(R.string.retry))
                        }
                    }
                }
                state.ticket != null -> {
                    val ticket = state.ticket!!
                    QrContent(
                        qrCode = ticket.qrCode,
                        eventName = ticket.eventName,
                        eventVenue = ticket.eventVenue,
                        eventStartsAt = ticket.eventStartsAt,
                        ticketTypeName = ticket.ticketTypeName,
                        isValid = ticket.status == TicketStatus.VALID,
                    )
                }
            }
        }
    }
}

@Composable
private fun QrContent(
    qrCode: String,
    eventName: String,
    eventVenue: String,
    eventStartsAt: String,
    ticketTypeName: String,
    isValid: Boolean,
) {
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var qrError by remember { mutableStateOf(false) }
    var isGenerating by remember { mutableStateOf(true) }

    LaunchedEffect(qrCode) {
        isGenerating = true
        qrError = false
        qrBitmap = null

        if (qrCode.isBlank()) {
            qrError = true
            isGenerating = false
            return@LaunchedEffect
        }

        val bmp = withContext(Dispatchers.Default) {
            generateQrBitmap(qrCode, 600)
        }

        if (bmp != null) {
            qrBitmap = bmp
        } else {
            qrError = true
        }
        isGenerating = false
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
    ) {
        Text(eventName.ifBlank { "—" }, style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(4.dp))
        if (eventVenue.isNotBlank()) {
            Text(
                text = eventVenue,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(4.dp))
        }
        if (eventStartsAt.isNotBlank()) {
            Text(
                text = eventStartsAt,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(24.dp))

        when {
            isGenerating -> {
                Box(modifier = Modifier.size(280.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            qrBitmap != null -> {
                Box(
                    modifier = Modifier
                        .size(280.dp)
                        .background(Color.White)
                        .padding(8.dp)
                ) {
                    Image(
                        bitmap = qrBitmap!!.asImageBitmap(),
                        contentDescription = stringResource(R.string.qr_code_desc),
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
            else -> {
                // QR üretilemedi — qrCode değerini göster
                Box(
                    modifier = Modifier
                        .size(280.dp)
                        .background(Color.White)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (qrCode.isBlank()) "QR kodu boş geldi" else "QR hatası:\n$qrCode",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Black,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Text(ticketTypeName.ifBlank { "—" }, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(4.dp))
        Text(
            text = if (isValid) stringResource(R.string.ticket_status_valid)
            else stringResource(R.string.ticket_status_used),
            style = MaterialTheme.typography.bodyMedium,
            color = if (isValid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
        )
    }
}