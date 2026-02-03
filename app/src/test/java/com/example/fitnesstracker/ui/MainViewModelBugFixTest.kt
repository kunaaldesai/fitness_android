package com.example.fitnesstracker.ui

import com.example.fitnesstracker.MainDispatcherRule
import com.example.fitnesstracker.data.FitnessRepository
import com.example.fitnesstracker.data.remote.User
import com.example.fitnesstracker.data.remote.Workout
import com.example.fitnesstracker.data.remote.WorkoutPlan
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelBugFixTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun startWorkoutFromPlan_doesNotSetInfoMessage() = runTest(mainDispatcherRule.dispatcher) {
        val repository = mockk<FitnessRepository>()
        val planId = "plan-1"
        val workoutPlan = WorkoutPlan(id = planId, name = "My Plan")
        val user = User(id = "user-1")

        coEvery { repository.fetchUser() } returns Result.success(user)
        coEvery { repository.fetchWorkouts(any()) } returns Result.success(emptyList())
        coEvery { repository.fetchWorkoutPlans() } returns Result.success(listOf(workoutPlan))
        coEvery { repository.fetchExercises(any()) } returns Result.success(emptyList())
        coEvery { repository.fetchWorkoutTemplate(planId) } returns Result.success(workoutPlan)

        val viewModel = MainViewModel(repository)
        advanceUntilIdle()

        viewModel.startWorkoutFromPlan(planId)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNull("Info message should be null", state.infoMessage)
        assertEquals(planId, state.activeWorkoutPlanId)
    }

    @Test
    fun selectWorkout_updatesWorkoutsList() = runTest(mainDispatcherRule.dispatcher) {
        val repository = mockk<FitnessRepository>()
        val existingWorkout = Workout(id = "old-1", date = "2025-01-01")
        val newWorkout = Workout(id = "new-1", date = "2025-01-02", notes = "Freshly fetched")
        val user = User(id = "user-1")

        coEvery { repository.fetchUser() } returns Result.success(user)
        coEvery { repository.fetchWorkouts(any()) } returns Result.success(listOf(existingWorkout))
        coEvery { repository.fetchWorkoutPlans() } returns Result.success(emptyList())
        coEvery { repository.fetchExercises(any()) } returns Result.success(emptyList())
        coEvery { repository.fetchWorkoutDetail("old-1") } returns Result.success(existingWorkout)
        coEvery { repository.fetchWorkoutDetail("new-1") } returns Result.success(newWorkout)

        val viewModel = MainViewModel(repository)
        advanceUntilIdle()

        // Initial state check
        assertEquals(1, viewModel.uiState.value.workouts.size)

        // Select the new workout (which is not in the list yet)
        viewModel.selectWorkout("new-1")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("new-1", state.selectedWorkout?.id)

        // Verification: The workouts list should now contain the new workout
        assertEquals(2, state.workouts.size)
        assertTrue(state.workouts.any { it.id == "new-1" })
        assertEquals("new-1", state.workouts.first().id) // Should be sorted by date desc
    }
}
