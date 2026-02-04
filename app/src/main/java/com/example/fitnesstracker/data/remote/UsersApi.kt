package com.example.fitnesstracker.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

interface UsersApi {
    @GET("getUser/{id}")
    suspend fun getUser(
        @Path("id") userId: String
    ): User

    @PUT("updateUser/{id}")
    suspend fun updateUser(
        @Path("id") userId: String,
        @Body body: UpdateUserRequest
    ): IdResponse
}
