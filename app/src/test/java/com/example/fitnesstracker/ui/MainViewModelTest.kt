package com.example.fitnesstracker.ui

import com.example.fitnesstracker.MainDispatcherRule
import com.example.fitnesstracker.data.FitnessRepository
import com.example.fitnesstracker.data.remote.Exercise
import com.example.fitnesstracker.data.remote.User
import com.example.fitnesstracker.data.remote.Workout
import com.example.fitnesstracker.data.remote.WorkoutItem
import com.example.fitnesstracker.data.remote.WorkoutPlan
import com.example.fitnesstracker.data.remote.WorkoutSet
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun refreshEverything_populatesStateAndSelectsFirstWorkout() = runTest(mainDispatcherRule.dispatcher) {
        val repository = mockk<FitnessRepository>()
        val workout = Workout(
            id = "workout-1",
            date = "2025-01-01",
            notes = "Leg day",
            items = listOf(
                WorkoutItem(
                    id = "item-1",
                    exerciseId = "exercise-1",
                    name = "Squat",
                    sets = listOf(WorkoutSet(id = "set-1", reps = 5))
                )
            )
        )
        stubBaseResponses(
            repository = repository,
            workouts = listOf(workout)
        )
        coEvery { repository.fetchWorkoutDetail("workout-1") } returns Result.success(workout)

        val viewModel = MainViewModel(repository)

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("workout-1", state.selectedWorkout?.id)
        assertEquals("workout-1", state.selectedWorkoutId)
        assertFalse(state.isLoading)
        assertTrue(state.errorMessage == null)
    }

    @Test
    fun createWorkout_blankDate_setsErrorWithoutCallingRepository() = runTest(mainDispatcherRule.dispatcher) {
        val repository = mockk<FitnessRepository>()
        stubBaseResponses(repository = repository)
        val viewModel = MainViewModel(repository)
        advanceUntilIdle()

        viewModel.createWorkout("   ", notes = null)

        val state = viewModel.uiState.value
        assertEquals("Date is required (YYYY-MM-DD).", state.errorMessage)
        coVerify(exactly = 0) { repository.createWorkout(any(), any(), any(), any(), any()) }
    }

    @Test
    fun createWorkout_success_updatesStateAndSelectsWorkout() = runTest(mainDispatcherRule.dispatcher) {
        val repository = mockk<FitnessRepository>()
        val workout = Workout(id = "workout-1", date = "2025-01-01")
        coEvery { repository.fetchUser() } returns Result.success(User(id = "user-1", firstName = "Sam"))
        coEvery { repository.fetchWorkouts(any()) } returnsMany listOf(
            Result.success(emptyList()),
            Result.success(listOf(workout))
        )
        coEvery { repository.fetchWorkoutPlans() } returns Result.success(emptyList())
        coEvery { repository.fetchExercises(any()) } returnsMany listOf(
            Result.success(emptyList()),
            Result.success(emptyList())
        )
        coEvery { repository.fetchWorkoutDetail("workout-1") } returns Result.success(workout)
        coEvery { repository.createWorkout(any(), any(), any(), any(), any()) } returns Result.success("workout-1")

        val viewModel = MainViewModel(repository)
        advanceUntilIdle()

        viewModel.createWorkout(date = "2025-01-01", notes = "Notes")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("workout-1", state.recentlyCreatedWorkoutId)
        assertEquals("Workout created", state.infoMessage)
        assertEquals("workout-1", state.selectedWorkout?.id)
        assertFalse(state.isActionRunning)
    }

    @Test
    fun addItemToWorkout_usesExerciseNameFromState() = runTest(mainDispatcherRule.dispatcher) {
        val repository = mockk<FitnessRepository>()
        val exercise = Exercise(id = "exercise-1", name = "Bench Press")
        stubBaseResponses(repository = repository, exercises = listOf(exercise))
        coEvery { repository.addWorkoutItem(any(), any(), any(), any(), any()) } returns Result.success("item-1")
        coEvery { repository.fetchWorkoutDetail("workout-1") } returns Result.success(Workout(id = "workout-1"))

        val viewModel = MainViewModel(repository)
        advanceUntilIdle()

        viewModel.addItemToWorkout(
            workoutId = "workout-1",
            exerciseId = "exercise-1",
            notes = "Keep form tight",
            order = 1
        )
        advanceUntilIdle()

        coVerify(exactly = 1) {
            repository.addWorkoutItem(
                workoutId = "workout-1",
                exerciseId = "exercise-1",
                notes = "Keep form tight",
                order = 1,
                exerciseName = "Bench Press"
            )
        }
    }

    @Test
    fun logWorkoutSets_emptyEntries_setsError() = runTest(mainDispatcherRule.dispatcher) {
        val repository = mockk<FitnessRepository>()
        stubBaseResponses(repository = repository)
        val viewModel = MainViewModel(repository)
        advanceUntilIdle()

        viewModel.logWorkoutSets(workoutId = "workout-1", entries = emptyList())

        val state = viewModel.uiState.value
        assertEquals("Add at least one set before saving.", state.errorMessage)
        coVerify(exactly = 0) { repository.addSet(any(), any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun updateWorkoutLog_noChanges_setsError() = runTest(mainDispatcherRule.dispatcher) {
        val repository = mockk<FitnessRepository>()
        stubBaseResponses(repository = repository)
        val viewModel = MainViewModel(repository)
        advanceUntilIdle()

        viewModel.updateWorkoutLog(
            workoutId = "workout-1",
            updates = emptyList(),
            newSets = emptyList()
        )

        val state = viewModel.uiState.value
        assertEquals("No changes to save.", state.errorMessage)
        coVerify(exactly = 0) { repository.addSet(any(), any(), any(), any(), any(), any(), any(), any()) }
        coVerify(exactly = 0) { repository.updateSet(any(), any(), any(), any(), any(), any(), any(), any(), any()) }
    }

    private fun stubBaseResponses(
        repository: FitnessRepository,
        user: User = User(id = "user-1", firstName = "Sam"),
        workouts: List<Workout> = emptyList(),
        workoutPlans: List<WorkoutPlan> = emptyList(),
        exercises: List<Exercise> = emptyList()
    ) {
        coEvery { repository.fetchUser() } returns Result.success(user)
        coEvery { repository.fetchWorkouts(any()) } returns Result.success(workouts)
        coEvery { repository.fetchWorkoutPlans() } returns Result.success(workoutPlans)
        coEvery { repository.fetchExercises(any()) } returns Result.success(exercises)
        if (workouts.isNotEmpty()) {
            coEvery { repository.fetchWorkoutDetail(workouts.first().id) } returns Result.success(workouts.first())
        }
    }
}
