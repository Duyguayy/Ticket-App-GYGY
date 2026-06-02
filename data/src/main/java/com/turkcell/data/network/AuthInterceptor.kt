package com.turkcell.data.network

import com.turkcell.data.local.TokenStore
import okhttp3.Interceptor
import okhttp3.Response

// Var olan jwt'i api'e giden isteklere ekle..***
//chain.proceed() İsteği sonraki interceptor'a veya gerçek işleme devam ettir
//Interceptor (Ara Kesici), bir istek veya işlemin çalışmasından önce ya da sonra araya girerek ek işlemler yapmanı sağlayan yapıdır.
class AuthInterceptor(private val tokenStore: TokenStore) : Interceptor
{
    private val authPaths = setOf(
        "/auth/login",
        "/auth/register",
        "/auth/refresh"
    )


    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val path = original.url.encodedPath

        if(path in authPaths) return chain.proceed(original)

        val token = tokenStore.accessTokenBlocking() ?: return chain.proceed(original)

        val authedRequest = original
            .newBuilder() // original isteğin klonunu yaratır..
            .header("Authorization", "Bearer $token")
            .build()
        return chain.proceed(authedRequest)
    }

}