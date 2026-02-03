package com.example.fitnesstracker.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface WorkoutsApi {
    @GET("users/{userId}/workouts")
    suspend fun getWorkouts(
        @Path("userId") userId: String,
        @Query("limit") limit: Int? = null,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null
    ): List<Workout>

    @GET("getAllWorkouts")
    suspend fun getAllWorkouts(): List<WorkoutPlan>

    @GET("getWorkout/{id}")
    suspend fun getWorkoutTemplate(
        @Path("id") id: String
    ): WorkoutPlan

    @GET("users/{userId}/workouts/{workoutId}")
    suspend fun getWorkoutDetail(
        @Path("userId") userId: String,
        @Path("workoutId") workoutId: String,
        @Query("includeItems") includeItems: Boolean = true,
        @Query("includeSets") includeSets: Boolean = true
    ): Workout

    @POST("users/{userId}/workouts")
    suspend fun createWorkout(
        @Path("userId") userId: String,
        @Body body: CreateWorkoutRequest
    ): IdResponse

    @PUT("users/{userId}/workouts/{workoutId}")
    suspend fun updateWorkout(
        @Path("userId") userId: String,
        @Path("workoutId") workoutId: String,
        @Body body: CreateWorkoutRequest
    ): IdResponse

    @POST("createWorkout")
    suspend fun createWorkoutPlan(
        @Body body: CreateWorkoutPlanRequest
    ): IdResponse

    @POST("users/{userId}/exercises")
    suspend fun createExercise(
        @Path("userId") userId: String,
        @Body body: CreateExerciseRequest
    ): IdResponse

    @GET("users/{userId}/exercises")
    suspend fun getExercises(
        @Path("userId") userId: String,
        @Query("includeArchived") includeArchived: Boolean = false
    ): List<Exercise>
}
