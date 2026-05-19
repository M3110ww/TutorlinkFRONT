package com.tutorlink.app.utils

import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tutorlink.app.navigation.Screen
import com.tutorlink.app.viewmodel.AuthViewModel

/**
 * Wraps content that should only be visible to a specific role.
 * If the current user's role doesn't match, navigates back to login.
 */
@Composable
fun RoleGuard(
    requiredRole: String,
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel(),
    content: @Composable () -> Unit
) {
    val userRole by viewModel.userRole.collectAsState()
    val isSessionReady by viewModel.isSessionReady.collectAsState()

    LaunchedEffect(isSessionReady, userRole) {
        if (!isSessionReady) return@LaunchedEffect
        if (userRole != requiredRole) {
            navController.navigate(Screen.Login.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    if (userRole == requiredRole) {
        content()
    }
}
