package com.example.studentmanagementapp.domain.usecase

import com.example.studentmanagementapp.domain.model.Student
import com.example.studentmanagementapp.domain.repository.StudentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class GetStudentsUseCaseTest {

    @Test
    fun getStudentsReturnsStudentList() = runTest {
        val repository = FakeStudentRepository()
        val useCase = GetStudentsUseCase(repository)
        val result = useCase().first()
        assertEquals(1, result.size)
        assertEquals("สมชาย", result.first().name)
    }

    private class FakeStudentRepository : StudentRepository {
        private val students = MutableStateFlow(
            listOf(
                Student(
                    id = 1,
                    studentCode = "66001",
                    name = "สมชาย",
                    major = "IT"
                )
            )
        )

        private val _onlineState = MutableStateFlow(true)
        override val onlineState: StateFlow<Boolean> = _onlineState.asStateFlow()
        override var lastFetchWasOnline: Boolean = true

        override fun observeStudents(): Flow<List<Student>> {
            return students
        }

        override suspend fun addStudent(student: Student) {
            students.value = students.value + student
        }

        override suspend fun syncPending() {
        }

        override suspend fun getStudents(): List<Student> {
            return students.value
        }

        override suspend fun updateStudent(student: Student) {
        }

        override suspend fun deleteStudent(student: Student) {
            students.value = students.value.filter { it.studentCode != student.studentCode }
        }
    }
}
