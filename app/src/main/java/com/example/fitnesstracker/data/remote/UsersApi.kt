package com.example.fitnesstracker.data.remote

import retrofit2.http.GET
import retrofit2.http.Path

interface UsersApi {
    @GET("getUser/{id}")
    suspend fun getUser(
        @Path("id") userId: String
    ): User
}
