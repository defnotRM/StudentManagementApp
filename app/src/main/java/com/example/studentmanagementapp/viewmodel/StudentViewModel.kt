package com.example.studentmanagementapp.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studentmanagementapp.domain.model.Student
import com.example.studentmanagementapp.domain.repository.StudentRepository
import com.example.studentmanagementapp.domain.usecase.AddStudentUseCase
import com.example.studentmanagementapp.domain.usecase.DeleteStudentUseCase
import com.example.studentmanagementapp.domain.usecase.GetStudentsUseCase
import com.example.studentmanagementapp.domain.usecase.SyncStudentsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StudentViewModel @Inject constructor(
    private val getStudentsUseCase: GetStudentsUseCase,
    private val syncStudentsUseCase: SyncStudentsUseCase,
    private val addStudentUseCase: AddStudentUseCase,
    private val deleteStudentUseCase: DeleteStudentUseCase,
    private val repository: StudentRepository
) : ViewModel() {

    val students: StateFlow<List<Student>> =
        getStudentsUseCase()
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

    var selectedStudent by mutableStateOf<Student?>(null)
        private set

    var showAddEditDialog by mutableStateOf(false)
        private set

    var studentToEdit by mutableStateOf<Student?>(null)
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

    fun selectStudent(student: Student?) {
        selectedStudent = student
    }

    fun openAddDialog() {
        studentToEdit = null
        showAddEditDialog = true
    }

    fun openEditDialog(student: Student) {
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

    fun addStudent(student: Student) {
        viewModelScope.launch {
            addStudentUseCase(student)
        }
    }

    fun syncStudents() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                syncStudentsUseCase()
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
                    if (selectedStudent?.studentCode == updated.studentCode) {
                        selectedStudent = updated
                    }
                    isOnline = repository.lastFetchWasOnline
                } else {
                    addStudentUseCase(
                        Student(
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

    fun deleteStudent(student: Student) {
        viewModelScope.launch {
            try {
                deleteStudentUseCase(student)
                if (selectedStudent?.studentCode == student.studentCode) {
                    selectedStudent = null
                }
                isOnline = repository.lastFetchWasOnline
            } catch (e: Exception) {
                errorMessage = e.message ?: "Delete failed"
            }
        }
    }
}
