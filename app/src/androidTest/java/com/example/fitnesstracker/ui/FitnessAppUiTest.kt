package com.example.fitnesstracker.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.fitnesstracker.data.FitnessRepository
import com.example.fitnesstracker.data.remote.CreateExerciseRequest
import com.example.fitnesstracker.data.remote.CreateSetRequest
import com.example.fitnesstracker.data.remote.CreateWorkoutItemRequest
import com.example.fitnesstracker.data.remote.CreateWorkoutPlanRequest
import com.example.fitnesstracker.data.remote.CreateWorkoutRequest
import com.example.fitnesstracker.data.remote.Exercise
import com.example.fitnesstracker.data.remote.IdResponse
import com.example.fitnesstracker.data.remote.StartWorkoutRequest
import com.example.fitnesstracker.data.remote.UpdateSetRequest
import com.example.fitnesstracker.data.remote.User
import com.example.fitnesstracker.data.remote.UsersApi
import com.example.fitnesstracker.data.remote.Workout
import com.example.fitnesstracker.data.remote.WorkoutItem
import com.example.fitnesstracker.data.remote.WorkoutPlan
import com.example.fitnesstracker.data.remote.WorkoutSet
import com.example.fitnesstracker.data.remote.WorkoutsApi
import com.example.fitnesstracker.ui.theme.FitnessTrackerTheme
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FitnessAppUiTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun home_showsWelcomeAndRecentActivity() {
        setContentWithSampleData()

        composeRule.onNodeWithText("Welcome back,").assertIsDisplayed()
        composeRule.onNodeWithText("Recent Activity").assertIsDisplayed()
        composeRule.onNodeWithText("Leg Day").assertIsDisplayed()
    }

    @Test
    fun bottomNav_navigatesToExplore() {
        setContentWithSampleData()

        composeRule.onNodeWithText("Explore").performClick()

        composeRule.onNodeWithText("Explore Plans").assertIsDisplayed()
    }

    @Test
    fun fab_opensCreateWorkoutPlan() {
        setContentWithSampleData()

        composeRule.onNodeWithContentDescription("Toggle create menu").performClick()
        composeRule.onNodeWithText("Create workout plan").performClick()

        composeRule.onNodeWithText("Create Workout Plan").assertIsDisplayed()
    }

    @Test
    fun recentActivity_opensWorkoutDetail() {
        setContentWithSampleData()

        composeRule.onNodeWithText("Leg Day").performClick()

        composeRule.onNodeWithText("Active Workout").assertIsDisplayed()
    }

    private fun setContentWithSampleData() {
        val workouts = listOf(
            Workout(
                id = "workout-1",
                date = "2025-01-01",
                notes = "Leg Day",
                timezone = "America/New_York",
                items = listOf(
                    WorkoutItem(
                        id = "item-1",
                        exerciseId = "exercise-1",
                        name = "Squat",
                        sets = listOf(
                            WorkoutSet(
                                id = "set-1",
                                reps = 5,
                                weight = 225.0,
                                rir = 2.0,
                                rpe = 8.0,
                                isPR = true,
                                notes = "Felt strong"
                            )
                        )
                    )
                )
            )
        )
        val workoutPlans = listOf(
            WorkoutPlan(
                id = "plan-1",
                name = "Strength Starter",
                sets = 4,
                type = "strength",
                muscleGroups = listOf("legs"),
                numberOfExercises = 3,
                description = "Full body foundation.",
                exercises = JsonArray(listOf(JsonPrimitive("Squat"), JsonPrimitive("Lunge"))),
                equipment = JsonArray(listOf(JsonPrimitive("Barbell")))
            )
        )
        val exercises = listOf(
            Exercise(
                id = "exercise-1",
                name = "Squat",
                muscleGroups = listOf("legs"),
                equipment = "Barbell",
                notes = "Depth to parallel"
            )
        )
        val repository = FitnessRepository(
            userId = "user-1",
            workoutsApi = FakeWorkoutsApi(
                workouts = workouts,
                workoutPlans = workoutPlans,
                exercises = exercises
            ),
            usersApi = FakeUsersApi(User(id = "user-1", firstName = "Ava"))
        )

        composeRule.setContent {
            FitnessTrackerTheme {
                FitnessApp(viewModel = MainViewModel(repository))
            }
        }
        composeRule.waitForIdle()
    }

    private class FakeUsersApi(
        private val user: User
    ) : UsersApi {
        override suspend fun getUser(userId: String): User = user
    }

    private class FakeWorkoutsApi(
        private val workouts: List<Workout>,
        private val workoutPlans: List<WorkoutPlan>,
        private val exercises: List<Exercise>
    ) : WorkoutsApi {
        override suspend fun getWorkouts(
            userId: String,
            limit: Int?,
            startDate: String?,
            endDate: String?
        ): List<Workout> = workouts

        override suspend fun getAllWorkouts(): List<WorkoutPlan> = workoutPlans

        override suspend fun getWorkoutDetail(
            userId: String,
            workoutId: String,
            includeItems: Boolean,
            includeSets: Boolean
        ): Workout = workouts.first { it.id == workoutId }

        override suspend fun createWorkout(userId: String, body: CreateWorkoutRequest): IdResponse {
            throw UnsupportedOperationException("Not used in UI tests.")
        }

        override suspend fun startWorkout(userId: String, body: StartWorkoutRequest): IdResponse {
            throw UnsupportedOperationException("Not used in UI tests.")
        }

        override suspend fun createWorkoutPlan(body: CreateWorkoutPlanRequest): IdResponse {
            throw UnsupportedOperationException("Not used in UI tests.")
        }

        override suspend fun createExercise(userId: String, body: CreateExerciseRequest): IdResponse {
            throw UnsupportedOperationException("Not used in UI tests.")
        }

        override suspend fun getExercises(userId: String, includeArchived: Boolean): List<Exercise> = exercises

        override suspend fun addWorkoutItem(
            userId: String,
            workoutId: String,
            body: CreateWorkoutItemRequest
        ): IdResponse {
            throw UnsupportedOperationException("Not used in UI tests.")
        }

        override suspend fun addSet(
            userId: String,
            workoutId: String,
            itemId: String,
            body: CreateSetRequest
        ): IdResponse {
            throw UnsupportedOperationException("Not used in UI tests.")
        }

        override suspend fun updateSet(
            userId: String,
            workoutId: String,
            itemId: String,
            setId: String,
            body: UpdateSetRequest
        ): IdResponse {
            throw UnsupportedOperationException("Not used in UI tests.")
        }
    }
}
