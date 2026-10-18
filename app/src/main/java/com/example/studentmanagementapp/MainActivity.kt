package com.example.studentmanagementapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.studentmanagementapp.ui.StudentScreen
import com.example.studentmanagementapp.ui.login.LoginScreen
import com.example.studentmanagementapp.ui.login.LoginViewModel
import com.example.studentmanagementapp.ui.theme.StudentManagementAppTheme
import com.example.studentmanagementapp.viewmodel.StudentViewModel
import com.example.studentmanagementapp.worker.SyncScheduler

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        SyncScheduler.schedulePeriodicSync(this)

        val appContainer = (application as StudentApplication).appContainer
        val tokenStorage = appContainer.tokenStorage
        val authApi = appContainer.authApi
        val repository = appContainer.studentRepository

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