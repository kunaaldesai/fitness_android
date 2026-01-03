package com.example.fitnesstracker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnesstracker.data.FitnessRepository
import com.example.fitnesstracker.data.remote.Exercise
import com.example.fitnesstracker.data.remote.User
import com.example.fitnesstracker.data.remote.Workout
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FitnessUiState(
    val user: User? = null,
    val workouts: List<Workout> = emptyList(),
    val exercises: List<Exercise> = emptyList(),
    val selectedWorkoutId: String? = null,
    val selectedWorkout: Workout? = null,
    val recentlyCreatedWorkoutId: String? = null,
    val isLoading: Boolean = false,
    val isActionRunning: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null
)

class MainViewModel(
    private val repository: FitnessRepository = FitnessRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(FitnessUiState())
    val uiState: StateFlow<FitnessUiState> = _uiState.asStateFlow()

    init {
        refreshEverything()
    }

    fun refreshEverything() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val userResult = repository.fetchUser()
            val workoutsResult = repository.fetchWorkouts()
            val exercisesResult = repository.fetchExercises()

            _uiState.update { state ->
                val workouts = workoutsResult.getOrNull() ?: state.workouts
                val exercises = exercisesResult.getOrNull() ?: state.exercises
                state.copy(
                    user = userResult.getOrNull() ?: state.user,
                    workouts = workouts,
                    exercises = exercises,
                    isLoading = false,
                    errorMessage = userResult.exceptionOrNull()?.message
                        ?: workoutsResult.exceptionOrNull()?.message
                        ?: exercisesResult.exceptionOrNull()?.message
                )
            }

            val initialId = _uiState.value.selectedWorkoutId ?: _uiState.value.workouts.firstOrNull()?.id
            initialId?.let { selectWorkout(it, force = true) }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(errorMessage = null, infoMessage = null) }
    }

    fun consumeRecentlyCreatedWorkout() {
        _uiState.update { it.copy(recentlyCreatedWorkoutId = null) }
    }

    fun selectWorkout(workoutId: String, force: Boolean = false, infoMessage: String? = null) {
        viewModelScope.launch {
            val alreadySelected = _uiState.value.selectedWorkout?.id == workoutId
            if (!force && alreadySelected) return@launch

            _uiState.update {
                it.copy(
                    isActionRunning = true,
                    selectedWorkoutId = workoutId,
                    errorMessage = null
                )
            }

            val result = repository.fetchWorkoutDetail(workoutId)
            _uiState.update { state ->
                result.fold(
                    onSuccess = { workout ->
                        state.copy(
                            selectedWorkout = workout,
                            selectedWorkoutId = workout.id,
                            isActionRunning = false,
                            infoMessage = infoMessage ?: state.infoMessage
                        )
                    },
                    onFailure = { error ->
                        state.copy(
                            isActionRunning = false,
                            errorMessage = error.userFacing("Could not load workout"),
                            selectedWorkoutId = workoutId
                        )
                    }
                )
            }
        }
    }

    fun createWorkout(date: String, notes: String?, timezone: String? = null) {
        val trimmedDate = date.trim()
        if (trimmedDate.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Date is required (YYYY-MM-DD).") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isActionRunning = true, errorMessage = null, recentlyCreatedWorkoutId = null) }
            val result = repository.createWorkout(
                date = trimmedDate,
                notes = notes?.takeUnless { it.isNullOrBlank() },
                timezone = timezone
            )
            result.fold(
                onSuccess = { id ->
                    refreshAfterAction(
                        selectWorkoutId = id,
                        infoMessage = "Workout created",
                        createdWorkoutId = id
                    )
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isActionRunning = false, errorMessage = error.userFacing("Could not create workout")) }
                }
            )
        }
    }

    fun createExercise(name: String, notes: String?) {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Exercise name is required.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isActionRunning = true, errorMessage = null) }
            val result = repository.createExercise(trimmedName, notes?.takeUnless { it.isNullOrBlank() })
            result.fold(
                onSuccess = {
                    refreshAfterAction(infoMessage = "Exercise saved")
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isActionRunning = false, errorMessage = error.userFacing("Could not save exercise")) }
                }
            )
        }
    }

    fun addItemToWorkout(
        workoutId: String,
        exerciseId: String,
        notes: String?,
        order: Int?
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isActionRunning = true, errorMessage = null) }
            val exerciseName = _uiState.value.exercises.firstOrNull { it.id == exerciseId }?.name
            val result = repository.addWorkoutItem(
                workoutId = workoutId,
                exerciseId = exerciseId,
                notes = notes?.takeUnless { it.isNullOrBlank() },
                order = order,
                exerciseName = exerciseName
            )
            result.fold(
                onSuccess = {
                    selectWorkout(workoutId, force = true, infoMessage = "Exercise added to workout")
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isActionRunning = false, errorMessage = error.userFacing("Could not add exercise to workout")) }
                }
            )
        }
    }

    fun addSet(
        workoutId: String,
        itemId: String,
        reps: Int,
        weight: Double?,
        rir: Double?,
        rpe: Double?,
        notes: String?,
        isPR: Boolean
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isActionRunning = true, errorMessage = null) }
            val result = repository.addSet(
                workoutId = workoutId,
                itemId = itemId,
                reps = reps,
                weight = weight,
                rir = rir,
                rpe = rpe,
                notes = notes?.takeUnless { it.isNullOrBlank() },
                isPR = isPR
            )
            result.fold(
                onSuccess = {
                    selectWorkout(workoutId, force = true, infoMessage = "Set added")
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isActionRunning = false, errorMessage = error.userFacing("Could not add set")) }
                }
            )
        }
    }

    private suspend fun refreshAfterAction(
        selectWorkoutId: String? = null,
        infoMessage: String? = null,
        createdWorkoutId: String? = null
    ) {
        val workoutsResult = repository.fetchWorkouts()
        val exercisesResult = repository.fetchExercises()
        _uiState.update { state ->
            state.copy(
                workouts = workoutsResult.getOrDefault(state.workouts),
                exercises = exercisesResult.getOrDefault(state.exercises),
                isActionRunning = false,
                errorMessage = workoutsResult.exceptionOrNull()?.message
                    ?: exercisesResult.exceptionOrNull()?.message,
                infoMessage = infoMessage ?: state.infoMessage,
                recentlyCreatedWorkoutId = createdWorkoutId ?: state.recentlyCreatedWorkoutId
            )
        }
        selectWorkoutId?.let { selectWorkout(it, force = true, infoMessage = infoMessage) }
    }

    private fun Throwable.userFacing(fallback: String): String =
        message?.takeIf { it.isNotBlank() } ?: fallback
}
