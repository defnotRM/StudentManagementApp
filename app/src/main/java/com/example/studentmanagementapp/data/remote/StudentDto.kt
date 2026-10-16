package com.example.studentmanagementapp.data.remote

data class StudentDto(
    val id: Int,
    val student_code: String,
    val name: String,
    val major: String
)

data class StudentCreateRequest(
    val student_code: String,
    val name: String,
    val major: String
)
