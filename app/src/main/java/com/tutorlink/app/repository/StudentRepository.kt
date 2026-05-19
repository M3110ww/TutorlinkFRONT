package com.tutorlink.app.repository

import com.tutorlink.app.data.remote.TutoApiService
import com.tutorlink.app.data.remote.dto.StudentRequest
import com.tutorlink.app.data.remote.dto.StudentResponse
import com.tutorlink.app.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StudentRepository @Inject constructor(private val api: TutoApiService) {
    fun getStudentById(id: Long): Flow<Resource<StudentResponse>>              = flowSafeCall { api.getStudentById(id) }
    fun registerStudent(userId: Long, req: StudentRequest): Flow<Resource<StudentResponse>> = flowSafeCall { api.registerStudent(userId, req) }
    fun updateStudent(id: Long, req: StudentRequest): Flow<Resource<StudentResponse>>       = flowSafeCall { api.updateStudent(id, req) }
}