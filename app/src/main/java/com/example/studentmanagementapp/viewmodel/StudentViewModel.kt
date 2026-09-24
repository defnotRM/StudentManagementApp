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

    // Internal list of all students (STEP 5, 10, 16)
    private var allStudents by mutableStateOf<List<Student>>(emptyList())

    // Search query state (Exercise 3)
    var searchQuery by mutableStateOf("")
        private set

    // Filtered students based on search (Exercise 3: studentCode, name, major)
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

    // Loading State (STEP 26)
    var isLoading by mutableStateOf(false)
        private set

    // Error State (STEP 27)
    var errorMessage by mutableStateOf<String?>(null)
        private set

    // Online / Offline State (Exercise 4)
    var isOnline by mutableStateOf(false)
        private set

    // Selected Student for Detail Screen/View (Bonus point - page 29)
    var selectedStudent by mutableStateOf<Student?>(null)
        private set

    // Add / Edit Dialog state (Exercise 1)
    var showAddEditDialog by mutableStateOf(false)
        private set

    var studentToEdit by mutableStateOf<Student?>(null)
        private set

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

    // Load Students Flow (STEP 7, 16, 23, 26, 27)
    fun loadStudents() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                // Test remote connectivity for Online/Offline badge
                try {
                    repository.getRemoteStudents()
                    isOnline = true
                } catch (e: Exception) {
                    isOnline = false
                }

                // Load data using Offline-First strategy
                allStudents = repository.getStudents()
            } catch (e: Exception) {
                errorMessage = "ไม่สามารถโหลดข้อมูลได้"
                isOnline = false
            } finally {
                isLoading = false
            }
        }
    }

    // Add or Update Student (Exercise 1 & Page 24 Flow)
    fun saveStudent(studentCode: String, name: String, major: String) {
        viewModelScope.launch {
            try {
                val current = studentToEdit
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
                    val newStudent = Student(
                        studentCode = studentCode,
                        name = name,
                        major = major
                    )
                    repository.addStudent(newStudent)
                }
                allStudents = repository.getLocalStudents()
                dismissAddEditDialog()
            } catch (e: Exception) {
                errorMessage = "เกิดข้อผิดพลาดในการบันทึกข้อมูล"
            }
        }
    }

    // Delete Student (Exercise 2 - Page 27)
    fun deleteStudent(student: Student) {
        viewModelScope.launch {
            try {
                repository.deleteStudent(student)
                if (selectedStudent?.id == student.id) {
                    selectedStudent = null
                }
                allStudents = repository.getLocalStudents()
            } catch (e: Exception) {
                errorMessage = "เกิดข้อผิดพลาดในการลบข้อมูล"
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
