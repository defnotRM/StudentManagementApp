package com.example.studentmanagementapp

import android.app.Application

class StudentApplication : Application() {

    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()

        appContainer = AppContainer(this)
    }
}
