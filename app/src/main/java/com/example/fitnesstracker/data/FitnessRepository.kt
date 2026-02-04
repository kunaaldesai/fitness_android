package com.example.fitnesstracker.data

import com.example.fitnesstracker.AppConfig
import com.example.fitnesstracker.data.remote.CreateExerciseRequest
import com.example.fitnesstracker.data.remote.CreateWorkoutPlanRequest
import com.example.fitnesstracker.data.remote.CreateWorkoutRequest
import com.example.fitnesstracker.data.remote.Exercise
import com.example.fitnesstracker.data.remote.IdResponse
import com.example.fitnesstracker.data.remote.NetworkModule
import com.example.fitnesstracker.data.remote.UpdateUserRequest
import com.example.fitnesstracker.data.remote.User
import com.example.fitnesstracker.data.remote.UsersApi
import com.example.fitnesstracker.data.remote.Workout
import com.example.fitnesstracker.data.remote.WorkoutPlan
import com.example.fitnesstracker.data.remote.WorkoutsApi

class FitnessRepository(
    private val userId: String = AppConfig.USER_ID,
    private val workoutsApi: WorkoutsApi = NetworkModule.workoutsApi,
    private val usersApi: UsersApi = NetworkModule.usersApi
) {

    suspend fun fetchUser(): Result<User> = runCatching { usersApi.getUser(userId) }

    suspend fun updateUser(firstName: String, lastName: String, bio: String): Result<Unit> = runCatching {
        usersApi.updateUser(
            userId,
            UpdateUserRequest(firstName = firstName, lastName = lastName, bio = bio)
        )
    }

    suspend fun fetchExercises(includeArchived: Boolean = false): Result<List<Exercise>> =
        runCatching { workoutsApi.getExercises(userId, includeArchived) }

    suspend fun fetchWorkouts(limit: Int? = AppConfig.DEFAULT_WORKOUT_FETCH_LIMIT): Result<List<Workout>> =
        runCatching { workoutsApi.getWorkouts(userId, limit = limit) }

    suspend fun fetchWorkoutPlans(): Result<List<WorkoutPlan>> =
        runCatching { workoutsApi.getAllWorkouts() }

    suspend fun fetchWorkoutTemplate(templateId: String): Result<WorkoutPlan> =
        runCatching { workoutsApi.getWorkoutTemplate(templateId) }

    suspend fun fetchWorkoutDetail(workoutId: String): Result<Workout> =
        runCatching { workoutsApi.getWorkoutDetail(userId, workoutId, includeItems = true, includeSets = true) }

    suspend fun createWorkout(request: CreateWorkoutRequest): Result<String> = runCatching {
        workoutsApi.createWorkout(userId, request).requireId("createWorkout")
    }

    suspend fun updateWorkout(workoutId: String, request: CreateWorkoutRequest): Result<String> = runCatching {
        workoutsApi.updateWorkout(userId, workoutId, request)
        // Update returns a message, but we can return the ID (which is the same)
        workoutId
    }

    suspend fun createWorkoutPlan(
        name: String,
        description: String?,
        exercises: List<String>,
        equipment: List<String>,
        muscleGroups: List<String>,
        numberOfExercises: Int?,
        sets: Int?,
        type: String?
    ): Result<String> = runCatching {
        workoutsApi.createWorkoutPlan(
            CreateWorkoutPlanRequest(
                name = name,
                description = description,
                exercises = exercises,
                equipment = equipment,
                muscleGroups = muscleGroups,
                numberOfExercises = numberOfExercises,
                sets = sets,
                type = type,
                default = false
            )
        ).requireId("createWorkoutPlan")
    }

    suspend fun createExercise(name: String, notes: String? = null): Result<String> =
        runCatching {
            workoutsApi.createExercise(
                userId,
                CreateExerciseRequest(
                    name = name,
                    notes = notes
                )
            ).requireId("createExercise")
        }

    private fun IdResponse.requireId(action: String): String {
        return id ?: error("Missing id in  response")
    }
}
