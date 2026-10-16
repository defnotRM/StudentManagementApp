package com.example.studentmanagementapp.data.repository

import com.example.studentmanagementapp.data.local.StudentDao
import com.example.studentmanagementapp.data.remote.RetrofitClient
import com.example.studentmanagementapp.data.remote.StudentApi
import com.example.studentmanagementapp.data.remote.StudentCreateRequest
import com.example.studentmanagementapp.data.remote.StudentDto
import com.example.studentmanagementapp.model.Student

class StudentRepository(
    private val dao: StudentDao,
    private val api: StudentApi = RetrofitClient.api
) {
    var lastFetchWasOnline: Boolean = false
        private set

    suspend fun getLocalStudents(): List<Student> {
        return dao.getAllStudents()
    }

    suspend fun getRemoteStudents(): List<StudentDto> {
        return api.getStudents()
    }

    suspend fun getStudents(): List<Student> {
        return try {
            val remoteStudents = api.getStudents()

            val localStudents = remoteStudents.map {
                Student(
                    id = it.id,
                    studentCode = it.student_code,
                    name = it.name,
                    major = it.major
                )
            }

            dao.deleteAll()
            dao.insertAll(localStudents)

            lastFetchWasOnline = true
            localStudents
        } catch (e: Exception) {
            lastFetchWasOnline = false
            val cached = dao.getAllStudents()
            if (cached.isEmpty()) {
                val seedData = listOf(
                    Student(id = 1, studentCode = "65001", name = "Somchai Jaidee", major = "Computer Science"),
                    Student(id = 2, studentCode = "65002", name = "Somsri Dee", major = "Information Technology"),
                    Student(id = 3, studentCode = "65003", name = "Anan Kong", major = "Data Science")
                )
                dao.insertAll(seedData)
                seedData
            } else {
                cached
            }
        }
    }

    suspend fun addStudent(studentCode: String, name: String, major: String) {
        try {
            val createdDto = api.createStudent(
                StudentCreateRequest(
                    student_code = studentCode,
                    name = name,
                    major = major
                )
            )
            dao.insertStudent(
                Student(
                    id = createdDto.id,
                    studentCode = createdDto.student_code,
                    name = createdDto.name,
                    major = createdDto.major
                )
            )
            lastFetchWasOnline = true
        } catch (e: Exception) {
            lastFetchWasOnline = false
            dao.insertStudent(
                Student(
                    studentCode = studentCode,
                    name = name,
                    major = major
                )
            )
        }
    }

    suspend fun updateStudent(student: Student) {
        try {
            val updatedDto = api.updateStudent(
                id = student.id,
                student = StudentCreateRequest(
                    student_code = student.studentCode,
                    name = student.name,
                    major = student.major
                )
            )
            dao.updateStudent(
                Student(
                    id = updatedDto.id,
                    studentCode = updatedDto.student_code,
                    name = updatedDto.name,
                    major = updatedDto.major
                )
            )
            lastFetchWasOnline = true
        } catch (e: Exception) {
            lastFetchWasOnline = false
            dao.updateStudent(student)
        }
    }

    suspend fun deleteStudent(student: Student) {
        try {
            api.deleteStudent(student.id)
            dao.deleteStudent(student)
            lastFetchWasOnline = true
        } catch (e: Exception) {
            lastFetchWasOnline = false
            dao.deleteStudent(student)
        }
    }
}
