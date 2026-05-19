package com.tutorlink.app.view.admin

import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tutorlink.app.data.remote.dto.TutorResponse
import com.tutorlink.app.ui.theme.*
import com.tutorlink.app.utils.Resource
import com.tutorlink.app.view.common.EmptyStateView
import com.tutorlink.app.view.common.ErrorView
import com.tutorlink.app.view.common.LoadingView
import com.tutorlink.app.view.components.*
import com.tutorlink.app.viewmodel.admin.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminTutorsScreen(
    navController: NavController,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val tutorsState by viewModel.tutors.collectAsState()
    val updateStatus by viewModel.updateStatus.collectAsState()
    var selectedTutor by remember { mutableStateOf<TutorResponse?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.getTutors()
    }

    LaunchedEffect(updateStatus) {
        if (updateStatus is Resource.Success) {
            showEditDialog = false
            selectedTutor = null
            viewModel.clearUpdateStatus()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestionar Tutores") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = tutorsState is Resource.Loading,
            onRefresh = { viewModel.getTutors() },
            modifier = Modifier.padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                when (tutorsState) {
                    is Resource.Idle, is Resource.Loading -> LoadingView()
                    is Resource.Success -> {
                        val tutors = tutorsState.data ?: emptyList()
                        if (tutors.isEmpty()) {
                            EmptyStateView(
                                icon = Icons.Default.PeopleOutline,
                                title = "No hay tutores",
                                subtitle = "Los tutores registrados aparecerán aquí"
                            )
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(tutors) { tutor ->
                                    AdminTutorItem(
                                        tutor = tutor,
                                        onToggleStatus = { viewModel.toggleTutorStatus(tutor.id, !tutor.active) },
                                        onEdit = {
                                            selectedTutor = tutor
                                            showEditDialog = true
                                        }
                                    )
                                }
                            }
                        }
                    }
                    is Resource.Error -> ErrorView(message = tutorsState.message ?: "Error", onRetry = { viewModel.getTutors() })
                }
            }
        }

        if (showEditDialog && selectedTutor != null) {
            EditTutorDialog(
                tutor = selectedTutor!!,
                isLoading = updateStatus is Resource.Loading,
                onDismiss = { 
                    showEditDialog = false
                    selectedTutor = null
                },
                onConfirm = { specialty, hourlyRate, description ->
                    viewModel.updateTutor(selectedTutor!!.id, specialty, hourlyRate, description)
                }
            )
        }
    }
}

@Composable
fun AdminTutorItem(tutor: TutorResponse, onToggleStatus: () -> Unit, onEdit: () -> Unit) {
    TutoCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            TutoAvatar(name = tutor.tutorName, size = 48.dp)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = tutor.tutorName, fontWeight = FontWeight.Bold, maxLines = 1)
                Text(text = tutor.email, style = MaterialTheme.typography.bodySmall, color = Color.Gray, maxLines = 1)
            }
            StatusBadge(
                text = if (tutor.active) "Activo" else "Inactivo",
                containerColor = if (tutor.active) SoftGreen else Color.LightGray.copy(alpha = 0.3f),
                contentColor = if (tutor.active) TextGreen else Color.Gray
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Especialidad: ${tutor.specialty ?: "N/A"}",
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    lineHeight = 18.sp
                )
                Text(
                    text = "Tarifa: ${String.format(java.util.Locale.US, "%,.0f", tutor.hourlyRate)} COP",
                    style = MaterialTheme.typography.bodySmall,
                    color = MainPurple,
                    fontWeight = FontWeight.Medium
                )
            }
            
            // Acciones alineadas a la derecha
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar",
                        tint = MainPurple,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(4.dp))
                
                Switch(
                    checked = tutor.active,
                    onCheckedChange = { onToggleStatus() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MainPurple,
                        checkedTrackColor = LightPurple
                    ),
                    modifier = Modifier.scale(0.8f) // Reducimos un poco el switch para que quepa mejor
                )
            }
        }
    }
}

@Composable
fun EditTutorDialog(
    tutor: TutorResponse,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String, Double, String?) -> Unit
) {
    var specialty by remember { mutableStateOf(tutor.specialty ?: "") }
    var hourlyRate by remember { mutableStateOf(tutor.hourlyRate.toString()) }
    var description by remember { mutableStateOf(tutor.description ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Tutor: ${tutor.tutorName}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = specialty,
                    onValueChange = { specialty = it },
                    label = { Text("Especialidad") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )
                OutlinedTextField(
                    value = hourlyRate,
                    onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) hourlyRate = it },
                    label = { Text("Tarifa por hora (COP)") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    enabled = !isLoading
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(specialty, hourlyRate.toDoubleOrNull() ?: 0.0, description) },
                enabled = specialty.isNotBlank() && hourlyRate.isNotBlank() && !isLoading
            ) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                else Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) {
                Text("Cancelar")
            }
        }
    )
}
