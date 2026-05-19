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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tutorlink.app.data.remote.dto.StudentResponse
import com.tutorlink.app.ui.theme.*
import com.tutorlink.app.utils.Resource
import com.tutorlink.app.view.common.EmptyStateView
import com.tutorlink.app.view.common.ErrorView
import com.tutorlink.app.view.common.LoadingView
import com.tutorlink.app.view.components.TutoAvatar
import com.tutorlink.app.view.components.TutoCard
import com.tutorlink.app.viewmodel.admin.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminStudentsScreen(
    navController: NavController,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val studentsState by viewModel.students.collectAsState()
    val updateStatus by viewModel.updateStatus.collectAsState()
    var selectedStudent by remember { mutableStateOf<StudentResponse?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.getStudents()
    }

    LaunchedEffect(updateStatus) {
        if (updateStatus is Resource.Success) {
            showEditDialog = false
            selectedStudent = null
            viewModel.clearUpdateStatus()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestionar Estudiantes") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = studentsState is Resource.Loading,
            onRefresh = { viewModel.getStudents() },
            modifier = Modifier.padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                when (studentsState) {
                    is Resource.Idle, is Resource.Loading -> LoadingView()
                    is Resource.Success -> {
                        val students = studentsState.data ?: emptyList()
                        if (students.isEmpty()) {
                            EmptyStateView(
                                icon = Icons.Default.PersonSearch,
                                title = "No hay estudiantes",
                                subtitle = "Los estudiantes registrados aparecerán aquí"
                            )
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(students) { student ->
                                    AdminStudentItem(
                                        student = student,
                                        onEdit = {
                                            selectedStudent = student
                                            showEditDialog = true
                                        }
                                    )
                                }
                            }
                        }
                    }
                    is Resource.Error -> ErrorView(message = studentsState.message ?: "Error", onRetry = { viewModel.getStudents() })
                }
            }
        }

        if (showEditDialog && selectedStudent != null) {
            EditStudentDialog(
                student = selectedStudent!!,
                isLoading = updateStatus is Resource.Loading,
                onDismiss = {
                    showEditDialog = false
                    selectedStudent = null
                },
                onConfirm = { level, interests ->
                    viewModel.updateStudent(selectedStudent!!.id, level, interests)
                }
            )
        }
    }
}

@Composable
fun AdminStudentItem(student: StudentResponse, onEdit: () -> Unit) {
    TutoCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TutoAvatar(name = student.studentName, size = 48.dp)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = student.studentName, fontWeight = FontWeight.Bold)
                Text(text = student.email, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MainPurple)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.School,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MainPurple
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = student.academicLevel ?: "Sin Nivel Académico",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun EditStudentDialog(
    student: StudentResponse,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String, String?) -> Unit
) {
    var academicLevel by remember { mutableStateOf(student.academicLevel ?: "") }
    var interests by remember { mutableStateOf(student.interests ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Estudiante: ${student.studentName}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = academicLevel,
                    onValueChange = { academicLevel = it },
                    label = { Text("Nivel Académico") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )
                OutlinedTextField(
                    value = interests,
                    onValueChange = { interests = it },
                    label = { Text("Intereses (separados por coma)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    enabled = !isLoading
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(academicLevel, interests) },
                enabled = academicLevel.isNotBlank() && !isLoading
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
