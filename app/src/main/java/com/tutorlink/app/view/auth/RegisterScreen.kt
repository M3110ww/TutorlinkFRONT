package com.tutorlink.app.view.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
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
import com.tutorlink.app.data.remote.dto.RegisterRequest
import com.tutorlink.app.data.remote.dto.UserRole
import com.tutorlink.app.navigation.Screen
import com.tutorlink.app.ui.theme.ExtraLightPurple
import com.tutorlink.app.utils.Resource
import com.tutorlink.app.view.components.RoleSelector
import com.tutorlink.app.view.components.TutoButton
import com.tutorlink.app.view.components.TutoTextField
import com.tutorlink.app.viewmodel.AuthViewModel
import kotlinx.coroutines.delay

@Composable
fun RegisterScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var selectedRoleStr by remember { mutableStateOf("Estudiante") }
    
    val authState by viewModel.authState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(key1 = authState) {
        if (authState is Resource.Success) {
            snackbarHostState.showSnackbar("¡Registro exitoso! Bienvenido a TutorLink")
            delay(1500)
            navController.navigate(Screen.Splash.route) {
                popUpTo(Screen.Register.route) { inclusive = true }
            }
        }
    }

    val scrollState = rememberScrollState()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White)
                .verticalScroll(scrollState)
        ) {
            // Header Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ExtraLightPurple)
                    .padding(top = 80.dp, bottom = 48.dp, start = 32.dp, end = 32.dp)
            ) {
                Column {
                    Text(
                        text = "Crea tu cuenta",
                        style = MaterialTheme.typography.displayLarge,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = "Únete a TutorLink hoy mismo",
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
                RoleSelector(
                    selectedRole = selectedRoleStr,
                    onRoleSelected = { selectedRoleStr = it }
                )

                Spacer(modifier = Modifier.height(32.dp))

                TutoTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "NOMBRE COMPLETO",
                    leadingIcon = { Icon(Icons.Default.Person, null, tint = Color.Gray.copy(alpha = 0.5f)) },
                    placeholder = { Text("Tu nombre") }
                )

                Spacer(modifier = Modifier.height(20.dp))

                TutoTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "CORREO ELECTRÓNICO",
                    leadingIcon = { Icon(Icons.Default.Email, null, tint = Color.Gray.copy(alpha = 0.5f)) },
                    placeholder = { Text("Correo electrónico") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Email)
                )

                Spacer(modifier = Modifier.height(20.dp))

                TutoTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "CONTRASEÑA",
                    leadingIcon = { Icon(Icons.Default.Lock, null, tint = Color.Gray.copy(alpha = 0.5f)) },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null,
                                tint = Color.Gray
                            )
                        }
                    },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Password)
                )

                Spacer(modifier = Modifier.height(32.dp))

                TutoButton(
                    text = "Registrarse",
                    onClick = {
                        val role = if (selectedRoleStr == "Estudiante") UserRole.STUDENT else UserRole.TUTOR
                        viewModel.register(RegisterRequest(name, email, password, role))
                    },
                    isLoading = authState is Resource.Loading
                )

                if (authState is Resource.Error) {
                    Text(
                        text = (authState as Resource.Error).message ?: "Error al conectar con el servidor",
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("¿Ya tienes cuenta? ", color = Color.Gray)
                    TextButton(onClick = { navController.popBackStack() }) {
                        Text("Inicia sesión", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
