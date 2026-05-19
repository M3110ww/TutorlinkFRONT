package com.tutorlink.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.tutorlink.app.utils.RoleGuard
import com.tutorlink.app.view.admin.AdminDashboardScreen
import com.tutorlink.app.view.admin.AdminSessionsScreen
import com.tutorlink.app.view.admin.AdminStudentsScreen
import com.tutorlink.app.view.admin.AdminTutorsScreen
import com.tutorlink.app.view.auth.LoginScreen
import com.tutorlink.app.view.auth.RegisterScreen
import com.tutorlink.app.view.auth.SplashScreen
import com.tutorlink.app.view.common.ProfileScreen
import com.tutorlink.app.view.student.BookSessionScreen
import com.tutorlink.app.view.student.StudentHomeScreen
import com.tutorlink.app.view.student.StudentSessionsScreen
import com.tutorlink.app.view.student.TutorDetailScreen
import com.tutorlink.app.view.student.TutorListScreen
import com.tutorlink.app.view.tutor.TutorAvailabilityScreen
import com.tutorlink.app.view.tutor.TutorDashboardScreen
import com.tutorlink.app.view.tutor.TutorProfileScreen
import com.tutorlink.app.viewmodel.AuthViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Splash.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(navController)
        }
        
        composable(Screen.Login.route) {
            LoginScreen(navController)
        }
        
        composable(Screen.Register.route) {
            RegisterScreen(navController)
        }

        // Student Screens
        composable(Screen.StudentHome.route) {
            RoleGuard("STUDENT", navController) {
                StudentHomeScreen(navController)
            }
        }

        composable(Screen.TutorList.route) {
            RoleGuard("STUDENT", navController) {
                TutorListScreen(navController)
            }
        }

        composable(
            route = Screen.TutorDetail.route,
            arguments = listOf(
                androidx.navigation.navArgument("tutorId") {
                    type = androidx.navigation.NavType.LongType
                }
            )
        ) { backStackEntry ->
            RoleGuard("STUDENT", navController) {
                val tutorId = backStackEntry.arguments?.getLong("tutorId") ?: 0L
                TutorDetailScreen(tutorId, navController)
            }
        }

        composable(
            route = Screen.BookSession.route,
            arguments = listOf(
                androidx.navigation.navArgument("tutorId") {
                    type = androidx.navigation.NavType.LongType
                }
            )
        ) { backStackEntry ->
            RoleGuard("STUDENT", navController) {
                val tutorId = backStackEntry.arguments?.getLong("tutorId") ?: 0L
                BookSessionScreen(tutorId, navController)
            }
        }

        composable(Screen.StudentSessions.route) {
            RoleGuard("STUDENT", navController) {
                StudentSessionsScreen(navController)
            }
        }

        composable(Screen.StudentProfile.route) {
            RoleGuard("STUDENT", navController) {
                ProfileScreen(navController)
            }
        }

        // Tutor Screens
        composable(Screen.TutorDashboard.route) {
            RoleGuard("TUTOR", navController) {
                TutorDashboardScreen(navController)
            }
        }

        composable(Screen.TutorAvailability.route) {
            RoleGuard("TUTOR", navController) {
                TutorAvailabilityScreen(navController)
            }
        }

        composable(Screen.TutorProfile.route) {
            RoleGuard("TUTOR", navController) {
                ProfileScreen(navController)
            }
        }

        // Admin Screens
        composable(Screen.AdminDashboard.route) {
            RoleGuard("ADMIN", navController) {
                AdminDashboardScreen(navController)
            }
        }

        composable(Screen.AdminTutors.route) {
            RoleGuard("ADMIN", navController) {
                AdminTutorsScreen(navController)
            }
        }

        composable(Screen.AdminSessions.route) {
            RoleGuard("ADMIN", navController) {
                AdminSessionsScreen(navController)
            }
        }

        composable(Screen.AdminStudents.route) {
            RoleGuard("ADMIN", navController) {
                AdminStudentsScreen(navController)
            }
        }
    }
}
