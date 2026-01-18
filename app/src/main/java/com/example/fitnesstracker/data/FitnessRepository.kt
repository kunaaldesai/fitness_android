package com.example.fitnesstracker.data

import com.example.fitnesstracker.AppConfig
import com.example.fitnesstracker.data.remote.CreateExerciseRequest
import com.example.fitnesstracker.data.remote.CreateSetRequest
import com.example.fitnesstracker.data.remote.CreateWorkoutItemRequest
import com.example.fitnesstracker.data.remote.CreateWorkoutPlanRequest
import com.example.fitnesstracker.data.remote.CreateWorkoutRequest
import com.example.fitnesstracker.data.remote.Exercise
import com.example.fitnesstracker.data.remote.IdResponse
import com.example.fitnesstracker.data.remote.NetworkModule
import com.example.fitnesstracker.data.remote.StartWorkoutRequest
import com.example.fitnesstracker.data.remote.UpdateSetRequest
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

    suspend fun fetchExercises(includeArchived: Boolean = false): Result<List<Exercise>> =
        runCatching { workoutsApi.getExercises(userId, includeArchived) }

    suspend fun fetchWorkouts(limit: Int? = AppConfig.DEFAULT_WORKOUT_FETCH_LIMIT): Result<List<Workout>> =
        runCatching { workoutsApi.getWorkouts(userId, limit = limit) }

    suspend fun fetchWorkoutPlans(): Result<List<WorkoutPlan>> =
        runCatching { workoutsApi.getAllWorkouts() }

    suspend fun fetchWorkoutDetail(workoutId: String): Result<Workout> =
        runCatching { workoutsApi.getWorkoutDetail(userId, workoutId, includeItems = true, includeSets = true) }

    suspend fun createWorkout(
        date: String,
        notes: String? = null,
        timezone: String? = null,
        startTime: String? = null,
        endTime: String? = null
    ): Result<String> = runCatching {
        workoutsApi.createWorkout(
            userId,
            CreateWorkoutRequest(
                date = date,
                startTime = startTime,
                endTime = endTime,
                notes = notes,
                timezone = timezone
            )
        ).requireId("createWorkout")
    }

    suspend fun startWorkout(
        workoutPlanId: String,
        date: String,
        timezone: String? = null
    ): Result<String> = runCatching {
        workoutsApi.startWorkout(
            userId,
            StartWorkoutRequest(
                workoutId = workoutPlanId,
                date = date,
                timezone = timezone
            )
        ).requireId("startWorkout")
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

    suspend fun addWorkoutItem(
        workoutId: String,
        exerciseId: String,
        notes: String? = null,
        order: Int? = null,
        exerciseName: String? = null
    ): Result<String> = runCatching {
        workoutsApi.addWorkoutItem(
            userId,
            workoutId,
            CreateWorkoutItemRequest(
                exerciseId = exerciseId,
                name = exerciseName,
                notes = notes,
                order = order
            )
        ).requireId("addWorkoutItem")
    }

    suspend fun addSet(
        workoutId: String,
        itemId: String,
        reps: Int,
        weight: Double?,
        rir: Double?,
        rpe: Double?,
        notes: String?,
        isPR: Boolean
    ): Result<String> = runCatching {
        workoutsApi.addSet(
            userId,
            workoutId,
            itemId,
            CreateSetRequest(
                reps = reps,
                weight = weight,
                rir = rir,
                rpe = rpe,
                notes = notes,
                isPR = isPR
            )
        ).requireId("addSet")
    }

    suspend fun updateSet(
        workoutId: String,
        itemId: String,
        setId: String,
        reps: Int?,
        weight: Double?,
        rir: Double?,
        rpe: Double?,
        notes: String?,
        isPr: Boolean?
    ): Result<Unit> = runCatching {
        workoutsApi.updateSet(
            userId,
            workoutId,
            itemId,
            setId,
            UpdateSetRequest(
                reps = reps,
                weight = weight,
                rir = rir,
                rpe = rpe,
                notes = notes,
                isPr = isPr
            )
        )
        Unit
    }

    private fun IdResponse.requireId(action: String): String {
        return id ?: error("Missing id in $action response")
    }
}
