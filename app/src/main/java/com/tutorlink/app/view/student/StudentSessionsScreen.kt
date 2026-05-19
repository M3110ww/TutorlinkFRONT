package com.tutorlink.app.view.student

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import java.util.Locale
import com.tutorlink.app.data.remote.dto.SessionResponse
import com.tutorlink.app.data.remote.dto.SessionStatus
import com.tutorlink.app.navigation.Screen
import com.tutorlink.app.ui.theme.*
import com.tutorlink.app.utils.Resource
import com.tutorlink.app.utils.toReadableDateTime
import com.tutorlink.app.view.common.EmptyStateView
import com.tutorlink.app.view.common.ErrorView
import com.tutorlink.app.view.common.LoadingView
import com.tutorlink.app.view.components.StatusBadge
import com.tutorlink.app.view.components.TutoAvatar
import com.tutorlink.app.view.components.TutoCard
import com.tutorlink.app.viewmodel.student.CreateReviewViewModel
import com.tutorlink.app.viewmodel.student.StudentSessionsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentSessionsScreen(
    navController: NavController,
    viewModel: StudentSessionsViewModel = hiltViewModel(),
    reviewViewModel: CreateReviewViewModel = hiltViewModel()
) {
    val sessionsState by viewModel.sessions.collectAsState()
    val reviewResult by reviewViewModel.result.collectAsState()
    
    var showReviewSheet by remember { mutableStateOf<SessionResponse?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(reviewResult) {
        if (reviewResult is Resource.Success) {
            showReviewSheet = null
            snackbarHostState.showSnackbar("¡Reseña publicada!")
            reviewViewModel.clearResult()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Mis sesiones", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                    val sessions = sessionsState.data ?: emptyList()
                    if (sessions.isEmpty()) {
                        EmptyStateView(
                            icon = Icons.Default.CalendarToday,
                            title = "Sin sesiones aún",
                            subtitle = "Reserva tu primera tutoría con un experto",
                            actionLabel = "Explorar tutores",
                            onAction = { navController.navigate(Screen.TutorList.route) }
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(sessions) { session ->
                                NewSessionItem(
                                    session = session,
                                    onLeaveReview = { showReviewSheet = session }
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

        if (showReviewSheet != null) {
            ReviewBottomSheet(
                session = showReviewSheet!!,
                onDismiss = { showReviewSheet = null },
                onSubmit = { rating, comment ->
                    reviewViewModel.submitReview(showReviewSheet!!.id, rating, comment)
                },
                isSubmitting = reviewResult is Resource.Loading
            )
        }
    }
}

@Composable
fun NewSessionItem(session: SessionResponse, onLeaveReview: () -> Unit) {
    TutoCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TutoAvatar(name = session.tutorName, size = 52.dp, containerColor = LightPurple)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = session.tutorName, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text(
                    text = session.scheduledAt.toReadableDateTime(), 
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
            StatusBadgeUI(status = session.status)
        }
        
        if (session.status == SessionStatus.COMPLETED) {
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onLeaveReview,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = ExtraLightPurple, contentColor = MainPurple),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(Icons.Default.Star, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text("Dejar reseña", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewBottomSheet(
    session: SessionResponse,
    onDismiss: () -> Unit,
    onSubmit: (Int, String) -> Unit,
    isSubmitting: Boolean
) {
    var rating by remember { mutableStateOf(5) }
    var comment by remember { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.padding(24.dp).padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("¿Cómo fue tu sesión con ${session.tutorName}?", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(Modifier.height(20.dp))
            
            Row {
                repeat(5) { i ->
                    val starIndex = i + 1
                    IconButton(onClick = { rating = starIndex }) {
                        Icon(
                            imageVector = if (starIndex <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = null,
                            tint = if (starIndex <= rating) Color(0xFFFFB300) else Color.Gray,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(20.dp))
            OutlinedTextField(
                value = comment,
                onValueChange = { comment = it },
                placeholder = { Text("Cuéntanos tu experiencia (opcional)...") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 4,
                shape = RoundedCornerShape(12.dp)
            )
            
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { onSubmit(rating, comment) },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !isSubmitting,
                colors = ButtonDefaults.buttonColors(containerColor = MainPurple)
            ) {
                if (isSubmitting) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                else Text("Publicar reseña")
            }
        }
    }
}

@Composable
fun StatusBadgeUI(status: SessionStatus) {
    val (containerColor, contentColor, label) = when (status) {
        SessionStatus.PENDING -> Triple(SoftOrange, TextOrange, "Pendiente")
        SessionStatus.CONFIRMED -> Triple(SoftGreen, TextGreen, "Confirmada")
        SessionStatus.CANCELLED -> Triple(Color(0xFFFFEBEE), Color.Red, "Cancelada")
        SessionStatus.COMPLETED -> Triple(LightPurple, MainPurple, "Completada")
    }
    StatusBadge(text = label, containerColor = containerColor, contentColor = contentColor)
}
