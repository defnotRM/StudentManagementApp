package com.example.studentmanagementapp.data.remote

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val tokenStorage: TokenStorage
) : Interceptor {

    override fun intercept(
        chain: Interceptor.Chain
    ): Response {
        val originalRequest = chain.request()
        val token = tokenStorage.getToken()

        val newRequest = if (token != null) {
            originalRequest.newBuilder()
                .addHeader(
                    "Authorization",
                    "Bearer $token"
                )
                .build()
        } else {
            originalRequest
        }

        return chain.proceed(newRequest)
    }
}
