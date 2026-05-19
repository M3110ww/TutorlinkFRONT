package com.tutorlink.app.view.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.tutorlink.app.ui.theme.*
import com.tutorlink.app.utils.Resource
import com.tutorlink.app.utils.toReadableDateTime
import com.tutorlink.app.view.common.EmptyStateView
import com.tutorlink.app.view.common.ErrorView
import com.tutorlink.app.view.common.LoadingView
import com.tutorlink.app.view.components.StatusBadge
import com.tutorlink.app.view.components.TutoCard
import com.tutorlink.app.viewmodel.admin.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSessionsScreen(
    navController: NavController,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val sessionsState by viewModel.sessions.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Sesiones") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                }
            )
        }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = sessionsState is Resource.Loading,
            onRefresh = { viewModel.getSessions() },
            modifier = Modifier.padding(padding)
        ) {
            when (sessionsState) {
                is Resource.Idle, is Resource.Loading -> LoadingView()
                is Resource.Success -> {
                    val list = sessionsState.data ?: emptyList()
                    if (list.isEmpty()) {
                        EmptyStateView(
                            icon = Icons.Default.EventBusy,
                            title = "No hay sesiones",
                            subtitle = "Las sesiones programadas aparecerán aquí"
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(list) { session ->
                                AdminSessionItem(
                                    session = session,
                                    onConfirm = { viewModel.confirmSession(session.id) },
                                    onCancel = { viewModel.cancelSession(session.id) }
                                )
                            }
                        }
                    }
                }
                is Resource.Error -> ErrorView(
                    message = sessionsState.message ?: "Error al cargar sesiones",
                    onRetry = { viewModel.getSessions() }
                )
            }
        }
    }
}

@Composable
fun AdminSessionItem(session: SessionResponse, onConfirm: () -> Unit, onCancel: () -> Unit) {
    TutoCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("${session.studentName} ↔ ${session.tutorName}", fontWeight = FontWeight.Bold)
                Text(session.scheduledAt.toReadableDateTime(), color = Color.Gray, style = MaterialTheme.typography.bodySmall)
            }
            StatusBadgeUI(session.status)
        }
        
        if (session.status == SessionStatus.PENDING) {
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onCancel, colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)) {
                    Text("Cancelar")
                }
                Spacer(Modifier.width(8.dp))
                Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = MainPurple)) {
                    Text("Confirmar")
                }
            }
        }
    }
}

@Composable
private fun StatusBadgeUI(status: SessionStatus) {
    val (text, container, content) = when (status) {
        SessionStatus.PENDING -> Triple("Pendiente", SoftOrange, TextOrange)
        SessionStatus.CONFIRMED -> Triple("Confirmada", SoftGreen, TextGreen)
        SessionStatus.CANCELLED -> Triple("Cancelada", Color(0xFFFFEBEE), Color.Red)
        SessionStatus.COMPLETED -> Triple("Completada", LightPurple, MainPurple)
    }
    StatusBadge(text = text, containerColor = container, contentColor = content)
}
