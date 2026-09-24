package com.example.studentmanagementapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.studentmanagementapp.data.local.AppDatabase
import com.example.studentmanagementapp.data.repository.StudentRepository
import com.example.studentmanagementapp.ui.StudentScreen
import com.example.studentmanagementapp.ui.theme.StudentManagementAppTheme
import com.example.studentmanagementapp.viewmodel.StudentViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Room Database & Repository
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = StudentRepository(database.studentDao())
        val viewModel = StudentViewModel(repository)

        setContent {
            StudentManagementAppTheme {
                StudentScreen(viewModel = viewModel)
            }
        }
    }
}