package com.tutorlink.app.view.tutor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
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
import com.tutorlink.app.view.components.*
import com.tutorlink.app.viewmodel.AuthViewModel
import com.tutorlink.app.viewmodel.tutor.TutorProfileViewModel
import com.tutorlink.app.view.common.LoadingView
import com.tutorlink.app.view.common.ProfileInfoItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TutorProfileScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel(),
    tutorViewModel: TutorProfileViewModel = hiltViewModel()
) {
    val tutorProfileState by tutorViewModel.profile.collectAsState()
    val tutorUpdateResult by tutorViewModel.updateResult.collectAsState()

    var isEditMode by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var specialty by remember { mutableStateOf("") }
    var hourlyRate by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    LaunchedEffect(tutorProfileState) {
        if (tutorProfileState is Resource.Success) {
            val p = (tutorProfileState as Resource.Success).data
            specialty = p?.specialty ?: ""
            hourlyRate = p?.hourlyRate?.let { if (it == 0.0) "" else it.toString() } ?: ""
            description = p?.description ?: ""
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
                title = { Text(if (isEditMode) "Editar Perfil" else "Mi Perfil de Tutor") },
                navigationIcon = {
                    IconButton(onClick = { if (isEditMode) isEditMode = false else navController.popBackStack() }) {
                        Icon(
                            imageVector = if (isEditMode) Icons.Default.Close else Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Regresar"
                        )
                    }
                }
            )
        }
    ) { padding ->
        when (tutorProfileState) {
            is Resource.Loading -> LoadingView(Modifier.padding(padding))
            is Resource.Success -> {
                val profile = (tutorProfileState as Resource.Success).data
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TutoAvatar(name = profile?.tutorName ?: "Tutor", size = 100.dp)
                    Spacer(Modifier.height(16.dp))
                    Text(profile?.tutorName ?: "Tutor", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        repeat(5) { index ->
                            Icon(
                                imageVector = if (index < (profile?.averageRating?.toInt() ?: 0)) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = null,
                                tint = Color(0xFFFFB300),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(Modifier.width(4.dp))
                        Text(
                            String.format(java.util.Locale.US, "%.1f", profile?.averageRating ?: 0.0),
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                    }
                    
                    StatusBadge(
                        text = "Tutor Profesional",
                        containerColor = LightPurple,
                        contentColor = MainPurple
                    )

                    Spacer(Modifier.height(32.dp))

                    if (!isEditMode) {
                        TutoCard(modifier = Modifier.fillMaxWidth()) {
                            ProfileInfoItem(Icons.Default.Work, "Especialidad", profile?.specialty ?: "No especificada")
                            Spacer(Modifier.height(16.dp))
                            ProfileInfoItem(Icons.Default.Payments, "Tarifa por hora", "${String.format(java.util.Locale.US, "%,.0f", profile?.hourlyRate ?: 0.0)} COP")
                            Spacer(Modifier.height(16.dp))
                            ProfileInfoItem(Icons.Default.Description, "Descripción", profile?.description ?: "Sin descripción")
                            Spacer(Modifier.height(16.dp))
                            ProfileInfoItem(Icons.Default.Email, "Email", profile?.email ?: "")
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
                        TutoTextField(
                            value = specialty,
                            onValueChange = { specialty = it },
                            label = "Especialidad",
                            leadingIcon = { Icon(Icons.Default.Work, null) }
                        )
                        Spacer(Modifier.height(16.dp))
                        TutoTextField(
                            value = hourlyRate,
                            onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) hourlyRate = it },
                            label = "Tarifa por hora (COP)",
                            leadingIcon = { Icon(Icons.Default.Payments, null) }
                        )
                        Spacer(Modifier.height(16.dp))
                        TutoTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = "Descripción",
                            modifier = Modifier.height(120.dp),
                            placeholder = { Text("Cuéntanos sobre tu experiencia como tutor...") }
                        )

                        Spacer(Modifier.height(32.dp))
                        
                        TutoButton(
                            text = "Guardar cambios",
                            isLoading = tutorUpdateResult is Resource.Loading,
                            onClick = {
                                tutorViewModel.updateProfile(specialty, description, hourlyRate.toDoubleOrNull() ?: 0.0)
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
            is Resource.Error -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text((tutorProfileState as Resource.Error).message ?: "Error desconocido")
                }
            }
            else -> {}
        }
    }
}
