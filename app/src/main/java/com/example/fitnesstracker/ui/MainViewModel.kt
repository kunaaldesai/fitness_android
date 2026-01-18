package com.example.fitnesstracker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnesstracker.data.FitnessRepository
import com.example.fitnesstracker.data.remote.Exercise
import com.example.fitnesstracker.data.remote.User
import com.example.fitnesstracker.data.remote.Workout
import com.example.fitnesstracker.data.remote.WorkoutPlan
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

data class FitnessUiState(
    val user: User? = null,
    val workouts: List<Workout> = emptyList(),
    val workoutPlans: List<WorkoutPlan> = emptyList(),
    val exercises: List<Exercise> = emptyList(),
    val selectedWorkoutId: String? = null,
    val selectedWorkout: Workout? = null,
    val recentlyCreatedWorkoutId: String? = null,
    val recentlyCreatedWorkoutPlanId: String? = null,
    val recentlyCompletedWorkoutId: String? = null,
    val activeWorkoutId: String? = null,
    val activeWorkoutPlanId: String? = null,
    val isLoading: Boolean = false,
    val isActionRunning: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null
)

data class WorkoutSetEntry(
    val itemId: String,
    val reps: Int,
    val weight: Double? = null,
    val rir: Double? = null,
    val rpe: Double? = null,
    val notes: String? = null,
    val isPr: Boolean = false
)

data class WorkoutSetUpdateEntry(
    val itemId: String,
    val setId: String,
    val reps: Int,
    val weight: Double? = null,
    val rir: Double? = null
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
            val workoutPlansResult = repository.fetchWorkoutPlans()
            val exercisesResult = repository.fetchExercises()

            _uiState.update { state ->
                val workouts = workoutsResult.getOrNull() ?: state.workouts
                val workoutPlans = workoutPlansResult.getOrNull() ?: state.workoutPlans
                val exercises = exercisesResult.getOrNull() ?: state.exercises
                state.copy(
                    user = userResult.getOrNull() ?: state.user,
                    workouts = workouts,
                    workoutPlans = workoutPlans,
                    exercises = exercises,
                    isLoading = false,
                    errorMessage = userResult.exceptionOrNull()?.message
                        ?: workoutsResult.exceptionOrNull()?.message
                        ?: workoutPlansResult.exceptionOrNull()?.message
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

    fun consumeRecentlyCreatedWorkoutPlan() {
        _uiState.update { it.copy(recentlyCreatedWorkoutPlanId = null) }
    }

    fun consumeRecentlyCompletedWorkout() {
        _uiState.update { it.copy(recentlyCompletedWorkoutId = null) }
    }

    fun clearActiveWorkout() {
        _uiState.update { it.copy(activeWorkoutId = null, activeWorkoutPlanId = null) }
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

    fun startWorkoutFromPlan(planId: String) {
        val date = LocalDate.now().toString()
        val timezone = ZoneId.systemDefault().id
        viewModelScope.launch {
            _uiState.update { it.copy(isActionRunning = true, errorMessage = null) }
            val result = repository.startWorkout(planId, date, timezone)
            result.fold(
                onSuccess = { workoutId ->
                    _uiState.update {
                        it.copy(
                            activeWorkoutId = workoutId,
                            activeWorkoutPlanId = planId
                        )
                    }
                    refreshAfterAction(
                        selectWorkoutId = workoutId,
                        infoMessage = "Workout started"
                    )
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isActionRunning = false,
                            errorMessage = error.userFacing("Could not start workout")
                        )
                    }
                }
            )
        }
    }

    fun createWorkoutPlan(
        name: String,
        description: String?,
        exercises: List<String>,
        equipment: List<String>,
        muscleGroups: List<String>,
        numberOfExercises: Int?,
        sets: Int?,
        type: String?
    ) {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Workout name is required.") }
            return
        }
        val sanitizedMuscleGroups = muscleGroups.map { it.trim() }.filter { it.isNotBlank() }
        viewModelScope.launch {
            _uiState.update { it.copy(isActionRunning = true, errorMessage = null) }
            val result = repository.createWorkoutPlan(
                name = trimmedName,
                description = description?.takeUnless { it.isNullOrBlank() },
                exercises = exercises,
                equipment = equipment,
                muscleGroups = sanitizedMuscleGroups,
                numberOfExercises = numberOfExercises,
                sets = sets,
                type = type?.takeUnless { it.isNullOrBlank() }
            )
            result.fold(
                onSuccess = { id ->
                    refreshWorkoutPlans(createdWorkoutPlanId = id)
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isActionRunning = false,
                            errorMessage = error.userFacing("Could not create workout plan")
                        )
                    }
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

    fun logWorkoutSets(workoutId: String, entries: List<WorkoutSetEntry>) {
        if (entries.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Add at least one set before saving.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isActionRunning = true, errorMessage = null) }
            var failure: Throwable? = null
            for (entry in entries) {
                val result = repository.addSet(
                    workoutId = workoutId,
                    itemId = entry.itemId,
                    reps = entry.reps,
                    weight = entry.weight,
                    rir = entry.rir,
                    rpe = entry.rpe,
                    notes = entry.notes,
                    isPR = entry.isPr
                )
                if (result.isFailure) {
                    failure = result.exceptionOrNull()
                    break
                }
            }
            if (failure == null) {
                refreshAfterAction(
                    selectWorkoutId = workoutId,
                    infoMessage = "Workout saved",
                    completedWorkoutId = workoutId
                )
            } else {
                _uiState.update {
                    it.copy(
                        isActionRunning = false,
                        errorMessage = failure.userFacing("Could not save workout")
                    )
                }
            }
        }
    }

    fun updateSet(
        workoutId: String,
        itemId: String,
        setId: String,
        reps: Int?,
        weight: Double?,
        rir: Double?,
        rpe: Double?,
        notes: String?,
        isPr: Boolean?
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isActionRunning = true, errorMessage = null) }
            val result = repository.updateSet(
                workoutId = workoutId,
                itemId = itemId,
                setId = setId,
                reps = reps,
                weight = weight,
                rir = rir,
                rpe = rpe,
                notes = notes?.takeUnless { it.isNullOrBlank() },
                isPr = isPr
            )
            result.fold(
                onSuccess = {
                    selectWorkout(workoutId, force = true, infoMessage = "Set updated")
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(isActionRunning = false, errorMessage = error.userFacing("Could not update set"))
                    }
                }
            )
        }
    }

    fun updateWorkoutLog(
        workoutId: String,
        updates: List<WorkoutSetUpdateEntry>,
        newSets: List<WorkoutSetEntry>
    ) {
        if (updates.isEmpty() && newSets.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "No changes to save.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isActionRunning = true, errorMessage = null) }
            var failure: Throwable? = null
            for (entry in newSets) {
                val result = repository.addSet(
                    workoutId = workoutId,
                    itemId = entry.itemId,
                    reps = entry.reps,
                    weight = entry.weight,
                    rir = entry.rir,
                    rpe = entry.rpe,
                    notes = entry.notes,
                    isPR = entry.isPr
                )
                if (result.isFailure) {
                    failure = result.exceptionOrNull()
                    break
                }
            }
            if (failure == null) {
                for (entry in updates) {
                    val result = repository.updateSet(
                        workoutId = workoutId,
                        itemId = entry.itemId,
                        setId = entry.setId,
                        reps = entry.reps,
                        weight = entry.weight,
                        rir = entry.rir,
                        rpe = null,
                        notes = null,
                        isPr = null
                    )
                    if (result.isFailure) {
                        failure = result.exceptionOrNull()
                        break
                    }
                }
            }
            if (failure == null) {
                selectWorkout(workoutId, force = true, infoMessage = "Workout updated")
            } else {
                _uiState.update {
                    it.copy(isActionRunning = false, errorMessage = failure.userFacing("Could not update workout"))
                }
            }
        }
    }

    private suspend fun refreshAfterAction(
        selectWorkoutId: String? = null,
        infoMessage: String? = null,
        createdWorkoutId: String? = null,
        completedWorkoutId: String? = null
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
                recentlyCreatedWorkoutId = createdWorkoutId ?: state.recentlyCreatedWorkoutId,
                recentlyCompletedWorkoutId = completedWorkoutId ?: state.recentlyCompletedWorkoutId
            )
        }
        selectWorkoutId?.let { selectWorkout(it, force = true, infoMessage = infoMessage) }
    }

    private suspend fun refreshWorkoutPlans(
        infoMessage: String? = null,
        createdWorkoutPlanId: String? = null
    ) {
        val workoutPlansResult = repository.fetchWorkoutPlans()
        _uiState.update { state ->
            state.copy(
                workoutPlans = workoutPlansResult.getOrDefault(state.workoutPlans),
                isActionRunning = false,
                errorMessage = workoutPlansResult.exceptionOrNull()?.message,
                infoMessage = infoMessage ?: state.infoMessage,
                recentlyCreatedWorkoutPlanId = createdWorkoutPlanId ?: state.recentlyCreatedWorkoutPlanId
            )
        }
    }

    private fun Throwable.userFacing(fallback: String): String =
        message?.takeIf { it.isNotBlank() } ?: fallback
}
