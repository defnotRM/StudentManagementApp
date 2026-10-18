package com.example.studentmanagementapp.data.repository

import com.example.studentmanagementapp.data.local.StudentDao
import com.example.studentmanagementapp.data.local.StudentEntity
import com.example.studentmanagementapp.data.remote.RetrofitClient
import com.example.studentmanagementapp.data.remote.StudentApi
import com.example.studentmanagementapp.data.remote.StudentDto
import com.example.studentmanagementapp.data.remote.StudentRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class StudentRepository(
    private val dao: StudentDao,
    private val api: StudentApi = RetrofitClient.api
) {
    private val syncMutex = Mutex()

    private val _onlineState = MutableStateFlow(false)
    val onlineState: StateFlow<Boolean> = _onlineState.asStateFlow()

    var lastFetchWasOnline: Boolean = false
        private set(value) {
            field = value
            _onlineState.value = value
        }

    fun observeStudents(): Flow<List<StudentEntity>> {
        return dao.observeStudents()
    }

    suspend fun getLocalStudents(): List<StudentEntity> {
        return dao.getAllStudents()
    }

    suspend fun getRemoteStudents(): List<StudentDto> {
        return api.getStudents()
    }

    suspend fun addStudent(
        student: StudentEntity
    ) {
        dao.insertStudent(
            student.copy(syncStatus = "PENDING")
        )
        dao.removeDuplicateStudents()
    }

    suspend fun addStudent(
        studentCode: String,
        name: String,
        major: String
    ) {
        addStudent(
            StudentEntity(
                studentCode = studentCode,
                name = name,
                major = major,
                syncStatus = "PENDING"
            )
        )
    }

    suspend fun syncPending() {
        syncMutex.withLock {
            dao.removeDuplicateStudents()
            val pending =
                dao.getPendingStudents()

            for (student in pending) {
                val response = api.createStudent(
                    StudentRequest(
                        studentCode = student.studentCode,
                        name = student.name,
                        major = student.major
                    )
                )

                dao.updateStudent(
                    student.copy(
                        serverId = response.id,
                        syncStatus = "SYNCED",
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }
            dao.removeDuplicateStudents()
            lastFetchWasOnline = true
        }
    }

    suspend fun getStudents(): List<StudentEntity> {
        return syncMutex.withLock {
            dao.removeDuplicateStudents()
            try {
                val remoteStudents = api.getStudents()
                val pendingCodes = dao.getPendingStudents().map { it.studentCode }.toSet()

                val syncedStudents = remoteStudents
                    .filter { it.studentCode !in pendingCodes }
                    .distinctBy { it.studentCode }
                    .map {
                        StudentEntity(
                            serverId = it.id,
                            studentCode = it.studentCode,
                            name = it.name,
                            major = it.major,
                            syncStatus = "SYNCED"
                        )
                    }

                dao.deleteSyncedStudents()
                dao.insertAll(syncedStudents)
                dao.removeDuplicateStudents()

                lastFetchWasOnline = true
                dao.getAllStudents()
            } catch (e: Exception) {
                lastFetchWasOnline = false
                dao.removeDuplicateStudents()
                val cached = dao.getAllStudents()
                if (cached.isEmpty()) {
                    val seedData = listOf(
                        StudentEntity(serverId = 1, studentCode = "65001", name = "Somchai Jaidee", major = "Computer Science", syncStatus = "SYNCED"),
                        StudentEntity(serverId = 2, studentCode = "65002", name = "Somsri Dee", major = "Information Technology", syncStatus = "SYNCED"),
                        StudentEntity(serverId = 3, studentCode = "65003", name = "Anan Kong", major = "Data Science", syncStatus = "SYNCED")
                    )
                    dao.insertAll(seedData)
                    dao.removeDuplicateStudents()
                    dao.getAllStudents()
                } else {
                    cached
                }
            }
        }
    }

    suspend fun updateStudent(student: StudentEntity) {
        try {
            val targetId = student.serverId ?: student.localId
            val updatedDto = api.updateStudent(
                id = targetId,
                student = StudentRequest(
                    studentCode = student.studentCode,
                    name = student.name,
                    major = student.major
                )
            )
            dao.updateStudent(
                student.copy(
                    serverId = updatedDto.id,
                    syncStatus = "SYNCED",
                    updatedAt = System.currentTimeMillis()
                )
            )
            lastFetchWasOnline = true
        } catch (e: Exception) {
            lastFetchWasOnline = false
            dao.updateStudent(
                student.copy(
                    syncStatus = "PENDING",
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun deleteStudent(student: StudentEntity) {
        try {
            val targetId = student.serverId
            if (targetId != null) {
                api.deleteStudent(targetId)
            }
            dao.deleteStudent(student)
            lastFetchWasOnline = true
        } catch (e: Exception) {
            lastFetchWasOnline = false
            dao.deleteStudent(student)
        }
    }
}
