package com.example.fitnesstracker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnesstracker.data.FitnessRepository
import com.example.fitnesstracker.data.remote.CreateWorkoutRequest
import com.example.fitnesstracker.data.remote.Exercise
import com.example.fitnesstracker.data.remote.User
import com.example.fitnesstracker.data.remote.Workout
import com.example.fitnesstracker.data.remote.WorkoutExerciseRequest
import com.example.fitnesstracker.data.remote.WorkoutItem
import com.example.fitnesstracker.data.remote.WorkoutPlan
import com.example.fitnesstracker.data.remote.WorkoutSet
import com.example.fitnesstracker.data.remote.WorkoutSetRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

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

                val error = userResult.exceptionOrNull()
                    ?: workoutsResult.exceptionOrNull()
                    ?: workoutPlansResult.exceptionOrNull()
                    ?: exercisesResult.exceptionOrNull()

                val errorMessage = if (error != null && !isForbidden(error)) {
                    error.message
                } else {
                    null
                }

                state.copy(
                    user = userResult.getOrNull() ?: state.user,
                    workouts = workouts,
                    workoutPlans = workoutPlans,
                    exercises = exercises,
                    isLoading = false,
                    errorMessage = errorMessage
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

            if (workoutId == _uiState.value.activeWorkoutId && _uiState.value.selectedWorkout?.id == workoutId) {
                return@launch
            }

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
                        val updatedWorkouts = (state.workouts.filter { it.id != workout.id } + workout)
                            .sortedByDescending { it.date ?: "" }
                        state.copy(
                            selectedWorkout = workout,
                            selectedWorkoutId = workout.id,
                            workouts = updatedWorkouts,
                            isActionRunning = false,
                            infoMessage = infoMessage ?: state.infoMessage
                        )
                    },
                    onFailure = { error ->
                        val errorMessage = if (isForbidden(error)) null else error.userFacing("Could not load workout")
                        state.copy(
                            isActionRunning = false,
                            errorMessage = errorMessage,
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

        val tempId = UUID.randomUUID().toString()
        val newWorkout = Workout(
            id = tempId,
            date = trimmedDate,
            notes = notes,
            timezone = timezone,
            items = emptyList()
        )

        _uiState.update {
            it.copy(
                activeWorkoutId = tempId,
                selectedWorkout = newWorkout,
                selectedWorkoutId = tempId,
                recentlyCreatedWorkoutId = tempId
            )
        }
    }

    fun startWorkoutFromPlan(planId: String) {
        val date = LocalDate.now().toString()
        val timezone = ZoneId.systemDefault().id

        viewModelScope.launch {
            _uiState.update { it.copy(isActionRunning = true, errorMessage = null) }
            val planResult = repository.fetchWorkoutTemplate(planId)

            planResult.fold(
                onSuccess = { plan ->
                    val tempId = UUID.randomUUID().toString()
                    val items = parseExercises(plan.exercises)
                    val newWorkout = Workout(
                        id = tempId,
                        date = date,
                        timezone = timezone,
                        items = items
                    )

                    _uiState.update {
                        it.copy(
                            activeWorkoutId = tempId,
                            activeWorkoutPlanId = planId,
                            selectedWorkout = newWorkout,
                            selectedWorkoutId = tempId,
                            isActionRunning = false
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isActionRunning = false,
                            errorMessage = error.userFacing("Could not load workout plan")
                        )
                    }
                }
            )
        }
    }

    private fun parseExercises(element: kotlinx.serialization.json.JsonElement?): List<WorkoutItem> {
        if (element == null) return emptyList()
        val items = mutableListOf<WorkoutItem>()
        if (element is JsonArray) {
            element.forEachIndexed { index, item ->
                val tempItemId = UUID.randomUUID().toString()
                if (item is JsonObject) {
                    val exerciseId = (item["exerciseId"] as? JsonPrimitive)?.contentOrNull
                        ?: (item["id"] as? JsonPrimitive)?.contentOrNull
                    val name = (item["name"] as? JsonPrimitive)?.contentOrNull
                        ?: (item["title"] as? JsonPrimitive)?.contentOrNull
                    val notes = (item["notes"] as? JsonPrimitive)?.contentOrNull

                    if (exerciseId != null || name != null) {
                        items.add(WorkoutItem(
                            id = tempItemId,
                            exerciseId = exerciseId,
                            name = name,
                            notes = notes,
                            order = index
                        ))
                    }
                } else if (item is JsonPrimitive && item.isString) {
                    items.add(WorkoutItem(
                        id = tempItemId,
                        name = item.content,
                        order = index
                    ))
                }
            }
        }
        return items
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

    fun updateUser(firstName: String, lastName: String, bio: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isActionRunning = true, errorMessage = null) }
            val result = repository.updateUser(firstName, lastName, bio)
            result.fold(
                onSuccess = {
                    refreshEverything()
                    _uiState.update { it.copy(infoMessage = "Profile updated") }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isActionRunning = false,
                            errorMessage = error.userFacing("Could not update profile")
                        )
                    }
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
        val active = _uiState.value.selectedWorkout
        if (active != null && active.id == workoutId) {
            val exerciseName = _uiState.value.exercises.firstOrNull { it.id == exerciseId }?.name
            val newItem = WorkoutItem(
                id = UUID.randomUUID().toString(),
                exerciseId = exerciseId,
                name = exerciseName,
                notes = notes,
                order = order ?: active.items.size
            )
            val updatedItems = active.items + newItem
            val updatedWorkout = active.copy(items = updatedItems)
            _uiState.update { it.copy(selectedWorkout = updatedWorkout) }
        }
    }

    fun logWorkoutSets(workoutId: String, entries: List<WorkoutSetEntry>) {
        if (entries.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Add at least one set before saving.") }
            return
        }

        val active = _uiState.value.selectedWorkout
        if (active == null || active.id != workoutId) {
            _uiState.update { it.copy(errorMessage = "Active workout mismatch.") }
            return
        }

        val itemsMap = active.items.associateBy { it.id }.toMutableMap()

        val entriesByItem = entries.groupBy { it.itemId }

        entriesByItem.forEach { (itemId, setEntries) ->
            val item = itemsMap[itemId]
            if (item != null) {
                val newSets = setEntries.map { entry ->
                    WorkoutSet(
                        id = UUID.randomUUID().toString(),
                        reps = entry.reps,
                        weight = entry.weight,
                        rir = entry.rir,
                        rpe = entry.rpe,
                        isPR = entry.isPr,
                        notes = entry.notes
                    )
                }

                val updatedSets = item.sets + newSets
                itemsMap[itemId] = item.copy(sets = updatedSets)
            }
        }

        val updatedWorkout = active.copy(items = itemsMap.values.toList())
        saveWorkout(updatedWorkout)
    }

    private fun saveWorkout(workout: Workout) {
        viewModelScope.launch {
            _uiState.update { it.copy(isActionRunning = true, errorMessage = null) }

            val request = CreateWorkoutRequest(
                date = workout.date,
                notes = workout.notes,
                timezone = workout.timezone,
                workoutId = _uiState.value.activeWorkoutPlanId,
                exercises = workout.items.map { item ->
                    WorkoutExerciseRequest(
                        exerciseId = item.exerciseId,
                        name = item.name,
                        notes = item.notes,
                        sets = item.sets.map { set ->
                            WorkoutSetRequest(
                                id = if (set.id.length > 30) null else set.id,
                                reps = set.reps,
                                weight = set.weight,
                                rir = set.rir,
                                rpe = set.rpe,
                                isPR = set.isPR,
                                notes = set.notes
                            )
                        }
                    )
                }
            )

            val result = repository.createWorkout(request)
            result.fold(
                onSuccess = { id ->
                    refreshAfterAction(
                        selectWorkoutId = id,
                        infoMessage = "Workout saved",
                        completedWorkoutId = id
                    )
                    _uiState.update { it.copy(activeWorkoutId = null, activeWorkoutPlanId = null) }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isActionRunning = false,
                            errorMessage = error.userFacing("Could not save workout")
                        )
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

        val active = _uiState.value.selectedWorkout
        if (active == null || active.id != workoutId) {
            _uiState.update { it.copy(errorMessage = "Workout mismatch.") }
            return
        }

        val itemsMap = active.items.associateBy { it.id }.toMutableMap()

        updates.forEach { update ->
            val item = itemsMap[update.itemId]
            if (item != null) {
                val updatedSets = item.sets.map { set ->
                    if (set.id == update.setId) {
                        set.copy(
                            reps = update.reps,
                            weight = update.weight,
                            rir = update.rir
                        )
                    } else set
                }
                itemsMap[update.itemId] = item.copy(sets = updatedSets)
            }
        }

        newSets.forEach { entry ->
             val item = itemsMap[entry.itemId]
             if (item != null) {
                 val newSet = WorkoutSet(
                    id = UUID.randomUUID().toString(),
                    reps = entry.reps,
                    weight = entry.weight,
                    rir = entry.rir,
                    rpe = entry.rpe,
                    isPR = entry.isPr,
                    notes = entry.notes
                 )
                 itemsMap[entry.itemId] = item.copy(sets = item.sets + newSet)
             }
        }

        val finalWorkout = active.copy(items = itemsMap.values.toList())

        viewModelScope.launch {
            _uiState.update { it.copy(isActionRunning = true, errorMessage = null) }

            val request = CreateWorkoutRequest(
                date = finalWorkout.date,
                notes = finalWorkout.notes,
                timezone = finalWorkout.timezone,
                exercises = finalWorkout.items.map { item ->
                    WorkoutExerciseRequest(
                        exerciseId = item.exerciseId,
                        name = item.name,
                        notes = item.notes,
                        sets = item.sets.map { set ->
                             WorkoutSetRequest(
                                id = if (set.id.length > 30) null else set.id,
                                reps = set.reps,
                                weight = set.weight,
                                rir = set.rir,
                                rpe = set.rpe,
                                isPR = set.isPR,
                                notes = set.notes
                            )
                        }
                    )
                }
            )

            val result = repository.updateWorkout(workoutId, request)
            result.fold(
                onSuccess = {
                     refreshAfterAction(
                        selectWorkoutId = workoutId,
                        infoMessage = "Workout updated"
                     )
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isActionRunning = false,
                            errorMessage = error.userFacing("Could not update workout")
                        )
                    }
                }
            )
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
            val error = workoutsResult.exceptionOrNull() ?: exercisesResult.exceptionOrNull()
            val errorMessage = if (error != null && !isForbidden(error)) error.message else null

            state.copy(
                workouts = workoutsResult.getOrDefault(state.workouts),
                exercises = exercisesResult.getOrDefault(state.exercises),
                isActionRunning = false,
                errorMessage = errorMessage,
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
            val error = workoutPlansResult.exceptionOrNull()
            val errorMessage = if (error != null && !isForbidden(error)) error.message else null

            state.copy(
                workoutPlans = workoutPlansResult.getOrDefault(state.workoutPlans),
                isActionRunning = false,
                errorMessage = errorMessage,
                infoMessage = infoMessage ?: state.infoMessage,
                recentlyCreatedWorkoutPlanId = createdWorkoutPlanId ?: state.recentlyCreatedWorkoutPlanId
            )
        }
    }

    private fun Throwable.userFacing(fallback: String): String =
        message?.takeIf { it.isNotBlank() } ?: fallback

    private fun isForbidden(throwable: Throwable?): Boolean {
        return throwable is HttpException && throwable.code() == 403
    }
}
