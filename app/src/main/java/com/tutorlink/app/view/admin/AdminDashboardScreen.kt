package com.tutorlink.app.view.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
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
import com.tutorlink.app.navigation.Screen
import com.tutorlink.app.ui.theme.*
import com.tutorlink.app.utils.Resource
import com.tutorlink.app.view.common.ErrorView
import com.tutorlink.app.view.common.LoadingView
import com.tutorlink.app.view.components.*
import com.tutorlink.app.viewmodel.AuthViewModel
import com.tutorlink.app.viewmodel.admin.AdminViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel(),
    adminViewModel: AdminViewModel = hiltViewModel()
) {
    val statsState by adminViewModel.stats.collectAsState()
    val tutorsState by adminViewModel.tutors.collectAsState()
    val studentsState by adminViewModel.students.collectAsState()
    val sessionsState by adminViewModel.sessions.collectAsState()

    val isRefreshing = statsState is Resource.Loading || tutorsState is Resource.Loading
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel de Control") },
                actions = {
                    IconButton(onClick = {
                        adminViewModel.getStats()
                        adminViewModel.getTutors()
                        adminViewModel.getStudents()
                        adminViewModel.getSessions()
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refrescar")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                NavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
                    label = { Text("Panel") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(Screen.AdminTutors.route) },
                    icon = { Icon(Icons.Default.People, contentDescription = null) },
                    label = { Text("Tutores") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(Screen.AdminSessions.route) },
                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                    label = { Text("Sesiones") }
                )
            }
        }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                adminViewModel.getStats()
                adminViewModel.getTutors()
                adminViewModel.getStudents()
                adminViewModel.getSessions()
            },
            modifier = Modifier.padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Estadísticas Generales",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Bienvenido, Administrador",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                    IconButton(onClick = { authViewModel.logout() }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout", tint = Color.Red)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                val stats = statsState.data
                val tutors = tutorsState.data
                val students = studentsState.data
                val sessions = sessionsState.data

                // Lógica de conteo robusta
                fun Map<String, Double>.findKey(vararg keys: String): Int {
                    for (key in keys) {
                        val v = this[key] ?: this[key.lowercase()] ?: this[key.uppercase()]
                            ?: this["total_$key"] ?: this["total${key.replaceFirstChar { it.uppercase() }}"]
                        if (v != null) return v.toInt()
                    }
                    return 0
                }

                val sCount = if (!students.isNullOrEmpty()) students.size else stats?.findKey("students", "totalStudents") ?: 0
                val tCount = if (!tutors.isNullOrEmpty()) tutors.size else stats?.findKey("tutors", "totalTutors") ?: 0
                val sessCount = if (!sessions.isNullOrEmpty()) sessions.size else stats?.findKey("sessions", "totalSessions") ?: 0

                AdminStatsGrid(
                    studentCount = sCount,
                    tutorCount = tCount,
                    sessionCount = sessCount
                )

                Spacer(modifier = Modifier.height(32.dp))
                
                Text(
                    text = "Accesos Rápidos",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    AdminQuickActionCard(
                        title = "Gestionar Tutores",
                        subtitle = "Aprobar o desactivar perfiles",
                        icon = Icons.Default.SupervisorAccount,
                        color = MainPurple,
                        onClick = { navController.navigate(Screen.AdminTutors.route) }
                    )
                    AdminQuickActionCard(
                        title = "Gestionar Estudiantes",
                        subtitle = "Ver alumnos registrados",
                        icon = Icons.Default.Groups,
                        color = Color(0xFF2196F3),
                        onClick = { navController.navigate(Screen.AdminStudents.route) }
                    )
                    AdminQuickActionCard(
                        title = "Monitorear Sesiones",
                        subtitle = "Ver todas las asesorías",
                        icon = Icons.Default.History,
                        color = SoftGreen,
                        onClick = { navController.navigate(Screen.AdminSessions.route) }
                    )
                }
            }
        }
    }
}

@Composable
fun AdminStatsGrid(studentCount: Int, tutorCount: Int, sessionCount: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatCard(
                title = "Estudiantes",
                value = studentCount.toString(),
                icon = Icons.Default.Person,
                color = LightPurple,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Tutores",
                value = tutorCount.toString(),
                icon = Icons.Default.School,
                color = SoftOrange,
                modifier = Modifier.weight(1f)
            )
        }
        StatCard(
            title = "Sesiones Totales",
            value = sessionCount.toString(),
            icon = Icons.Default.Event,
            color = SoftGreen,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun StatCard(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, modifier: Modifier = Modifier) {
    TutoCard(modifier = modifier, backgroundColor = color.copy(alpha = 0.2f)) {
        Icon(icon, contentDescription = null, tint = Color.DarkGray, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(text = title, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
    }
}

@Composable
fun AdminQuickActionCard(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, onClick: () -> Unit) {
    TutoCard(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(48.dp).background(color.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
        }
    }
}
