package com.turkcell.data.repository

import com.turkcell.core.domain.auth.AuthRepository
import com.turkcell.core.domain.auth.AuthSession
import com.turkcell.core.domain.auth.User
import com.turkcell.core.domain.auth.UserRole
import com.turkcell.data.dto.auth.CredentialsDto
import com.turkcell.data.dto.auth.RefreshRequestDto
import com.turkcell.data.local.TokenStore
import com.turkcell.data.remote.AuthApi
import com.turkcell.data.util.runCatchingApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class AuthRepositoryImpl(
    private val authApi: AuthApi,
    private val tokenStore: TokenStore,
) : AuthRepository {

    // Login/register sonrası ve restoreSession ile oturum bilgisini bellekte tutuyoruz
    private val _currentSession = MutableStateFlow<AuthSession?>(null)
    override val currentSession: Flow<AuthSession?> = _currentSession.asStateFlow()

    override val isLoggedIn: Flow<Boolean> = tokenStore.accessToken.map { it != null }

    override suspend fun restoreSession() {
        val accessToken = tokenStore.accessToken.first() ?: return
        val refreshToken = tokenStore.refreshToken.first() ?: return

        if (_currentSession.value != null) return // Zaten yüklü


        _currentSession.value = AuthSession(
            user = User(id = "", email = "", role = UserRole.USER),
            accessToken = accessToken,
            refreshToken = refreshToken,
        )
    }

    override suspend fun login(email: String, password: String): Result<AuthSession> =
        runCatchingApi {
            authApi.login(CredentialsDto(email = email, password = password))
        }.onSuccess {
            tokenStore.save(it.accessToken, it.refreshToken)
        }.map { dto ->
            AuthSession(
                user = User(dto.user.id, dto.user.email, UserRole.fromApi(dto.user.role)),
                accessToken = dto.accessToken,
                refreshToken = dto.refreshToken,
            ).also { session -> _currentSession.value = session }
        }

    override suspend fun register(email: String, password: String): Result<AuthSession> =
        runCatchingApi {
            authApi.register(CredentialsDto(email = email, password = password))
        }.onSuccess {
            tokenStore.save(it.accessToken, it.refreshToken)
        }.map { dto ->
            AuthSession(
                user = User(dto.user.id, dto.user.email, UserRole.fromApi(dto.user.role)),
                accessToken = dto.accessToken,
                refreshToken = dto.refreshToken,
            ).also { session -> _currentSession.value = session }
        }

    override suspend fun logout(): Result<Unit> = runCatchingApi {
        val refresh = tokenStore.refreshTokenBlocking()
        if (refresh != null) {
            runCatching { authApi.logout(RefreshRequestDto(refresh)) }
        }
        tokenStore.clear()
        _currentSession.value = null
    }
}