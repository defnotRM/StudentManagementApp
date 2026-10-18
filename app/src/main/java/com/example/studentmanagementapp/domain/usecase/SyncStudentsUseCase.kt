package com.example.studentmanagementapp.domain.usecase

import com.example.studentmanagementapp.domain.repository.StudentRepository
import javax.inject.Inject

class SyncStudentsUseCase @Inject constructor(
    private val repository: StudentRepository
) {
    suspend operator fun invoke() {
        repository.syncPending()
    }
}
