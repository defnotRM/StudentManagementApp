package com.example.studentmanagementapp.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.studentmanagementapp.model.Student

@Dao
interface StudentDao {
    @Query("SELECT * FROM students ORDER BY id ASC")
    suspend fun getAllStudents(): List<Student>

    @Query("SELECT * FROM students WHERE id = :id")
    suspend fun getStudentById(id: Int): Student?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: Student)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(
        students: List<Student>
    )

    @Query("DELETE FROM students")
    suspend fun deleteAll()

    @Update
    suspend fun updateStudent(student: Student)

    @Delete
    suspend fun deleteStudent(student: Student)

    @Query("DELETE FROM students WHERE id = :id")
    suspend fun deleteById(id: Int)
}
