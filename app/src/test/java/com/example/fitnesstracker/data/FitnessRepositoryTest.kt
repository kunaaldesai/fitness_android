package com.example.fitnesstracker.data

import com.example.fitnesstracker.data.remote.CreateWorkoutRequest
import com.example.fitnesstracker.data.remote.IdResponse
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

        val request = CreateWorkoutRequest(
            date = "2025-01-01",
            notes = "Leg day",
            timezone = "America/Chicago"
        )
        val result = repository.createWorkout(request)

        assertTrue(result.isSuccess)
        assertEquals("workout-1", result.getOrNull())
        assertEquals("2025-01-01", requestSlot.captured.date)
        assertEquals("Leg day", requestSlot.captured.notes)
        assertEquals("America/Chicago", requestSlot.captured.timezone)
    }

    @Test
    fun createWorkout_missingIdFailsResult() = runTest {
        coEvery { workoutsApi.createWorkout("user-123", any()) } returns IdResponse(id = null)

        val result = repository.createWorkout(CreateWorkoutRequest(date = "2025-01-01"))

        assertTrue(result.isFailure)
    }
}
