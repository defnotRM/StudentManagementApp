package com.example.studentmanagementapp.data.repository

import com.example.studentmanagementapp.data.local.StudentDao
import com.example.studentmanagementapp.data.remote.RetrofitClient
import com.example.studentmanagementapp.data.remote.StudentApi
import com.example.studentmanagementapp.model.Student

class StudentRepository(
    private val dao: StudentDao,
    private val api: StudentApi = RetrofitClient.api
) {
    suspend fun getLocalStudents(): List<Student> {
        return dao.getAllStudents()
    }

    suspend fun getRemoteStudents(): List<Student> {
        return api.getStudents()
    }

    // Offline-First Logic (STEP 23)
    suspend fun getStudents(): List<Student> {
        return try {
            val remoteStudents = api.getStudents()
            remoteStudents.forEach {
                dao.insertStudent(it)
            }
            dao.getAllStudents()
        } catch (e: Exception) {
            // If network fails (Offline mode), fallback to Room Database
            val local = dao.getAllStudents()
            if (local.isEmpty()) {
                // Initial seed data as defined in lab sheet (STEP 3 & STEP 5)
                val initialStudents = listOf(
                    Student(id = 1, studentCode = "65010001", name = "Somchai", major = "Computer Science"),
                    Student(id = 2, studentCode = "65010002", name = "Suda", major = "Information Technology"),
                    Student(id = 3, studentCode = "65010003", name = "Mana", major = "Digital Technology")
                )
                dao.insertStudents(initialStudents)
                initialStudents
            } else {
                local
            }
        }
    }

    suspend fun addStudent(student: Student) {
        dao.insertStudent(student)
    }

    suspend fun updateStudent(student: Student) {
        dao.updateStudent(student)
    }

    suspend fun deleteStudent(student: Student) {
        dao.deleteStudent(student)
    }
}
