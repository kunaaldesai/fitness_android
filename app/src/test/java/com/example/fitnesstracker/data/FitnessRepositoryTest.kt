package com.example.fitnesstracker.data

import com.example.fitnesstracker.data.remote.CreateWorkoutItemRequest
import com.example.fitnesstracker.data.remote.CreateWorkoutRequest
import com.example.fitnesstracker.data.remote.IdResponse
import com.example.fitnesstracker.data.remote.StartWorkoutRequest
import com.example.fitnesstracker.data.remote.User
import com.example.fitnesstracker.data.remote.UsersApi
import com.example.fitnesstracker.data.remote.WorkoutsApi
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FitnessRepositoryTest {

    private val workoutsApi = mockk<WorkoutsApi>()
    private val usersApi = mockk<UsersApi>()
    private val repository = FitnessRepository(
        userId = "user-123",
        workoutsApi = workoutsApi,
        usersApi = usersApi
    )

    @Test
    fun fetchUser_returnsUserResult() = runTest {
        val user = User(id = "user-123", firstName = "Sam")
        coEvery { usersApi.getUser("user-123") } returns user

        val result = repository.fetchUser()

        assertTrue(result.isSuccess)
        assertEquals(user, result.getOrNull())
        coVerify(exactly = 1) { usersApi.getUser("user-123") }
    }

    @Test
    fun fetchExercises_passesIncludeArchivedFlag() = runTest {
        coEvery { workoutsApi.getExercises("user-123", true) } returns emptyList()

        val result = repository.fetchExercises(includeArchived = true)

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { workoutsApi.getExercises("user-123", true) }
    }

    @Test
    fun createWorkout_sendsRequestAndReturnsId() = runTest {
        val requestSlot = slot<CreateWorkoutRequest>()
        coEvery { workoutsApi.createWorkout("user-123", capture(requestSlot)) } returns IdResponse(id = "workout-1")

        val result = repository.createWorkout(
            date = "2025-01-01",
            notes = "Leg day",
            timezone = "America/Chicago",
            startTime = "08:00",
            endTime = "09:00"
        )

        assertTrue(result.isSuccess)
        assertEquals("workout-1", result.getOrNull())
        assertEquals("2025-01-01", requestSlot.captured.date)
        assertEquals("Leg day", requestSlot.captured.notes)
        assertEquals("America/Chicago", requestSlot.captured.timezone)
        assertEquals("08:00", requestSlot.captured.startTime)
        assertEquals("09:00", requestSlot.captured.endTime)
    }

    @Test
    fun createWorkout_missingIdFailsResult() = runTest {
        coEvery { workoutsApi.createWorkout("user-123", any()) } returns IdResponse(id = null)

        val result = repository.createWorkout(date = "2025-01-01")

        assertTrue(result.isFailure)
    }

    @Test
    fun addWorkoutItem_passesExerciseName() = runTest {
        val requestSlot = slot<CreateWorkoutItemRequest>()
        coEvery { workoutsApi.addWorkoutItem("user-123", "workout-1", capture(requestSlot)) } returns IdResponse(id = "item-1")

        val result = repository.addWorkoutItem(
            workoutId = "workout-1",
            exerciseId = "exercise-1",
            notes = "Keep form strict",
            order = 2,
            exerciseName = "Bench Press"
        )

        assertTrue(result.isSuccess)
        assertEquals("Bench Press", requestSlot.captured.name)
        assertEquals("Keep form strict", requestSlot.captured.notes)
        assertEquals(2, requestSlot.captured.order)
    }

    @Test
    fun startWorkout_sendsTemplateIdDateAndTimezone() = runTest {
        val requestSlot = slot<StartWorkoutRequest>()
        coEvery { workoutsApi.startWorkout("user-123", capture(requestSlot)) } returns IdResponse(id = "workout-42")

        val result = repository.startWorkout(
            workoutPlanId = "plan-9",
            date = "2025-02-01",
            timezone = "America/New_York"
        )

        assertTrue(result.isSuccess)
        assertEquals("workout-42", result.getOrNull())
        assertEquals("plan-9", requestSlot.captured.workoutId)
        assertEquals("2025-02-01", requestSlot.captured.date)
        assertEquals("America/New_York", requestSlot.captured.timezone)
    }
}
