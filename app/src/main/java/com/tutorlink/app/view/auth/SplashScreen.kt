package com.tutorlink.app.view.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tutorlink.app.R
import com.tutorlink.app.navigation.Screen
import com.tutorlink.app.ui.theme.MainPurple
import com.tutorlink.app.viewmodel.AuthViewModel

@Composable
fun SplashScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val userRole by viewModel.userRole.collectAsState()
    val isSessionReady by viewModel.isSessionReady.collectAsState()

    LaunchedEffect(key1 = isSessionReady, key2 = userRole) {
        if (isSessionReady && userRole != null) {
            when (userRole) {
                "STUDENT" -> navController.navigate(Screen.StudentHome.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
                "TUTOR" -> navController.navigate(Screen.TutorDashboard.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
                "ADMIN" -> navController.navigate(Screen.AdminDashboard.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            }
        }
    }

    val gradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF1A0A3A), Color(0xFF2D1F50))
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            // Logo area
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(MainPurple, Color(0xFF9C27B0))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Assuming there's a logo resource, if not, use a simple text or icon
                Text(
                    text = "TL",
                    color = Color.White,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "TutorLink",
                color = Color.White,
                fontSize = 40.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Conecta con el tutor ideal para cada materia",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            if (!isSessionReady) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            }

            AnimatedVisibility(
                visible = isSessionReady && userRole == null,
                enter = fadeIn()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Button(
                        onClick = { navController.navigate(Screen.Register.route) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFF1A0A3A)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Empezar", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = { navController.navigate(Screen.Login.route) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Iniciar sesión en mi cuenta", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}
