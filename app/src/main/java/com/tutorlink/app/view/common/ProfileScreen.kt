package com.tutorlink.app.view.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tutorlink.app.navigation.Screen
import com.tutorlink.app.ui.theme.*
import com.tutorlink.app.utils.Resource
import com.tutorlink.app.view.common.LoadingView
import com.tutorlink.app.view.components.*
import com.tutorlink.app.viewmodel.AuthViewModel
import com.tutorlink.app.viewmodel.student.StudentProfileViewModel
import com.tutorlink.app.viewmodel.tutor.TutorProfileViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel(),
    studentViewModel: StudentProfileViewModel = hiltViewModel(),
    tutorViewModel: TutorProfileViewModel = hiltViewModel()
) {
    val userRole by authViewModel.userRole.collectAsState()
    val studentProfileState by studentViewModel.profile.collectAsState()
    val tutorProfileState by tutorViewModel.profile.collectAsState()
    val studentUpdateResult by studentViewModel.updateResult.collectAsState()
    val tutorUpdateResult by tutorViewModel.updateResult.collectAsState()

    var isEditMode by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Student fields
    var academicLevel by remember { mutableStateOf("") }
    var interests by remember { mutableStateOf("") }

    // Tutor fields
    var specialty by remember { mutableStateOf("") }
    var hourlyRate by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    // Sync fields when profile loaded
    LaunchedEffect(studentProfileState) {
        if (studentProfileState is Resource.Success) {
            val p = (studentProfileState as Resource.Success).data
            academicLevel = p?.academicLevel ?: ""
            interests = p?.interests ?: ""
        }
    }
    LaunchedEffect(tutorProfileState) {
        if (tutorProfileState is Resource.Success) {
            val p = (tutorProfileState as Resource.Success).data
            specialty = p?.specialty ?: ""
            hourlyRate = p?.hourlyRate.toString()
            description = p?.description ?: ""
        }
    }

    // Handle update results
    LaunchedEffect(studentUpdateResult) {
        studentUpdateResult?.let {
            if (it is Resource.Success) {
                isEditMode = false
                scope.launch { snackbarHostState.showSnackbar("Perfil actualizado") }
                studentViewModel.clearUpdateResult()
                studentViewModel.loadProfile()
            } else if (it is Resource.Error) {
                scope.launch { snackbarHostState.showSnackbar(it.message ?: "Error al actualizar") }
            }
        }
    }
    LaunchedEffect(tutorUpdateResult) {
        tutorUpdateResult?.let {
            if (it is Resource.Success) {
                isEditMode = false
                scope.launch { snackbarHostState.showSnackbar("Perfil actualizado") }
                tutorViewModel.clearUpdateResult()
                tutorViewModel.loadProfile()
            } else if (it is Resource.Error) {
                scope.launch { snackbarHostState.showSnackbar(it.message ?: "Error al actualizar") }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Editar Perfil" else "Mi Perfil") },
                navigationIcon = {
                    IconButton(onClick = { if (isEditMode) isEditMode = false else navController.popBackStack() }) {
                        Icon(if (isEditMode) Icons.Default.Close else Icons.Default.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        val isLoading = if (userRole == "STUDENT") studentProfileState is Resource.Loading else tutorProfileState is Resource.Loading
        
        if (isLoading) {
            LoadingView(Modifier.padding(padding))
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val name = if (userRole == "STUDENT") {
                    (studentProfileState as? Resource.Success)?.data?.studentName ?: "Estudiante"
                } else {
                    (tutorProfileState as? Resource.Success)?.data?.tutorName ?: "Tutor"
                }

                TutoAvatar(name = name, size = 100.dp)
                Spacer(Modifier.height(16.dp))
                Text(name, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                
                StatusBadge(
                    text = if (userRole == "STUDENT") "Estudiante" else "Tutor",
                    containerColor = if (userRole == "STUDENT") SoftGreen else LightPurple,
                    contentColor = if (userRole == "STUDENT") TextGreen else MainPurple
                )

                Spacer(Modifier.height(32.dp))

                if (!isEditMode) {
                    // VIEW MODE
                    TutoCard(modifier = Modifier.fillMaxWidth()) {
                        if (userRole == "STUDENT") {
                            ProfileInfoItem(Icons.Default.School, "Nivel Académico", academicLevel)
                            Spacer(Modifier.height(16.dp))
                            ProfileInfoItem(Icons.Default.Favorite, "Intereses", interests.ifBlank { "No especificados" })
                        } else {
                            ProfileInfoItem(Icons.Default.Work, "Especialidad", specialty)
                            Spacer(Modifier.height(16.dp))
                            ProfileInfoItem(Icons.Default.Payments, "Tarifa por hora", "$${hourlyRate} COP")
                            Spacer(Modifier.height(16.dp))
                            ProfileInfoItem(Icons.Default.Description, "Descripción", description.ifBlank { "Sin descripción" })
                        }
                    }

                    Spacer(Modifier.height(32.dp))
                    TutoButton(
                        text = "Editar perfil",
                        onClick = { isEditMode = true },
                        containerColor = MainPurple
                    )
                    Spacer(Modifier.height(12.dp))
                    TutoButton(
                        text = "Cerrar sesión",
                        onClick = {
                            authViewModel.logout()
                            navController.navigate(Screen.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        containerColor = Color(0xFFFFEBEE),
                        contentColor = Color.Red
                    )
                } else {
                    // EDIT MODE
                    if (userRole == "STUDENT") {
                        TutoTextField(
                            value = academicLevel,
                            onValueChange = { academicLevel = it },
                            label = "Nivel Académico",
                            leadingIcon = { Icon(Icons.Default.School, null) }
                        )
                        Spacer(Modifier.height(16.dp))
                        TutoTextField(
                            value = interests,
                            onValueChange = { interests = it },
                            label = "Intereses",
                            leadingIcon = { Icon(Icons.Default.Favorite, null) }
                        )
                    } else {
                        TutoTextField(
                            value = specialty,
                            onValueChange = { specialty = it },
                            label = "Especialidad",
                            leadingIcon = { Icon(Icons.Default.Work, null) }
                        )
                        Spacer(Modifier.height(16.dp))
                        TutoTextField(
                            value = hourlyRate,
                            onValueChange = { hourlyRate = it },
                            label = "Tarifa por hora (COP)",
                            leadingIcon = { Icon(Icons.Default.Payments, null) }
                        )
                        Spacer(Modifier.height(16.dp))
                        TutoTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = "Descripción",
                            modifier = Modifier.height(120.dp),
                            placeholder = { Text("Cuéntanos sobre ti...") }
                        )
                    }

                    Spacer(Modifier.height(32.dp))
                    
                    val isUpdating = if (userRole == "STUDENT") studentUpdateResult is Resource.Loading else tutorUpdateResult is Resource.Loading
                    
                    TutoButton(
                        text = "Guardar cambios",
                        isLoading = isUpdating,
                        onClick = {
                            if (userRole == "STUDENT") {
                                studentViewModel.updateProfile(academicLevel, interests)
                            } else {
                                tutorViewModel.updateProfile(specialty, description, hourlyRate.toDoubleOrNull() ?: 0.0)
                            }
                        }
                    )
                    Spacer(Modifier.height(12.dp))
                    TutoSecondaryButton(
                        text = "Cancelar",
                        onClick = { isEditMode = false }
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileInfoItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = MainPurple, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
            Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        }
    }
}

