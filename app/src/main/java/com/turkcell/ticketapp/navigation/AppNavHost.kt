package com.turkcell.ticketapp.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.turkcell.core.domain.auth.AuthRepository
import com.turkcell.core.domain.auth.UserRole
import com.turkcell.ticketapp.screen.EventDetailScreen
import com.turkcell.ticketapp.screen.HomeScreen
import com.turkcell.ticketapp.screen.LoginScreen
import com.turkcell.ticketapp.screen.MyTicketsScreen
import com.turkcell.ticketapp.screen.RegisterScreen
import com.turkcell.ticketapp.screen.StaffScreen
import com.turkcell.ticketapp.screen.TicketDetailScreen
import org.koin.compose.koinInject

@Composable
fun AppNavHost(
    authRepository: AuthRepository = koinInject(),
) {
    // null  → henüz DataStore'dan okuma bitmedi (splash göster)
    // false → giriş yapılmamış → Login
    // true  → giriş yapılmış → rol kontrolü
    val isLoggedIn by authRepository.isLoggedIn
        .collectAsStateWithLifecycle(initialValue = null)

    when (isLoggedIn) {
        null -> SplashScreen()

        false -> UnAuthedNavHost(navController = rememberNavController())

        true -> {
            val session by authRepository.currentSession
                .collectAsStateWithLifecycle(initialValue = null)

            // Token var ama session henüz bellekte yok (soğuk açılış).
            // restoreSession() çağrısı session'ı dolduracak, recompose tetiklenecek.
            LaunchedEffect(Unit) {
                authRepository.restoreSession()
            }

            when (session?.user?.role) {
                null -> {
                    // restoreSession tamamlanana kadar splash
                    SplashScreen()
                }
                UserRole.STAFF,
                UserRole.ADMIN -> StaffNavHost(navController = rememberNavController())

                UserRole.USER -> UserNavHost(navController = rememberNavController())
            }
        }
    }
}
@Composable
private fun SplashScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}
@Composable
private fun UnAuthedNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Login) {
        composable<Login> {
            LoginScreen(
                // isLoggedIn Flow tetiklenince AppNavHost bu NavHost'u yok edip
                // UserNavHost / StaffNavHost render eder — callback boş kalabilir.
                onLoginSuccess = {},
                onNavigateToRegister = { navController.navigate(Register) },
            )
        }
        composable<Register> {
            RegisterScreen(
                onRegisterSuccess = {},
                onNavigateToLogin = { navController.popBackStack() },
            )
        }
    }
}

// USER rolü
@Composable
private fun UserNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Home) {
        composable<Home> {
            HomeScreen(
                onEventClick = { eventId -> navController.navigate(EventDetail(eventId)) },
                onMyTicketsClick = { navController.navigate(MyTickets) },
            )
        }
        composable<EventDetail> {
            EventDetailScreen(
                onBack = { navController.popBackStack() },
                onNavigateToTickets = {
                    navController.navigate(MyTickets) { popUpTo(Home) }
                },
            )
        }
        composable<MyTickets> {
            MyTicketsScreen(
                onTicketClick = { ticketId -> navController.navigate(TicketDetail(ticketId)) },
                onBack = { navController.popBackStack() },
            )
        }
        composable<TicketDetail> {
            TicketDetailScreen(
                onBack = { navController.popBackStack() },
            )
        }
    }
}

// STAFF / ADMIN rolü
@Composable
private fun StaffNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Staff) {
        composable<Staff> {
            StaffScreen()
            // Logout → authRepository.logout() → isLoggedIn=false → AppNavHost Login'e düşer
        }
    }
}