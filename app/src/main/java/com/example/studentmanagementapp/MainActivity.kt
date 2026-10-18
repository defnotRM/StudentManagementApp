package com.example.studentmanagementapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.studentmanagementapp.data.local.AppDatabase
import com.example.studentmanagementapp.data.remote.AuthApi
import com.example.studentmanagementapp.data.remote.RetrofitClient
import com.example.studentmanagementapp.data.remote.StudentApi
import com.example.studentmanagementapp.data.remote.TokenStorage
import com.example.studentmanagementapp.data.repository.StudentRepository
import com.example.studentmanagementapp.ui.StudentScreen
import com.example.studentmanagementapp.ui.login.LoginScreen
import com.example.studentmanagementapp.ui.login.LoginViewModel
import com.example.studentmanagementapp.ui.theme.StudentManagementAppTheme
import com.example.studentmanagementapp.viewmodel.StudentViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val tokenStorage = TokenStorage(applicationContext)
        val retrofit = RetrofitClient.create(tokenStorage)
        val authApi = retrofit.create(AuthApi::class.java)
        val studentApi = retrofit.create(StudentApi::class.java)

        val database = AppDatabase.getDatabase(applicationContext)
        val repository = StudentRepository(database.studentDao(), studentApi)

        val loginViewModel = LoginViewModel(authApi, tokenStorage)
        val studentViewModel = StudentViewModel(repository)

        setContent {
            StudentManagementAppTheme {
                var isLoggedIn by remember {
                    mutableStateOf(tokenStorage.getToken() != null)
                }

                if (isLoggedIn) {
                    StudentScreen(
                        viewModel = studentViewModel,
                        onLogout = {
                            tokenStorage.clearToken()
                            isLoggedIn = false
                        }
                    )
                } else {
                    LoginScreen(
                        viewModel = loginViewModel,
                        onLoginSuccess = {
                            isLoggedIn = true
                            studentViewModel.loadStudents()
                        }
                    )
                }
            }
        }
    }
}