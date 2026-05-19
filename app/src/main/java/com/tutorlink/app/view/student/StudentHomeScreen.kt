package com.tutorlink.app.view.student

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.Icons
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
import com.tutorlink.app.data.remote.dto.SessionStatus
import com.tutorlink.app.navigation.Screen
import com.tutorlink.app.ui.theme.ExtraLightPurple
import com.tutorlink.app.ui.theme.LightPurple
import com.tutorlink.app.ui.theme.MainPurple
import com.tutorlink.app.ui.theme.SoftGreen
import com.tutorlink.app.ui.theme.SoftOrange
import com.tutorlink.app.ui.theme.TextGreen
import com.tutorlink.app.view.components.*
import com.tutorlink.app.utils.Resource
import com.tutorlink.app.viewmodel.AuthViewModel
import com.tutorlink.app.viewmodel.student.StudentHomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentHomeScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel(),
    homeViewModel: StudentHomeViewModel = hiltViewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    val userName by authViewModel.userName.collectAsState()
    val featuredTutorsState by homeViewModel.featuredTutors.collectAsState()
    val sessionsState by homeViewModel.sessions.collectAsState()
    val firstName = userName?.split(" ")?.firstOrNull() ?: "Estudiante"

    val (upcomingSessionsCount, hoursThisMonth) = when (sessionsState) {
        is Resource.Success -> {
            val sessions = sessionsState.data ?: emptyList()
            val upcoming = sessions.count { it.status == SessionStatus.CONFIRMED || it.status == SessionStatus.PENDING }
            val hours = sessions.filter { it.status == SessionStatus.COMPLETED }.sumOf { it.durationMinutes.toDouble() } / 60.0
            Pair(upcoming.toString(), "${String.format("%.0f", hours)}h")
        }
        else -> Pair("—", "—")
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = true,
                    onClick = { /* Ya estamos aquí */ },
                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                    label = { Text("Inicio") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MainPurple,
                        selectedTextColor = MainPurple,
                        indicatorColor = ExtraLightPurple
                    )
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(Screen.TutorList.route) },
                    icon = { Icon(Icons.Default.Search, contentDescription = null) },
                    label = { Text("Explorar") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(Screen.StudentSessions.route) },
                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                    label = { Text("Sesiones") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(Screen.StudentProfile.route) },
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    label = { Text("Perfil") }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            // Header: Greeting and Avatar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Buenos días, $firstName",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp
                        )
                    )
                    Text(
                        text = "¿Listo para aprender algo nuevo?",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                }
                TutoAvatar(name = firstName, size = 52.dp, containerColor = SoftOrange)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Search Bar
            TutoSearchBar(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                onSearch = {
                    if (searchQuery.isNotBlank()) {
                        navController.navigate(Screen.TutorList.route) 
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Stats Row (Mantenemos visual, pero listo para conectar)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TutoCard(
                    modifier = Modifier.weight(1f),
                    backgroundColor = ExtraLightPurple,
                    onClick = { navController.navigate(Screen.StudentSessions.route) }
                ) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = MainPurple)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = upcomingSessionsCount, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(text = "Próximas", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                }

                TutoCard(
                    modifier = Modifier.weight(1f),
                    backgroundColor = SoftGreen.copy(alpha = 0.4f)
                ) {
                    Icon(Icons.Default.HourglassEmpty, contentDescription = null, tint = TextGreen)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = hoursThisMonth, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(text = "Este mes", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Featured Tutors Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tutores destacados",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = { navController.navigate(Screen.TutorList.route) }) {
                    Text("Ver todos", color = MainPurple, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Featured Tutors List from DB
            when (featuredTutorsState) {
                is Resource.Loading, is Resource.Idle -> {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MainPurple)
                    }
                }
                is Resource.Success -> {
                    val tutors = featuredTutorsState.data?.take(3) ?: emptyList()
                    if (tutors.isEmpty()) {
                        Text("No hay tutores disponibles por el momento.", color = Color.Gray)
                    } else {
                        tutors.forEach { tutor ->
                            TutoCard(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { navController.navigate(Screen.TutorDetail.createRoute(tutor.id)) }
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    TutoAvatar(name = tutor.tutorName, size = 56.dp, containerColor = LightPurple)
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = tutor.tutorName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                        Text(text = tutor.specialty ?: "Sin especialidad", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                                    }
                                    StatusBadge(text = "Disponible", containerColor = SoftGreen, contentColor = TextGreen)
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "${String.format(java.util.Locale.US, "%,.0f", tutor.hourlyRate)} COP/hr", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(text = "⭐ ${tutor.averageRating}", color = Color.Gray, fontSize = 14.sp)
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
                is Resource.Error -> {
                    Text("Error al cargar tutores: ${featuredTutorsState.message}", color = Color.Red)
                }
            }
        }
    }
}
