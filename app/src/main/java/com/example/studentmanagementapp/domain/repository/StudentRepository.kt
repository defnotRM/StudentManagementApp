package com.example.studentmanagementapp.domain.repository

import com.example.studentmanagementapp.domain.model.Student
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface StudentRepository {
    val onlineState: StateFlow<Boolean>
    var lastFetchWasOnline: Boolean
    fun observeStudents(): Flow<List<Student>>
    suspend fun addStudent(student: Student)
    suspend fun syncPending()
    suspend fun getStudents(): List<Student>
    suspend fun updateStudent(student: Student)
    suspend fun deleteStudent(student: Student)
}
