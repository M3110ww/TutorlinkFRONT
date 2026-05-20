package com.tutorlink.app.data.remote

import com.tutorlink.app.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface TutoApiService {

    // ── Auth ──────────────────────────────────────────────────────────────────
    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    // ── Tutors ────────────────────────────────────────────────────────────────
    @GET("api/tutors")
    suspend fun getActiveTutors(): Response<List<TutorResponse>>

    @GET("api/tutors/all")
    suspend fun getAllTutors(
        @Header("Authorization") token: String? = null
    ): Response<List<TutorResponse>>

    @GET("api/tutors/{id}")
    suspend fun getTutorById(@Path("id") id: Long): Response<TutorResponse>

    @GET("api/tutors/search")
    suspend fun searchTutors(@Query("specialty") specialty: String): Response<List<TutorResponse>>

    /**
     * ADDED — resolves tutorId (tutors table) from userId (users table).
     * Requires adding to backend:
     *   @GetMapping("/user/{userId}")
     *   public ResponseEntity<TutorResponse> getByUserId(@PathVariable Long userId) {
     *     return ResponseEntity.ok(tutorService.findByUserId(userId));
     *   }
     */
    @GET("api/tutors/user/{userId}")
    suspend fun getTutorByUserId(
        @Path("userId") userId: Long,
        @Header("Authorization") token: String? = null
    ): Response<TutorResponse>

    @POST("api/tutors/user/{userId}")
    suspend fun registerTutor(
        @Path("userId") userId: Long,
        @Body request: TutorRequest,
        @Header("Authorization") token: String? = null
    ): Response<TutorResponse>

    @PUT("api/tutors/{id}")
    suspend fun updateTutor(
        @Path("id") id: Long,
        @Body request: TutorRequest
    ): Response<TutorResponse>

    @PATCH("api/tutors/{id}/status")
    suspend fun changeTutorStatus(
        @Path("id") id: Long,
        @Query("active") active: Boolean
    ): Response<TutorResponse>

    // ── Students ──────────────────────────────────────────────────────────────
    @GET("api/students/{id}")
    suspend fun getStudentById(@Path("id") id: Long): Response<StudentResponse>

    /**
     * ADDED — resolves studentId (students table) from userId (users table).
     * Requires adding to backend:
     *   @GetMapping("/user/{userId}")
     *   public ResponseEntity<StudentResponse> getByUserId(@PathVariable Long userId) {
     *     return ResponseEntity.ok(estudianteService.findByUserId(userId));
     *   }
     * The backend already has EstudianteRepository.findByUserId(userId) — just expose it.
     */
    @GET("api/students/user/{userId}")
    suspend fun getStudentByUserId(
        @Path("userId") userId: Long,
        @Header("Authorization") token: String? = null
    ): Response<StudentResponse>

    @GET("api/students")
    suspend fun getAllStudents(
        @Header("Authorization") token: String? = null
    ): Response<List<StudentResponse>>

    @POST("api/students/user/{userId}")
    suspend fun registerStudent(
        @Path("userId") userId: Long,
        @Body request: StudentRequest,
        @Header("Authorization") token: String? = null
    ): Response<StudentResponse>

    @PUT("api/students/{id}")
    suspend fun updateStudent(
        @Path("id") id: Long,
        @Body request: StudentRequest
    ): Response<StudentResponse>

    // ── Sessions ──────────────────────────────────────────────────────────────
    @GET("api/sessions/student/{studentId}")
    suspend fun getStudentSessions(@Path("studentId") studentId: Long): Response<List<SessionResponse>>

    @GET("api/sessions/tutor/{tutorId}")
    suspend fun getTutorSessions(@Path("tutorId") tutorId: Long): Response<List<SessionResponse>>

    @POST("api/sessions/student/{studentId}")
    suspend fun bookSession(
        @Path("studentId") studentId: Long,
        @Body request: SessionRequest
    ): Response<SessionResponse>

    @PATCH("api/sessions/{id}/confirm")
    suspend fun confirmSession(@Path("id") id: Long): Response<SessionResponse>

    @PATCH("api/sessions/{id}/cancel")
    suspend fun cancelSession(@Path("id") id: Long): Response<SessionResponse>

    @PATCH("api/sessions/{id}/complete")
    suspend fun completeSession(@Path("id") id: Long): Response<SessionResponse>

    // ── Availability ──────────────────────────────────────────────────────────
    @GET("api/availability/tutor/{tutorId}")
    suspend fun getTutorAvailability(@Path("tutorId") tutorId: Long): Response<List<AvailabilityResponse>>

    @GET("api/availability/tutor/{tutorId}/available")
    suspend fun getAvailableTutorSlots(@Path("tutorId") tutorId: Long): Response<List<AvailabilityResponse>>

    @POST("api/availability/tutor/{tutorId}")
    suspend fun addAvailability(
        @Path("tutorId") tutorId: Long,
        @Body request: AvailabilityRequest
    ): Response<AvailabilityResponse>

    @DELETE("api/availability/{id}")
    suspend fun deleteAvailability(@Path("id") id: Long): Response<Unit>

    // ── Reviews ───────────────────────────────────────────────────────────────
    @GET("api/reviews/tutor/{tutorId}")
    suspend fun getReviewsByTutor(@Path("tutorId") tutorId: Long): Response<List<ReviewResponse>>

    @POST("api/reviews/session/{sessionId}/student/{studentId}")
    suspend fun createReview(
        @Path("sessionId") sessionId: Long,
        @Path("studentId") studentId: Long,
        @Body request: ReviewRequest
    ): Response<ReviewResponse>

    // ── Admin ─────────────────────────────────────────────────────────────────
    // FIXED: Map<String, Double> — Gson deserializes JSON numbers as Double, not Long
    @GET("api/admin/stats")
    suspend fun getAdminStats(): Response<Map<String, Double>>

    @GET("api/admin/sessions")
    suspend fun getAdminSessions(): Response<List<SessionResponse>>

    @GET("api/admin/tutors")
    suspend fun getAdminTutors(): Response<List<TutorResponse>>

    @PATCH("api/admin/sessions/{id}/confirm")
    suspend fun adminConfirmSession(@Path("id") id: Long): Response<SessionResponse>

    @PATCH("api/admin/sessions/{id}/cancel")
    suspend fun adminCancelSession(@Path("id") id: Long): Response<SessionResponse>

    @PATCH("api/admin/tutors/{id}/status")
    suspend fun adminChangeTutorStatus(
        @Path("id") id: Long,
        @Query("active") active: Boolean
    ): Response<TutorResponse>
}