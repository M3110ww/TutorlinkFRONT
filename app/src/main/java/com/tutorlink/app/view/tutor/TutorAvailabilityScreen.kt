package com.tutorlink.app.view.tutor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
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
import com.tutorlink.app.data.remote.dto.AvailabilityResponse
import com.tutorlink.app.ui.theme.*
import com.tutorlink.app.utils.Resource
import com.tutorlink.app.view.common.EmptyStateView
import com.tutorlink.app.view.common.ErrorView
import com.tutorlink.app.view.common.LoadingView
import com.tutorlink.app.view.components.TutoCard
import com.tutorlink.app.viewmodel.tutor.TutorAvailabilityViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TutorAvailabilityScreen(
    navController: NavController,
    viewModel: TutorAvailabilityViewModel = hiltViewModel()
) {
    val availabilityState by viewModel.availability.collectAsState()
    val actionResult by viewModel.actionResult.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showAddDialog by remember { mutableStateOf(false) }

    // Manejo de mensajes de éxito/error
    LaunchedEffect(actionResult) {
        actionResult?.let {
            scope.launch { snackbarHostState.showSnackbar(it) }
            viewModel.clearActionResult()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Mi Disponibilidad", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true }, 
                containerColor = MainPurple,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, "Agregar", tint = Color.White)
            }
        }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = availabilityState is Resource.Loading,
            onRefresh = { viewModel.getAvailability() },
            modifier = Modifier.padding(padding)
        ) {
            when (availabilityState) {
                is Resource.Idle, is Resource.Loading -> LoadingView()
                is Resource.Success -> {
                    val list = availabilityState.data ?: emptyList()
                    if (list.isEmpty()) {
                        EmptyStateView(
                            icon = Icons.Default.EventAvailable,
                            title = "Sin horarios configurados",
                            subtitle = "Agrega los bloques de tiempo en los que estás disponible para que los estudiantes puedan reservarte."
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(list) { slot ->
                                AvailabilitySlotItem(
                                    slot = slot, 
                                    onDelete = { slot.id?.let { viewModel.deleteSlot(it) } }
                                )
                            }
                        }
                    }
                }
                is Resource.Error -> ErrorView(
                    message = availabilityState.message ?: "Error al cargar disponibilidad",
                    onRetry = { viewModel.getAvailability() }
                )
            }
        }

        if (showAddDialog) {
            AddAvailabilityDialog(
                onDismiss = { showAddDialog = false },
                onAdd = { day, start, end ->
                    viewModel.addSlot(day, start, end)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun AvailabilitySlotItem(slot: AvailabilityResponse, onDelete: () -> Unit) {
    val dayName = when (slot.dayOfWeek ?: 0) {
        1 -> "Lunes"
        2 -> "Martes"
        3 -> "Miércoles"
        4 -> "Jueves"
        5 -> "Viernes"
        6 -> "Sábado"
        7 -> "Domingo"
        else -> "Día"
    }
    
    TutoCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp)
        ) {
            Surface(
                color = ExtraLightPurple,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Schedule, contentDescription = null, tint = MainPurple)
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(dayName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                val start = slot.startTime?.take(5) ?: "00:00"
                val end = slot.endTime?.take(5) ?: "00:00"
                Text("$start - $end", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.DeleteOutline, "Eliminar", tint = Color.Red.copy(alpha = 0.7f))
            }
        }
    }
}

@Composable
fun AddAvailabilityDialog(onDismiss: () -> Unit, onAdd: (Int, String, String) -> Unit) {
    var day by remember { mutableIntStateOf(1) }
    var startTime by remember { mutableStateOf("08:00") }
    var endTime by remember { mutableStateOf("10:00") }

    val days = listOf("L", "M", "M", "J", "V", "S", "D")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo Horario de Tutoría", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Column {
                    Text("Día de la semana", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
                    Spacer(Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        days.forEachIndexed { index, name ->
                            val d = index + 1
                            val isSelected = day == d
                            Surface(
                                modifier = Modifier.size(38.dp).clickable { day = d },
                                shape = CircleShape,
                                color = if (isSelected) MainPurple else Color.LightGray.copy(alpha = 0.2f),
                                border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = name,
                                        color = if (isSelected) Color.White else Color.Black,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
                
                OutlinedTextField(
                    value = startTime,
                    onValueChange = { startTime = it },
                    label = { Text("Hora inicio (HH:mm)") },
                    placeholder = { Text("08:00") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.AutoMirrored.Filled.Login, null, modifier = Modifier.size(20.dp)) }
                )
                
                OutlinedTextField(
                    value = endTime,
                    onValueChange = { endTime = it },
                    label = { Text("Hora fin (HH:mm)") },
                    placeholder = { Text("10:00") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.AutoMirrored.Filled.Logout, null, modifier = Modifier.size(20.dp)) }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onAdd(day, startTime, endTime) },
                colors = ButtonDefaults.buttonColors(containerColor = MainPurple),
                shape = RoundedCornerShape(8.dp)
            ) { Text("Agregar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
