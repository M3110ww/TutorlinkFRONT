package com.tutorlink.app.view.student

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import com.tutorlink.app.viewmodel.student.TutorDetailViewModel
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.StarBorder
import com.tutorlink.app.data.remote.dto.AvailabilityResponse
import com.tutorlink.app.data.remote.dto.ReviewResponse
import kotlinx.coroutines.launch

import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TutorDetailScreen(
    tutorId: Long,
    navController: NavController,
    viewModel: TutorDetailViewModel = hiltViewModel()
) {
    val tutorState by viewModel.tutor.collectAsState()
    val availabilityState by viewModel.availability.collectAsState()
    val reviewsState by viewModel.reviews.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(tutorId) {
        viewModel.getTutorDetail(tutorId)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        scope.launch { 
                            snackbarHostState.showSnackbar("Función de marcadores próximamente") 
                        } 
                    }) {
                        Icon(Icons.Default.BookmarkBorder, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ExtraLightPurple)
            )
        },
        bottomBar = {
            if (tutorState is Resource.Success) {
                val tutor = tutorState.data!!
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 8.dp,
                    color = Color.White
                ) {
                    Row(
                        modifier = Modifier
                            .padding(24.dp)
                            .navigationBarsPadding(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Tarifa de sesión", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = String.format(Locale.US, "%,.0f", tutor.hourlyRate), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                                Text(text = " COP/hora", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                            }
                        }
                        TutoButton(
                            text = "Reservar esta sesión",
                            onClick = { navController.navigate(Screen.BookSession.createRoute(tutorId)) },
                            modifier = Modifier.weight(1.5f)
                        )
                    }
                }
            }
        }
    ) { padding ->
        when (tutorState) {
            is Resource.Idle, is Resource.Loading -> LoadingView()
            is Resource.Success -> {
                val tutor = tutorState.data!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White)
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Profile Header
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ExtraLightPurple)
                            .padding(bottom = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            TutoAvatar(name = tutor.tutorName, size = 100.dp, containerColor = Color.White)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = tutor.tutorName,
                                style = MaterialTheme.typography.displayLarge,
                                fontSize = 24.sp
                            )
                            Text(
                                text = tutor.specialty ?: "Sin especialidad",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.Gray
                            )
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            // Stats
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                DetailStatItem(String.format(Locale.US, "%.1f", tutor.averageRating), "Calificación")
                                DetailStatItem("-", "Sesiones")
                                DetailStatItem("N/A", "Exp.")
                            }
                        }
                    }

                    // Tabs
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color.White,
                        contentColor = MainPurple,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = MainPurple
                            )
                        },
                        divider = { HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f)) }
                    ) {
                        Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                            Text(text = "Sobre mí", modifier = Modifier.padding(16.dp), fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal)
                        }
                        Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                            Text(text = "Horario", modifier = Modifier.padding(16.dp), fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal)
                        }
                        Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }) {
                            Text(text = "Reseñas", modifier = Modifier.padding(16.dp), fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal)
                        }
                    }

                    Column(modifier = Modifier.padding(24.dp)) {
                        when (selectedTab) {
                            0 -> AboutTabContent(tutor.description)
                            1 -> AvailabilityTabContent(
                                availabilityState = availabilityState,
                                onRetry = { viewModel.getTutorAvailability(tutorId) }
                            )
                            2 -> ReviewsTabContent(reviewsState)
                        }
                        
                        Spacer(modifier = Modifier.height(100.dp)) // Extra space for bottom bar
                    }
                }
            }
            is Resource.Error -> ErrorView(message = tutorState.message ?: "Error desconocido", onRetry = { viewModel.getTutorDetail(tutorId) })
        }
    }
}

@Composable
fun AboutTabContent(description: String?) {
    Column {
        Text(
            text = "BIOGRAFÍA",
            style = MaterialTheme.typography.labelLarge,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = description ?: "Ingeniero enfocado en el desarrollo de software. Guío a los estudiantes a través de temas complejos utilizando ejemplos del mundo real.",
            style = MaterialTheme.typography.bodyLarge,
            lineHeight = 24.sp
        )
    }
}

@Composable
fun AvailabilityTabContent(
    availabilityState: Resource<List<AvailabilityResponse>>,
    onRetry: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "HORARIOS DISPONIBLES",
                style = MaterialTheme.typography.labelLarge,
                color = Color.Gray
            )
            StatusBadge(text = "Semanal", containerColor = SoftGreen, contentColor = TextGreen)
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (availabilityState) {
            is Resource.Idle, is Resource.Loading -> Box(modifier = Modifier.height(80.dp)) { LoadingView() }
            is Resource.Success -> {
                val slots = availabilityState.data ?: emptyList()
                if (slots.isEmpty()) {
                    Text("No hay horarios configurados.", color = Color.Gray, modifier = Modifier.padding(top = 8.dp))
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        slots.forEach { slot ->
                            TutoCard(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val dayName = when(slot.dayOfWeek ?: 0) {
                                        1, 8 -> "Lunes"
                                        2 -> "Martes"
                                        3 -> "Miércoles"
                                        4 -> "Jueves"
                                        5 -> "Viernes"
                                        6 -> "Sábado"
                                        0, 7 -> "Domingo"
                                        else -> "Horario"
                                    }
                                    Text(dayName, fontWeight = FontWeight.Bold)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        val start = slot.startTime?.take(5) ?: "00:00"
                                        val end = slot.endTime?.take(5) ?: "00:00"
                                        Text("$start - $end")
                                        if (slot.occupied == true) {
                                            Spacer(Modifier.width(8.dp))
                                            StatusBadge(text = "Ocupado", containerColor = Color(0xFFFFEBEE), contentColor = Color.Red)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            is Resource.Error -> ErrorView(
                message = availabilityState.message ?: "Error al cargar horarios",
                onRetry = onRetry
            )
        }
    }
}

@Composable
fun ReviewsTabContent(reviewsState: Resource<List<ReviewResponse>>) {
    Column {
        Text(
            text = "OPINIONES DE ESTUDIANTES",
            style = MaterialTheme.typography.labelLarge,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(16.dp))

        when (reviewsState) {
            is Resource.Idle, is Resource.Loading -> LoadingView()
            is Resource.Success -> {
                val reviews = reviewsState.data ?: emptyList()
                if (reviews.isEmpty()) {
                    Text("Este tutor aún no tiene reseñas.", color = Color.Gray)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        reviews.forEach { review ->
                            ReviewItem(review)
                        }
                    }
                }
            }
            is Resource.Error -> Text("Error al cargar reseñas", color = Color.Red)
        }
    }
}

@Composable
fun ReviewItem(review: ReviewResponse) {
    TutoCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row {
                    repeat(5) { index ->
                        Icon(
                            imageVector = if (index < review.rating) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = null,
                            tint = if (index < review.rating) Color(0xFFFFB300) else Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Text(
                    text = review.createdAt?.substringBefore("T") ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = review.comment ?: "Sin comentarios.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun DetailStatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
    }
}

@Composable
fun TimeSlotChip(time: String, isSelected: Boolean, modifier: Modifier = Modifier) {
    Surface(
        color = if (isSelected) MainPurple else Color.LightGray.copy(alpha = 0.2f),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Box(modifier = Modifier.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
            Text(
                text = time,
                style = MaterialTheme.typography.labelLarge,
                color = if (isSelected) Color.White else Color.Black,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
