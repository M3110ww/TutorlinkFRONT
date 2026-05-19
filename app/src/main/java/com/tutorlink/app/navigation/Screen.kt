package com.tutorlink.app.navigation

sealed class Screen(val route: String) {
    // Auth
    object Login : Screen("login")
    object Register : Screen("register")
    object Splash : Screen("splash")

    // Student
    object StudentHome : Screen("student_home")
    object TutorList : Screen("tutor_list")
    object TutorDetail : Screen("tutor_detail/{tutorId}") {
        fun createRoute(tutorId: Long) = "tutor_detail/$tutorId"
    }
    object BookSession : Screen("book_session/{tutorId}") {
        fun createRoute(tutorId: Long) = "book_session/$tutorId"
    }
    object StudentSessions : Screen("student_sessions")
    object StudentProfile : Screen("student_profile")

    // Tutor
    object TutorDashboard : Screen("tutor_dashboard")
    object TutorAvailability : Screen("tutor_availability")
    object TutorRequests : Screen("tutor_requests")
    object TutorHistory : Screen("tutor_history")
    object TutorProfile : Screen("tutor_profile")

    // Admin
    object AdminDashboard : Screen("admin_dashboard")
    object AdminTutors : Screen("admin_tutors")
    object AdminSessions : Screen("admin_sessions")
    object AdminStudents : Screen("admin_students")
}
