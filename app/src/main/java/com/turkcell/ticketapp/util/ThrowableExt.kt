package com.turkcell.ticketapp.util

import com.turkcell.core.util.apiErrorMessage
import com.turkcell.data.network.ApiException
import com.turkcell.data.network.NetworkException

fun Throwable.toUserMessage(): String = when (this) {
    is ApiException -> apiErrorMessage(code, errorMessage)
    is NetworkException -> "İnternet bağlantısı yok"
    else -> message ?: "Bilinmeyen bir hata oluştu"
}
