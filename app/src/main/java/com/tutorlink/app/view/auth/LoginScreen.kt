package com.tutorlink.app.view.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tutorlink.app.navigation.Screen
import com.tutorlink.app.ui.theme.ExtraLightPurple
import com.tutorlink.app.ui.theme.MainPurple
import com.tutorlink.app.utils.Resource
import com.tutorlink.app.view.components.RoleSelector
import com.tutorlink.app.view.components.TutoButton
import com.tutorlink.app.view.components.TutoTextField
import com.tutorlink.app.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    
    val authState by viewModel.authState.collectAsState()

    LaunchedEffect(key1 = authState) {
        if (authState is Resource.Success) {
            navController.navigate(Screen.Splash.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
        }
    }
    
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(scrollState)
    ) {
        // Header Section - Matching Mockup 02
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(ExtraLightPurple)
                .padding(top = 80.dp, bottom = 48.dp, start = 32.dp, end = 32.dp)
        ) {
            Column {
                IconButton(
                    onClick = { /* Back */ },
                    modifier = Modifier.offset(x = (-12).dp, y = (-40).dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = "Bienvenido de nuevo",
                    style = MaterialTheme.typography.displayLarge,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "Inicia sesión para continuar",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray
                )
            }
        }

        // Form Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TutoTextField(
                value = email,
                onValueChange = { email = it },
                label = "CORREO ELECTRÓNICO",
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color.Gray.copy(alpha = 0.5f)) },
                placeholder = { Text("Correo electrónico", color = Color.Gray.copy(alpha = 0.5f)) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            TutoTextField(
                value = password,
                onValueChange = { password = it },
                label = "CONTRASEÑA",
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Gray.copy(alpha = 0.5f)) },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = Color.Gray
                        )
                    }
                }
            )

            TextButton(
                onClick = { /* Forgot password */ },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(
                    "¿Olvidaste tu contraseña?",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Black,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            TutoButton(
                text = "Iniciar sesión",
                onClick = { viewModel.login(email, password) },
                isLoading = authState is Resource.Loading,
                containerColor = Color.Black // Mockup shows dark button for login
            )

            if (authState is Resource.Error) {
                Text(
                    text = (authState as Resource.Error).message ?: "Error desconocido",
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("¿No tienes cuenta? ", color = Color.Gray)
                TextButton(onClick = { navController.navigate(Screen.Register.route) }) {
                    Text("Regístrate aquí", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun SocialButton(text: String, icon: @Composable (() -> Unit)?) {
    OutlinedButton(
        onClick = { },
        modifier = Modifier.fillMaxWidth().height(56.dp).padding(4.dp), // Adjusting weight by wrapping in weight modifier elsewhere
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Icon would go here
            Text(text, color = Color.Black, fontWeight = FontWeight.SemiBold)
        }
    }
}

// Fixed SocialButton usage to include weight
@Composable
fun ColumnScope.SocialButtonRow(text1: String, text2: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(modifier = Modifier.weight(1f)) { SocialButton(text1, null) }
        Box(modifier = Modifier.weight(1f)) { SocialButton(text2, null) }
    }
}


// Added placeholder since it was missing in the TutoTextField definition
// REMOVED local TutoTextField as it is now in TutoComponents.kt
