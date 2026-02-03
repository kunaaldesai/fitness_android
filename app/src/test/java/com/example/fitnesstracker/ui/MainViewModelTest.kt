package com.example.fitnesstracker.ui

import com.example.fitnesstracker.MainDispatcherRule
import com.example.fitnesstracker.data.FitnessRepository
import com.example.fitnesstracker.data.remote.CreateWorkoutRequest
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
import org.junit.Assert.assertNotNull
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
        coVerify(exactly = 0) { repository.createWorkout(any()) }
    }

    @Test
    fun createWorkout_success_updatesLocalStateOnly() = runTest(mainDispatcherRule.dispatcher) {
        val repository = mockk<FitnessRepository>()
        stubBaseResponses(repository = repository)
        val viewModel = MainViewModel(repository)
        advanceUntilIdle()

        viewModel.createWorkout(date = "2025-01-01", notes = "Notes")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.activeWorkoutId)
        assertEquals(state.activeWorkoutId, state.selectedWorkoutId)
        assertEquals("2025-01-01", state.selectedWorkout?.date)
        assertEquals("Notes", state.selectedWorkout?.notes)
        // Verify repository was NOT called
        coVerify(exactly = 0) { repository.createWorkout(any()) }
    }

    @Test
    fun addItemToWorkout_updatesLocalState() = runTest(mainDispatcherRule.dispatcher) {
        val repository = mockk<FitnessRepository>()
        val exercise = Exercise(id = "exercise-1", name = "Bench Press")
        stubBaseResponses(repository = repository, exercises = listOf(exercise))

        val viewModel = MainViewModel(repository)
        advanceUntilIdle()

        // First create a local workout
        viewModel.createWorkout("2025-01-01", "Notes")
        val activeId = viewModel.uiState.value.activeWorkoutId!!

        viewModel.addItemToWorkout(
            workoutId = activeId,
            exerciseId = "exercise-1",
            notes = "Keep form tight",
            order = 1
        )
        advanceUntilIdle()

        val item = viewModel.uiState.value.selectedWorkout?.items?.firstOrNull()
        assertNotNull(item)
        assertEquals("exercise-1", item?.exerciseId)
        assertEquals("Bench Press", item?.name)
        assertEquals("Keep form tight", item?.notes)

        // Verify repository NOT called (deprecated method)
        // We can't verify addWorkoutItem because it's removed from repository/api in my plan,
        // but if I removed it, I can't verify it wasn't called.
        // I can verify createWorkout wasn't called yet.
        coVerify(exactly = 0) { repository.createWorkout(any()) }
    }

    @Test
    fun logWorkoutSets_savesFullWorkoutToRepository() = runTest(mainDispatcherRule.dispatcher) {
        val repository = mockk<FitnessRepository>()
        stubBaseResponses(repository = repository)
        coEvery { repository.createWorkout(any()) } returns Result.success("server-id-123")
        coEvery { repository.fetchWorkoutDetail("server-id-123") } returns Result.success(Workout(id="server-id-123"))

        val viewModel = MainViewModel(repository)
        advanceUntilIdle()

        // Create local workout
        viewModel.createWorkout("2025-01-01", "Notes")
        val activeId = viewModel.uiState.value.activeWorkoutId!!

        // Add item
        viewModel.addItemToWorkout(activeId, "exercise-1", null, 0)
        val itemId = viewModel.uiState.value.selectedWorkout!!.items.first().id

        // Log sets (Save)
        val entries = listOf(WorkoutSetEntry(itemId, 10, 100.0, 2.0))
        viewModel.logWorkoutSets(workoutId = activeId, entries = entries)
        advanceUntilIdle()

        coVerify(exactly = 1) {
            repository.createWorkout(match { request ->
                request.date == "2025-01-01" &&
                request.notes == "Notes" &&
                request.exercises.size == 1 &&
                request.exercises[0].sets.size == 1 &&
                request.exercises[0].sets[0].reps == 10
            })
        }

        assertEquals("Workout saved", viewModel.uiState.value.infoMessage)
        assertEquals("server-id-123", viewModel.uiState.value.selectedWorkoutId)
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
