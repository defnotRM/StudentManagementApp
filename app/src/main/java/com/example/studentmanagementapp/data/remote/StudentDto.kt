package com.example.studentmanagementapp.data.remote

import com.google.gson.annotations.SerializedName

data class StudentDto(
    val id: Int,
    @SerializedName("student_code")
    val studentCode: String,
    val name: String,
    val major: String
)

data class StudentRequest(
    @SerializedName("student_code")
    val studentCode: String,
    val name: String,
    val major: String
)

typealias StudentCreateRequest = StudentRequest
