package com.example.studentmanagementapp.data.repository

import com.example.studentmanagementapp.data.local.StudentDao
import com.example.studentmanagementapp.data.local.StudentEntity
import com.example.studentmanagementapp.data.remote.StudentApi
import com.example.studentmanagementapp.data.remote.StudentRequest
import com.example.studentmanagementapp.domain.model.Student
import com.example.studentmanagementapp.domain.repository.StudentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StudentRepositoryImpl @Inject constructor(
    private val dao: StudentDao,
    private val api: StudentApi
) : StudentRepository {

    private val syncMutex = Mutex()

    private val _onlineState = MutableStateFlow(false)
    override val onlineState: StateFlow<Boolean> = _onlineState.asStateFlow()

    override var lastFetchWasOnline: Boolean = false
        set(value) {
            field = value
            _onlineState.value = value
        }

    override fun observeStudents(): Flow<List<Student>> {
        return dao.observeStudents().map { entities ->
            entities.map { entity ->
                Student(
                    id = entity.serverId ?: entity.localId,
                    studentCode = entity.studentCode,
                    name = entity.name,
                    major = entity.major,
                    syncStatus = entity.syncStatus
                )
            }
        }
    }

    override suspend fun addStudent(student: Student) {
        dao.insertStudent(
            StudentEntity(
                studentCode = student.studentCode,
                name = student.name,
                major = student.major,
                syncStatus = "PENDING"
            )
        )
        dao.removeDuplicateStudents()
    }

    override suspend fun syncPending() {
        syncMutex.withLock {
            dao.removeDuplicateStudents()
            val pending = dao.getPendingStudents()

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

    override suspend fun getStudents(): List<Student> {
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
                syncedStudents.map {
                    Student(
                        id = it.serverId ?: it.localId,
                        studentCode = it.studentCode,
                        name = it.name,
                        major = it.major,
                        syncStatus = it.syncStatus
                    )
                }
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
                    seedData.map {
                        Student(
                            id = it.serverId ?: it.localId,
                            studentCode = it.studentCode,
                            name = it.name,
                            major = it.major,
                            syncStatus = it.syncStatus
                        )
                    }
                } else {
                    cached.map {
                        Student(
                            id = it.serverId ?: it.localId,
                            studentCode = it.studentCode,
                            name = it.name,
                            major = it.major,
                            syncStatus = it.syncStatus
                        )
                    }
                }
            }
        }
    }

    override suspend fun updateStudent(student: Student) {
        try {
            val updatedDto = api.updateStudent(
                id = student.id,
                student = StudentRequest(
                    studentCode = student.studentCode,
                    name = student.name,
                    major = student.major
                )
            )
            val current = dao.getAllStudents().firstOrNull { it.studentCode == student.studentCode }
            if (current != null) {
                dao.updateStudent(
                    current.copy(
                        serverId = updatedDto.id,
                        name = updatedDto.name,
                        major = updatedDto.major,
                        syncStatus = "SYNCED",
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }
            lastFetchWasOnline = true
        } catch (e: Exception) {
            lastFetchWasOnline = false
        }
    }

    override suspend fun deleteStudent(student: Student) {
        try {
            if (student.id > 0) {
                api.deleteStudent(student.id)
            }
            val current = dao.getAllStudents().firstOrNull { it.studentCode == student.studentCode }
            if (current != null) {
                dao.deleteStudent(current)
            }
            lastFetchWasOnline = true
        } catch (e: Exception) {
            lastFetchWasOnline = false
            val current = dao.getAllStudents().firstOrNull { it.studentCode == student.studentCode }
            if (current != null) {
                dao.deleteStudent(current)
            }
        }
    }
}
