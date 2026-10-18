package com.example.studentmanagementapp.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.studentmanagementapp.data.local.StudentEntity
import com.example.studentmanagementapp.data.repository.StudentRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StudentViewModel(
    private val repository: StudentRepository
) : ViewModel() {

    val students: StateFlow<List<StudentEntity>> =
        repository.observeStudents()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    var searchQuery by mutableStateOf("")
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var isOnline by mutableStateOf(false)
        private set

    var selectedStudent by mutableStateOf<StudentEntity?>(null)
        private set

    var showAddEditDialog by mutableStateOf(false)
        private set

    var studentToEdit by mutableStateOf<StudentEntity?>(null)
        private set

    private var isSaving = false

    init {
        viewModelScope.launch {
            repository.onlineState.collect { online ->
                isOnline = online
                if (online) {
                    errorMessage = null
                }
            }
        }
        loadStudents()
    }

    fun clearError() {
        errorMessage = null
    }

    fun onSearchQueryChanged(query: String) {
        searchQuery = query
    }

    fun selectStudent(student: StudentEntity?) {
        selectedStudent = student
    }

    fun openAddDialog() {
        studentToEdit = null
        showAddEditDialog = true
    }

    fun openEditDialog(student: StudentEntity) {
        studentToEdit = student
        showAddEditDialog = true
    }

    fun dismissAddEditDialog() {
        showAddEditDialog = false
        studentToEdit = null
    }

    fun loadStudents() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                repository.getStudents()
                isOnline = repository.lastFetchWasOnline
            } catch (e: Exception) {
                errorMessage = e.message ?: "Connection Error"
                isOnline = false
            } finally {
                isLoading = false
            }
        }
    }

    fun addStudent(student: StudentEntity) {
        viewModelScope.launch {
            repository.addStudent(student)
        }
    }

    fun syncStudents() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                repository.syncPending()
                repository.getStudents()
                isOnline = repository.lastFetchWasOnline
            } catch (e: Exception) {
                errorMessage = "Sync failed: Server unreachable"
                isOnline = false
            } finally {
                isLoading = false
            }
        }
    }

    fun saveStudent(studentCode: String, name: String, major: String) {
        if (isSaving) return
        isSaving = true
        val current = studentToEdit
        dismissAddEditDialog()

        viewModelScope.launch {
            try {
                if (current != null) {
                    val updated = current.copy(
                        studentCode = studentCode,
                        name = name,
                        major = major
                    )
                    repository.updateStudent(updated)
                    if (selectedStudent?.localId == updated.localId) {
                        selectedStudent = updated
                    }
                    isOnline = repository.lastFetchWasOnline
                } else {
                    repository.addStudent(
                        StudentEntity(
                            studentCode = studentCode,
                            name = name,
                            major = major,
                            syncStatus = "PENDING"
                        )
                    )
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "Save student failed"
            } finally {
                isSaving = false
            }
        }
    }

    fun deleteStudent(student: StudentEntity) {
        viewModelScope.launch {
            try {
                repository.deleteStudent(student)
                if (selectedStudent?.localId == student.localId) {
                    selectedStudent = null
                }
                isOnline = repository.lastFetchWasOnline
            } catch (e: Exception) {
                errorMessage = e.message ?: "Delete failed"
            }
        }
    }
}

class StudentViewModelFactory(
    private val repository: StudentRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StudentViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StudentViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
