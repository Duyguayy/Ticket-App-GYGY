package com.turkcell.core.util

// LoginViewModel'deki toUserMessage kalıbı buraya taşındı ve genişletildi.
// Tüm modüller bu fonksiyonu kullanır; data katmanına bağımlılık yoktur.

fun apiErrorMessage(httpCode: Int, apiErrorCode: String?): String = when {
    httpCode == 401 && apiErrorCode == "invalid_credentials" -> "Email veya şifre hatalı"
    httpCode == 401 -> "Oturum süreniz doldu, lütfen tekrar giriş yapın"
    httpCode == 403 && apiErrorCode == "not_purchase_owner" -> "Bu satın alım size ait değil"
    httpCode == 409 && apiErrorCode == "email_taken" -> "Bu email zaten kayıtlı"
    httpCode == 409 && apiErrorCode == "capacity_exceeded" -> "Seçilen bilet türünde yeterli stok yok"
    httpCode == 409 && apiErrorCode == "already_paid" -> "Bu satın alım zaten ödenmiş"
    httpCode == 409 && apiErrorCode == "already_used" -> "Bu bilet daha önce kullanılmış"
    httpCode in 500..599 -> "Sunucu şu anda cevap veremiyor, lütfen daha sonra deneyin"
    else -> "Beklenmeyen bir hata oluştu (HTTP $httpCode)"
}
