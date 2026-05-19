package com.tutorlink.app.view.tutor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tutorlink.app.data.remote.dto.SessionResponse
import com.tutorlink.app.data.remote.dto.SessionStatus
import com.tutorlink.app.navigation.Screen
import com.tutorlink.app.ui.theme.*
import com.tutorlink.app.utils.Resource
import com.tutorlink.app.utils.toReadableDateTime
import com.tutorlink.app.view.common.EmptyStateView
import com.tutorlink.app.view.common.ErrorView
import com.tutorlink.app.view.common.LoadingView
import com.tutorlink.app.view.components.*
import com.tutorlink.app.viewmodel.AuthViewModel
import com.tutorlink.app.viewmodel.tutor.TutorDashboardViewModel
import java.time.LocalDate
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TutorDashboardScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel(),
    dashboardViewModel: TutorDashboardViewModel = hiltViewModel()
) {
    val sessionsState by dashboardViewModel.sessions.collectAsState()
    val userName by authViewModel.userName.collectAsState()

    val today = LocalDate.now().toString()
    
    val (sessionsToday, earningsMonth) = when (sessionsState) {
        is Resource.Success -> {
            val sessions = sessionsState.data ?: emptyList()
            val todayCount = sessions.count { 
                it.scheduledAt.startsWith(today) && 
                (it.status == SessionStatus.CONFIRMED || it.status == SessionStatus.PENDING)
            }
            val monthlyEarnings = sessions.filter { it.status == SessionStatus.COMPLETED }.sumOf { it.totalCost ?: 0.0 }
            Pair(todayCount.toString(), "${String.format(Locale.US, "%,.0f", monthlyEarnings)} COP")
        }
        else -> Pair("—", "—")
    }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                NavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = { Icon(Icons.Default.Dashboard, null) },
                    label = { Text("Dashboard") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(Screen.TutorAvailability.route) },
                    icon = { Icon(Icons.Default.EventAvailable, null) },
                    label = { Text("Disponibilidad") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(Screen.TutorProfile.route) },
                    icon = { Icon(Icons.Default.Person, null) },
                    label = { Text("Perfil") }
                )
            }
        }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = sessionsState is Resource.Loading,
            onRefresh = { dashboardViewModel.getTutorSessions() },
            modifier = Modifier.padding(padding)
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Hola, ${userName?.split(" ")?.firstOrNull() ?: "Tutor"}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                        if (sessionsState is Resource.Success) {
                            val pending = (sessionsState.data ?: emptyList()).count { it.status == SessionStatus.PENDING }
                            Text("Tienes $pending solicitudes pendientes", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                        }
                    }
                    TutoAvatar(name = userName ?: "Tutor", size = 50.dp)
                }

                Spacer(Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    TutoCard(modifier = Modifier.weight(1f), backgroundColor = ExtraLightPurple) {
                        Icon(Icons.Default.Schedule, null, tint = MainPurple)
                        Spacer(Modifier.height(8.dp))
                        Text(sessionsToday, style = MaterialTheme.typography.titleLarge)
                        Text("Sesiones hoy", style = MaterialTheme.typography.labelSmall)
                    }
                    TutoCard(modifier = Modifier.weight(1f), backgroundColor = SoftOrange) {
                        Icon(Icons.Default.Payments, null, tint = TextOrange)
                        Spacer(Modifier.height(8.dp))
                        Text(earningsMonth, style = MaterialTheme.typography.titleLarge)
                        Text("Ingresos mes", style = MaterialTheme.typography.labelSmall)
                    }
                }

                Spacer(Modifier.height(32.dp))
                Text("Próximas Sesiones", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))

                when (sessionsState) {
                    is Resource.Idle, is Resource.Loading -> LoadingView()
                    is Resource.Success -> {
                        val sessions = sessionsState.data ?: emptyList()
                        if (sessions.isEmpty()) {
                            EmptyStateView(
                                icon = Icons.Default.EventBusy,
                                title = "Sin sesiones pendientes",
                                subtitle = "Cuando estudiantes reserven contigo, aparecerán aquí"
                            )
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(sessions) { session ->
                                    TutorSessionItem(
                                        session = session,
                                        onAccept = { dashboardViewModel.confirmSession(session.id) },
                                        onReject = { dashboardViewModel.cancelSession(session.id) },
                                        onComplete = { dashboardViewModel.completeSession(session.id) }
                                    )
                                }
                            }
                        }
                    }
                    is Resource.Error -> ErrorView(
                        message = sessionsState.message ?: "Error al cargar sesiones",
                        onRetry = { dashboardViewModel.getTutorSessions() }
                    )
                }
            }
        }
    }
}

@Composable
fun TutorSessionItem(
    session: SessionResponse,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onComplete: () -> Unit
) {
    TutoCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TutoAvatar(name = session.studentName, size = 40.dp)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(session.studentName, fontWeight = FontWeight.Bold)
                Text(session.scheduledAt.toReadableDateTime(), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            StatusBadgeUI(session.status)
        }
        
        if (session.status == SessionStatus.PENDING) {
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = onReject) { Icon(Icons.Default.Close, null, tint = Color.Red) }
                IconButton(onClick = onAccept) { Icon(Icons.Default.Check, null, tint = TextGreen) }
            }
        } else if (session.status == SessionStatus.CONFIRMED) {
            Spacer(Modifier.height(12.dp))
            TextButton(
                onClick = onComplete,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.textButtonColors(contentColor = MainPurple)
            ) {
                Icon(Icons.Default.CheckCircle, null)
                Spacer(Modifier.width(8.dp))
                Text("Marcar completada")
            }
        }
    }
}

@Composable
fun StatusBadgeUI(status: SessionStatus) {
    val (text, container, content) = when (status) {
        SessionStatus.PENDING -> Triple("Pendiente", SoftOrange, TextOrange)
        SessionStatus.CONFIRMED -> Triple("Confirmada", SoftGreen, TextGreen)
        SessionStatus.CANCELLED -> Triple("Cancelada", Color(0xFFFFEBEE), Color.Red)
        SessionStatus.COMPLETED -> Triple("Completada", LightPurple, MainPurple)
    }
    StatusBadge(text = text, containerColor = container, contentColor = content)
}
