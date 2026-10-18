package com.example.studentmanagementapp

import android.content.Context
import androidx.room.Room
import com.example.studentmanagementapp.data.local.AppDatabase
import com.example.studentmanagementapp.data.remote.AuthApi
import com.example.studentmanagementapp.data.remote.RetrofitClient
import com.example.studentmanagementapp.data.remote.StudentApi
import com.example.studentmanagementapp.data.remote.TokenStorage
import com.example.studentmanagementapp.data.repository.StudentRepository

class AppContainer(context: Context) {

    private val database =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "student.db"
        ).fallbackToDestructiveMigration(true)
            .build()

    val tokenStorage =
        TokenStorage(context)

    private val retrofit =
        RetrofitClient.create(tokenStorage)

    val authApi: AuthApi =
        retrofit.create(AuthApi::class.java)

    private val studentApi =
        retrofit.create(StudentApi::class.java)

    val studentRepository =
        StudentRepository(
            dao = database.studentDao(),
            api = studentApi
        )
}
