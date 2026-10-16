package com.example.studentmanagementapp.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.studentmanagementapp.data.repository.StudentRepository
import com.example.studentmanagementapp.model.Student
import kotlinx.coroutines.launch

class StudentViewModel(
    private val repository: StudentRepository
) : ViewModel() {

    private var allStudents by mutableStateOf<List<Student>>(emptyList())

    var searchQuery by mutableStateOf("")
        private set

    val students: List<Student>
        get() {
            if (searchQuery.isBlank()) return allStudents
            val query = searchQuery.trim().lowercase()
            return allStudents.filter {
                it.studentCode.lowercase().contains(query) ||
                it.name.lowercase().contains(query) ||
                it.major.lowercase().contains(query)
            }
        }

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
        loadStudents()
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
                allStudents = repository.getStudents()
                isOnline = repository.lastFetchWasOnline
            } catch (e: Exception) {
                errorMessage = e.message ?: "Connection Error"
                isOnline = false
            } finally {
                isLoading = false
            }
        }
    }

    fun addStudent(
        studentCode: String,
        name: String,
        major: String
    ) {
        viewModelScope.launch {
            try {
                repository.addStudent(studentCode, name, major)
                isOnline = repository.lastFetchWasOnline
                allStudents = if (isOnline) repository.getStudents() else repository.getLocalStudents()
            } catch (e: Exception) {
                errorMessage = e.message ?: "Add student failed"
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
                    if (selectedStudent?.id == updated.id) {
                        selectedStudent = updated
                    }
                } else {
                    repository.addStudent(studentCode, name, major)
                }
                isOnline = repository.lastFetchWasOnline
                allStudents = if (isOnline) repository.getStudents() else repository.getLocalStudents()
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
                repository.deleteStudent(student)
                if (selectedStudent?.id == student.id) {
                    selectedStudent = null
                }
                isOnline = repository.lastFetchWasOnline
                allStudents = if (isOnline) repository.getStudents() else repository.getLocalStudents()
            } catch (e: Exception) {
                errorMessage = e.message ?: "Delete failed"
            }
        }
    }

    fun deleteStudent(id: Int) {
        val target = allStudents.find { it.id == id } ?: return
        deleteStudent(target)
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
