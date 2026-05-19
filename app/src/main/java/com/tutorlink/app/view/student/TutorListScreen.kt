package com.tutorlink.app.view.student

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
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
import com.tutorlink.app.data.remote.dto.TutorResponse
import com.tutorlink.app.navigation.Screen
import com.tutorlink.app.ui.theme.*
import com.tutorlink.app.utils.Resource
import com.tutorlink.app.view.common.EmptyStateView
import com.tutorlink.app.view.common.ErrorView
import com.tutorlink.app.view.common.LoadingView
import com.tutorlink.app.view.components.TutoAvatar
import com.tutorlink.app.view.components.TutoCard
import com.tutorlink.app.view.components.TutoSearchBar
import com.tutorlink.app.viewmodel.student.TutorListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TutorListScreen(
    navController: NavController,
    viewModel: TutorListViewModel = hiltViewModel()
) {
    val tutorsState by viewModel.tutors.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(Color.White).padding(bottom = 8.dp)) {
                CenterAlignedTopAppBar(
                    title = { Text("Encuentra tu Tutor", fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
                )
                TutoSearchBar(
                    value = searchQuery,
                    onValueChange = { 
                        searchQuery = it
                        viewModel.searchTutors(it)
                    },
                    modifier = Modifier.padding(horizontal = 20.dp),
                    placeholder = "Ej: Matemáticas, Java, Física..."
                )
            }
        }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = tutorsState is Resource.Loading,
            onRefresh = { viewModel.getTutors() },
            modifier = Modifier.padding(padding)
        ) {
            when (tutorsState) {
                is Resource.Idle, is Resource.Loading -> LoadingView()
                is Resource.Success -> {
                    val list = tutorsState.data ?: emptyList()
                    if (list.isEmpty()) {
                        EmptyStateView(
                            icon = Icons.Default.SearchOff,
                            title = "Sin resultados",
                            subtitle = "No encontramos tutores con esa especialidad",
                            actionLabel = "Limpiar búsqueda",
                            onAction = { 
                                searchQuery = ""
                                viewModel.getTutors() 
                            }
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(list) { tutor ->
                                TutorItem(
                                    tutor = tutor,
                                    onClick = { navController.navigate(Screen.TutorDetail.createRoute(tutor.id)) }
                                )
                            }
                        }
                    }
                }
                is Resource.Error -> ErrorView(
                    message = tutorsState.message ?: "Error al cargar tutores",
                    onRetry = { viewModel.getTutors() }
                )
            }
        }
    }
}

@Composable
fun TutorItem(tutor: TutorResponse, onClick: () -> Unit) {
    TutoCard(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TutoAvatar(name = tutor.tutorName, size = 60.dp)
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(tutor.tutorName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(tutor.specialty ?: "Tutor", color = MainPurple, style = MaterialTheme.typography.bodyMedium)
                
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    Icon(Icons.Default.Star, null, tint = Color(0xFFFFB300), modifier = Modifier.size(16.dp))
                    Text(
                        text = " ${String.format(java.util.Locale.US, "%.1f", tutor.averageRating)}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "${String.format(java.util.Locale.US, "%,.0f", tutor.hourlyRate)} COP/h",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
            Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray)
        }
    }
}
