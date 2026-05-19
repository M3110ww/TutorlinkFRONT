package com.tutorlink.app.view.student

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tutorlink.app.data.remote.dto.SessionModality
import com.tutorlink.app.ui.theme.*
import com.tutorlink.app.utils.Resource
import com.tutorlink.app.view.components.TutoButton
import com.tutorlink.app.view.components.TutoCard
import com.tutorlink.app.viewmodel.student.BookSessionViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookSessionScreen(
    tutorId: Long,
    navController: NavController,
    viewModel: BookSessionViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val bookingResult by viewModel.bookingResult.collectAsState()
    val tutorInfo by viewModel.tutorInfo.collectAsState()

    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }
    var durationMinutes by remember { mutableStateOf(60) }
    var modality by remember { mutableStateOf(SessionModality.VIRTUAL) }
    var meetingLink by remember { mutableStateOf("") }

    val fullDateFormatter = SimpleDateFormat("EEE, d 'de' MMMM · hh:mm a", Locale("es", "CO"))

    LaunchedEffect(tutorId) {
        viewModel.loadTutorInfo(tutorId)
    }

    LaunchedEffect(bookingResult) {
        if (bookingResult is Resource.Success) {
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reservar Sesión") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // Tutor Summary Card
            tutorInfo?.let { tutor ->
                TutoCard(modifier = Modifier.fillMaxWidth(), backgroundColor = ExtraLightPurple) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(tutor.tutorName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text(tutor.specialty ?: "Tutor", color = MainPurple, style = MaterialTheme.typography.bodyMedium)
                        }
                        Text("${String.format(java.util.Locale.US, "%,.0f", tutor.hourlyRate)} COP/h", fontWeight = FontWeight.Bold, color = DarkPurple)
                    }
                }
                Spacer(Modifier.height(24.dp))
            }

            // Date Selection
            Text("¿Cuándo será la sesión?", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            TutoCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    val dpd = android.app.DatePickerDialog(
                        context,
                        { _, y, m, d ->
                            selectedDate.set(y, m, d)
                            val tpd = android.app.TimePickerDialog(
                                context,
                                { _, h, min ->
                                    selectedDate.set(Calendar.HOUR_OF_DAY, h)
                                    selectedDate.set(Calendar.MINUTE, min)
                                    val newCal = Calendar.getInstance()
                                    newCal.time = selectedDate.time
                                    selectedDate = newCal
                                },
                                selectedDate.get(Calendar.HOUR_OF_DAY),
                                selectedDate.get(Calendar.MINUTE),
                                false
                            )
                            tpd.show()
                        },
                        selectedDate.get(Calendar.YEAR),
                        selectedDate.get(Calendar.MONTH),
                        selectedDate.get(Calendar.DAY_OF_MONTH)
                    )
                    dpd.show()
                }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarToday, null, tint = MainPurple)
                    Spacer(Modifier.width(12.dp))
                    Text(fullDateFormatter.format(selectedDate.time).replaceFirstChar { it.uppercase() })
                }
            }

            Spacer(Modifier.height(24.dp))

            // Duration Selection
            Text("Duración de la sesión", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(30, 60, 90, 120).forEach { mins ->
                    val isSelected = durationMinutes == mins
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { durationMinutes = mins },
                        color = if (isSelected) MainPurple else Color.White,
                        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray)
                    ) {
                        Box(Modifier.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                            Text("$mins min", color = if (isSelected) Color.White else Color.DarkGray, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Modality Selection
            Text("Modalidad", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf(SessionModality.VIRTUAL, SessionModality.IN_PERSON).forEach { mod ->
                    val isSelected = modality == mod
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { modality = mod },
                        color = if (isSelected) MainPurple else Color.White,
                        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray)
                    ) {
                        Box(Modifier.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                            Text(
                                if (mod == SessionModality.VIRTUAL) "Virtual" else "Presencial",
                                color = if (isSelected) Color.White else Color.DarkGray,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(visible = modality == SessionModality.VIRTUAL) {
                Column {
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = meetingLink,
                        onValueChange = { meetingLink = it },
                        label = { Text("Link de la reunión (opcional)") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Link, null) },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            // Summary Card
            tutorInfo?.let { tutor ->
                val total = (tutor.hourlyRate * durationMinutes) / 60.0
                TutoCard(modifier = Modifier.fillMaxWidth(), backgroundColor = Color(0xFFF8F9FA)) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Total a pagar:", fontWeight = FontWeight.Medium)
                        Text(String.format(Locale.US, "%,.0f COP", total), fontWeight = FontWeight.Bold, color = TextGreen, fontSize = 18.sp)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            if (bookingResult is Resource.Error) {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    color = Color(0xFFFFEBEE),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        (bookingResult as Resource.Error).message ?: "Error al reservar",
                        color = Color.Red,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            TutoButton(
                text = "Confirmar reserva",
                isLoading = bookingResult is Resource.Loading,
                onClick = {
                    val isoDate = String.format(
                        Locale.US,
                        "%04d-%02d-%02dT%02d:%02d:00",
                        selectedDate.get(Calendar.YEAR),
                        selectedDate.get(Calendar.MONTH) + 1,
                        selectedDate.get(Calendar.DAY_OF_MONTH),
                        selectedDate.get(Calendar.HOUR_OF_DAY),
                        selectedDate.get(Calendar.MINUTE)
                    )
                    viewModel.bookSession(
                        tutorId = tutorId,
                        scheduledAt = isoDate,
                        durationMinutes = durationMinutes,
                        modality = modality,
                        meetingLink = meetingLink.ifBlank { null }
                    )
                }
            )
        }
    }
}
