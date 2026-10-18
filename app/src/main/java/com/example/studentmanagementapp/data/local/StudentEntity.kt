package com.example.studentmanagementapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "students")
data class StudentEntity(
    @PrimaryKey(autoGenerate = true)
    val localId: Int = 0,
    val serverId: Int? = null,
    val studentCode: String,
    val name: String,
    val major: String,
    val syncStatus: String = "PENDING",
    val updatedAt: Long =
        System.currentTimeMillis()
)
