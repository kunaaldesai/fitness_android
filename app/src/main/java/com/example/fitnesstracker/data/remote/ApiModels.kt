package com.example.fitnesstracker.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class User(
    val id: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val phoneNumber: String? = null,
    val bio: String? = null,
    val imageUrl: String? = null,
    val gender: String? = null,
    @SerialName("createdAt") val createdAt: JsonElement? = null,
    @SerialName("updatedAt") val updatedAt: JsonElement? = null
)

@Serializable
data class Exercise(
    val id: String,
    val name: String,
    val muscleGroups: List<String> = emptyList(),
    val equipment: String? = null,
    val notes: String? = null,
    val archived: Boolean = false,
    @SerialName("createdAt") val createdAt: JsonElement? = null,
    @SerialName("updatedAt") val updatedAt: JsonElement? = null
)

@Serializable
data class WorkoutSet(
    val id: String,
    val reps: Int? = null,
    val weight: Double? = null,
    val rir: Double? = null,
    val rpe: Double? = null,
    val isPR: Boolean? = null,
    val notes: String? = null,
    @SerialName("createdAt") val createdAt: JsonElement? = null,
    @SerialName("updatedAt") val updatedAt: JsonElement? = null
)

@Serializable
data class WorkoutItem(
    val id: String,
    val exerciseId: String? = null,
    val name: String? = null,
    val notes: String? = null,
    val order: Int? = null,
    val sets: List<WorkoutSet> = emptyList(),
    @SerialName("createdAt") val createdAt: JsonElement? = null,
    @SerialName("updatedAt") val updatedAt: JsonElement? = null
)

@Serializable
data class Workout(
    val id: String,
    val date: String? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    val notes: String? = null,
    val timezone: String? = null,
    val items: List<WorkoutItem> = emptyList(),
    @SerialName("createdAt") val createdAt: JsonElement? = null,
    @SerialName("updatedAt") val updatedAt: JsonElement? = null
)

@Serializable
data class WorkoutPlan(
    val id: String,
    val name: String? = null,
    val sets: Int? = null,
    val type: String? = null,
    @SerialName("muscle_group") val muscleGroup: String? = null,
    @SerialName("number_of_exercises") val numberOfExercises: Int? = null,
    val description: String? = null,
    val exercises: JsonElement? = null,
    val equipment: JsonElement? = null
)

@Serializable
data class CreateWorkoutPlanRequest(
    val description: String? = null,
    val exercises: List<String> = emptyList(),
    val equipment: List<String> = emptyList(),
    @SerialName("muscle_group") val muscleGroup: String? = null,
    val name: String? = null,
    @SerialName("number_of_exercises") val numberOfExercises: Int? = null,
    val sets: Int? = null,
    val type: String? = null,
    val default: Boolean = false
)

@Serializable
data class CreateWorkoutRequest(
    val date: String? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    val notes: String? = null,
    val timezone: String? = null
)

@Serializable
data class CreateExerciseRequest(
    val name: String,
    val muscleGroups: List<String> = emptyList(),
    val equipment: String? = null,
    val notes: String? = null,
    val archived: Boolean = false
)

@Serializable
data class CreateWorkoutItemRequest(
    val exerciseId: String,
    val name: String? = null,
    val notes: String? = null,
    val order: Int? = null
)

@Serializable
data class CreateSetRequest(
    val reps: Int,
    val weight: Double? = null,
    val rir: Double? = null,
    val rpe: Double? = null,
    val notes: String? = null,
    val isPR: Boolean? = null
)

@Serializable
data class IdResponse(
    val message: String? = null,
    val id: String? = null
)
