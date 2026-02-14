@file:OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)

package com.example.fitnesstracker.ui

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.DirectionsRun
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Pool
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.UUID
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fitnesstracker.data.remote.Exercise
import com.example.fitnesstracker.data.remote.Workout
import com.example.fitnesstracker.data.remote.WorkoutItem
import com.example.fitnesstracker.data.remote.WorkoutPlan
import com.example.fitnesstracker.data.remote.WorkoutSet
import com.example.fitnesstracker.ui.theme.Blue500
import com.example.fitnesstracker.ui.theme.Orange500
import com.example.fitnesstracker.ui.theme.Purple500
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.delay
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

private val compactWidthThreshold = 520.dp

private data class WorkoutPlanHighlight(
    val title: String,
    val subtitle: String,
    val exerciseCount: String,
    val setCount: String,
    val badge: String,
    val description: String? = null,
    val exercisePreview: String? = null
)

private fun WorkoutPlan.toHighlight(): WorkoutPlanHighlight {
    val titleText = name?.trim().takeUnless { it.isNullOrBlank() } ?: "Upper Body Power"
    val typeLabel = type?.trim().takeUnless { it.isNullOrBlank() }?.toDisplayLabel()
    val muscleLabels = muscleGroups.mapNotNull { it.trim().takeIf { label -> label.isNotBlank() } }
        .map { it.toDisplayLabel() }
    val muscleLabel = when {
        muscleLabels.isEmpty() -> null
        muscleLabels.size == 1 -> muscleLabels.first()
        muscleLabels.size == 2 -> muscleLabels.joinToString(" + ")
        else -> muscleLabels.take(2).joinToString(" + ") + " +${muscleLabels.size - 2}"
    }
    val subtitleText = listOfNotNull(typeLabel, muscleLabel).joinToString(" - ").ifBlank { "Workout Plan" }
    val exerciseList = exercises.toStringList()
    val exercisesCountValue = numberOfExercises ?: exerciseList.size.takeIf { it > 0 } ?: 0
    val setsCountValue = sets ?: 0
    val exercisePreview = exerciseList.take(3).joinToString(", ").takeIf { it.isNotBlank() }
        ?.let { "Exercises: $it" }

    return WorkoutPlanHighlight(
        title = titleText,
        subtitle = subtitleText,
        exerciseCount = "$exercisesCountValue exercises",
        setCount = "$setsCountValue sets",
        badge = "Workout Plan",
        description = description?.trim().takeUnless { it.isNullOrBlank() },
        exercisePreview = exercisePreview
    )
}

private fun JsonElement?.toStringList(): List<String> {
    return when (this) {
        is JsonArray -> mapNotNull { (it as? JsonPrimitive)?.contentOrNull }
        is JsonObject -> entries.sortedBy { it.key }
            .mapNotNull { (it.value as? JsonPrimitive)?.contentOrNull }
        is JsonPrimitive -> contentOrNull?.let { listOf(it) }.orEmpty()
        else -> emptyList()
    }.map { it.trim() }.filter { it.isNotBlank() }
}

private fun String.toDisplayLabel(): String =
    replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

private data class ProgressDay(
    val label: String,
    val value: String,
    val progress: Float,
    val isActive: Boolean = false
)

private data class ActivityItem(
    val title: String,
    val subtitle: String,
    val primary: String,
    val secondary: String,
    val icon: ImageVector,
    val accent: Color,
    val workoutId: String? = null
)

private data class NavItem(
    val label: String,
    val icon: ImageVector
)

private data class LapEntry(
    val label: String,
    val lapMs: Long,
    val totalMs: Long,
    val isAuto: Boolean
)

private enum class TimerMode {
    Stopwatch,
    Timer
}

private val bottomNavItems = listOf(
    NavItem("Home", Icons.Rounded.Home),
    NavItem("Explore", Icons.Rounded.Explore),
    NavItem("Analytics", Icons.Rounded.BarChart),
    NavItem("Profile", Icons.Rounded.Person)
)

sealed interface FitnessDestination {
    data object Home : FitnessDestination
    data object Explore : FitnessDestination
    data object CreateWorkout : FitnessDestination
    data object CreateWorkoutPlan : FitnessDestination
    data class StartWorkout(val planId: String) : FitnessDestination
    data class WorkoutDetail(val id: String) : FitnessDestination
    data object WorkoutHistory : FitnessDestination
    data object CreateExercise : FitnessDestination
    data object Profile : FitnessDestination
}

@Composable
fun FitnessApp(viewModel: MainViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var destination by remember { mutableStateOf<FitnessDestination>(FitnessDestination.Home) }
    val isHome = destination == FitnessDestination.Home
    val showBottomNav = destination == FitnessDestination.Home || destination == FitnessDestination.Explore || destination == FitnessDestination.Profile

    state.errorMessage?.let { message ->
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessage()
        }
    }
    state.infoMessage?.let { message ->
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessage()
        }
    }

    LaunchedEffect(state.recentlyCreatedWorkoutId) {
        state.recentlyCreatedWorkoutId?.let { id ->
            destination = FitnessDestination.WorkoutDetail(id)
            viewModel.consumeRecentlyCreatedWorkout()
        }
    }

    LaunchedEffect(state.recentlyCompletedWorkoutId) {
        state.recentlyCompletedWorkoutId?.let { id ->
            destination = FitnessDestination.WorkoutDetail(id)
            viewModel.consumeRecentlyCompletedWorkout()
            viewModel.clearActiveWorkout()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (isHome) {
                HomeFloatingActionButton(onCreateWorkoutPlan = { destination = FitnessDestination.CreateWorkoutPlan })
            }
        },
        bottomBar = {
            if (showBottomNav) {
                val selectedIndex = when (destination) {
                    FitnessDestination.Home -> 0
                    FitnessDestination.Explore -> 1
                    FitnessDestination.Profile -> 3
                    else -> 0
                }
                BottomNavBar(
                    selectedIndex = selectedIndex,
                    onSelect = { index ->
                        destination = when (index) {
                            0 -> FitnessDestination.Home
                            1 -> FitnessDestination.Explore
                            3 -> FitnessDestination.Profile
                            else -> destination
                        }
                    }
                )
            }
        }
    ) { padding ->
        when (val screen = destination) {
            FitnessDestination.Home -> HomeScreen(
                state = state,
                onOpenCreateWorkout = { destination = FitnessDestination.CreateWorkout },
                onStartWorkoutPlan = { plan -> destination = FitnessDestination.StartWorkout(plan.id) },
                onOpenHistory = { destination = FitnessDestination.WorkoutHistory },
                onOpenWorkout = { workoutId ->
                    viewModel.selectWorkout(workoutId)
                    destination = FitnessDestination.WorkoutDetail(workoutId)
                },
                modifier = Modifier.padding(padding)
            )

            FitnessDestination.Explore -> ExploreScreen(
                state = state,
                onStartWorkoutPlan = { plan -> destination = FitnessDestination.StartWorkout(plan.id) },
                onCreateWorkoutPlan = { destination = FitnessDestination.CreateWorkoutPlan },
                modifier = Modifier.padding(padding)
            )

            FitnessDestination.CreateWorkout -> CreateWorkoutScreen(
                onBack = { destination = FitnessDestination.Home },
                isSubmitting = state.isActionRunning,
                initialDate = LocalDate.now(),
                onCreateWorkout = { date, notes, timezone ->
                    viewModel.createWorkout(date, notes, timezone)
                },
                modifier = Modifier.padding(padding)
            )

            FitnessDestination.CreateWorkoutPlan -> CreateWorkoutPlanScreen(
                onBack = { destination = FitnessDestination.Home },
                isSubmitting = state.isActionRunning,
                onCreateWorkoutPlan = viewModel::createWorkoutPlan,
                showSuccess = state.recentlyCreatedWorkoutPlanId != null,
                onSuccessComplete = {
                    viewModel.consumeRecentlyCreatedWorkoutPlan()
                    destination = FitnessDestination.Home
                },
                modifier = Modifier.padding(padding)
            )

            is FitnessDestination.StartWorkout -> StartWorkoutScreen(
                planId = screen.planId,
                state = state,
                onBack = {
                    viewModel.clearActiveWorkout()
                    destination = FitnessDestination.Home
                },
                onStartWorkout = viewModel::startWorkoutFromPlan,
                onSaveWorkout = viewModel::logWorkoutSets,
                modifier = Modifier.padding(padding)
            )

            is FitnessDestination.WorkoutDetail -> WorkoutDetailScreen(
                workoutId = screen.id,
                state = state,
                onBack = { destination = FitnessDestination.Home },
                onSaveEdits = viewModel::updateWorkoutLog,
                onRefresh = { viewModel.selectWorkout(screen.id) },
                modifier = Modifier.padding(padding)
            )

            FitnessDestination.WorkoutHistory -> WorkoutHistoryScreen(
                state = state,
                onBack = { destination = FitnessDestination.Home },
                onSelectWorkout = { workoutId ->
                    viewModel.selectWorkout(workoutId)
                    destination = FitnessDestination.WorkoutDetail(workoutId)
                },
                modifier = Modifier.padding(padding)
            )

            FitnessDestination.Profile -> ProfileScreen(
                state = state,
                onEditProfile = viewModel::updateUser,
                modifier = Modifier.padding(padding)
            )

            FitnessDestination.CreateExercise -> CreateExerciseScreen(
                onBack = {
                    destination = state.selectedWorkoutId?.let { FitnessDestination.WorkoutDetail(it) }
                        ?: FitnessDestination.Home
                },
                isSubmitting = state.isActionRunning,
                onCreateExercise = viewModel::createExercise,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
private fun HomeScreen(
    state: FitnessUiState,
    onOpenCreateWorkout: () -> Unit,
    onStartWorkoutPlan: (WorkoutPlan) -> Unit,
    onOpenHistory: () -> Unit,
    onOpenWorkout: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val displayName = state.user?.firstName?.takeIf { it.isNotBlank() } ?: "Kunaal"
    val workoutsWithSets = remember(state.workouts) {
        state.workouts.filter { workout -> workout.items.sumOf { it.sets.size } > 0 }
    }
    val streakCount = remember(workoutsWithSets) {
        if (workoutsWithSets.isNotEmpty()) workoutsWithSets.size.coerceAtMost(12) else 12
    }
    val workoutPlans = state.workoutPlans
    val hasWorkoutPlans = workoutPlans.isNotEmpty()
    val progressDays = listOf(
        ProgressDay("Mon", "20 min", 0.4f),
        ProgressDay("Tue", "45 min", 0.7f),
        ProgressDay("Wed", "15 min", 0.3f),
        ProgressDay("Thu", "60 min", 0.85f, isActive = true),
        ProgressDay("Fri", "30 min", 0.5f),
        ProgressDay("Sat", "10 min", 0.2f),
        ProgressDay("Sun", "Rest", 0.1f)
    )
    val activityItems = remember(workoutsWithSets) {
        if (workoutsWithSets.isNotEmpty()) {
            val icons = listOf(Icons.AutoMirrored.Rounded.DirectionsRun, Icons.Rounded.SelfImprovement, Icons.Rounded.Pool)
            val accents = listOf(Orange500, Purple500, Blue500)
            workoutsWithSets.sortedByDescending { it.date ?: "" }
                .take(3)
                .mapIndexed { index, workout ->
                    ActivityItem(
                        title = workout.notes?.takeIf { it.isNotBlank() } ?: "Workout Session",
                        subtitle = workout.date?.let { "Logged on $it" } ?: "Recent activity",
                        primary = "${workout.items.size} exercises",
                        secondary = "${workout.items.sumOf { it.sets.size }} sets",
                        icon = icons[index % icons.size],
                        accent = accents[index % accents.size],
                        workoutId = workout.id
                    )
                }
        } else {
            listOf(
                ActivityItem(
                    title = "Morning Run",
                    subtitle = "Yesterday, 6:30 AM",
                    primary = "5.2 km",
                    secondary = "32 min",
                    icon = Icons.AutoMirrored.Rounded.DirectionsRun,
                    accent = Orange500
                ),
                ActivityItem(
                    title = "Yoga Flow",
                    subtitle = "Oct 24, 5:00 PM",
                    primary = "30 min",
                    secondary = "120 kcal",
                    icon = Icons.Rounded.SelfImprovement,
                    accent = Purple500
                ),
                ActivityItem(
                    title = "Swimming",
                    subtitle = "Oct 22, 7:00 AM",
                    primary = "1200 m",
                    secondary = "45 min",
                    icon = Icons.Rounded.Pool,
                    accent = Blue500
                )
            )
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isCompact = maxWidth < compactWidthThreshold
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                MaterialTheme.colorScheme.background,
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                            )
                        )
                    )
            )
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .offset(x = 180.dp, y = (-60).dp)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .offset(x = (-80).dp, y = 320.dp)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                Blue500.copy(alpha = 0.12f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 140.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                item {
                    StaggeredItem(delayMillis = 0) {
                        HomeHeader(
                            userName = displayName,
                            streakCount = streakCount,
                            notificationCount = 1,
                            showStreakInline = !isCompact
                        )
                    }
                }
                item {
                    StaggeredItem(delayMillis = 200) {
                        TodayWorkoutSection(
                            workoutPlans = workoutPlans,
                            onStartWorkoutPlan = onStartWorkoutPlan,
                            onCreateWorkout = onOpenCreateWorkout,
                            ctaLabel = if (hasWorkoutPlans) "Start Workout" else "Create Workout"
                        )
                    }
                }
                item {
                    StaggeredItem(delayMillis = 260) {
                        StopwatchTimerCard(modifier = Modifier.fillMaxWidth())
                    }
                }
                item {
                    StaggeredItem(delayMillis = 320) {
                        ProgressOverviewSection(days = progressDays)
                    }
                }
                item {
                    StaggeredItem(delayMillis = 380) {
                        RecentActivitySection(
                            activities = activityItems,
                            onSeeAll = onOpenHistory,
                            onSelectWorkout = onOpenWorkout
                        )
                    }
                }
                if (state.isLoading) {
                    item {
                        StaggeredItem(delayMillis = 440) {
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExploreScreen(
    state: FitnessUiState,
    onStartWorkoutPlan: (WorkoutPlan) -> Unit,
    onCreateWorkoutPlan: () -> Unit,
    modifier: Modifier = Modifier
) {
    val workoutPlans = state.workoutPlans
    val hasPlans = workoutPlans.isNotEmpty()

    Box(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                        )
                    )
                )
        )
        Box(
            modifier = Modifier
                .size(220.dp)
                .offset(x = 160.dp, y = (-40).dp)
                .background(
                    Brush.radialGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 140.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                StaggeredItem(delayMillis = 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Explore Plans",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                if (hasPlans) "All workout plans in your library." else "No plans yet. Create your first plan.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.Rounded.Explore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            if (!hasPlans) {
                item {
                    StaggeredItem(delayMillis = 120) {
                        WorkoutPlanCard(
                            highlight = WorkoutPlanHighlight(
                                title = "Build a Plan",
                                subtitle = "Start with your goal",
                                exerciseCount = "0 exercises",
                                setCount = "0 sets",
                                badge = "Workout Plan",
                                description = "Create a plan to see it here."
                            ),
                            onStartWorkout = onCreateWorkoutPlan,
                            ctaLabel = "Create Plan",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            } else {
                items(workoutPlans, key = { it.id }) { plan ->
                    StaggeredItem(delayMillis = 120) {
                        WorkoutPlanCard(
                            highlight = plan.toHighlight(),
                            onStartWorkout = { onStartWorkoutPlan(plan) },
                            ctaLabel = "Start Workout",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun HomeHeader(
    userName: String,
    streakCount: Int,
    notificationCount: Int,
    showStreakInline: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ProfileAvatar(userName = userName)
            Column {
                Text(
                    "Welcome back,",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    userName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (showStreakInline) {
                StreakBadge(streakCount = streakCount)
            }
            NotificationBell(notificationCount = notificationCount)
        }
    }
}


@Composable
private fun StreakBadge(streakCount: Int, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.LocalFireDepartment,
                contentDescription = null,
                tint = Orange500,
                modifier = Modifier.size(18.dp)
            )
            Text(
                streakCount.toString(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun NotificationBell(notificationCount: Int, modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        IconButton(onClick = { }) {
            Icon(
                imageVector = Icons.Rounded.Notifications,
                contentDescription = "Notifications"
            )
        }
        if (notificationCount > 0) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .align(Alignment.TopEnd)
                    .background(Color(0xFFEF4444), CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.background, CircleShape)
            )
        }
    }
}

@Composable
private fun DailyStreakCard(streakCount: Int, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Orange500.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.LocalFireDepartment,
                        contentDescription = null,
                        tint = Orange500,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text("Daily Streak", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            }
            Text("$streakCount Days", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun StatsSummaryRow(stats: List<StatSummary>, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        stats.forEach { summary ->
            StatSummaryCard(summary = summary, modifier = Modifier.weight(1f))
        }
    }
}


@Composable
private fun TodayWorkoutSection(
    workoutPlans: List<WorkoutPlan>,
    onStartWorkoutPlan: (WorkoutPlan) -> Unit,
    onCreateWorkout: () -> Unit,
    ctaLabel: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Today's Workout",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                "View Plan",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        if (workoutPlans.isEmpty()) {
            WorkoutPlanCard(
                highlight = WorkoutPlanHighlight(
                    title = "Upper Body Power",
                    subtitle = "Strength - Chest",
                    exerciseCount = "6 exercises",
                    setCount = "24 sets",
                    badge = "Workout Plan"
                ),
                onStartWorkout = onCreateWorkout,
                ctaLabel = ctaLabel,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            val pagerState = rememberPagerState(pageCount = { workoutPlans.size })
            HorizontalPager(
                state = pagerState,
                contentPadding = PaddingValues(horizontal = 4.dp),
                pageSpacing = 14.dp,
                modifier = Modifier.fillMaxWidth()
            ) { page ->
                WorkoutPlanCard(
                    highlight = workoutPlans[page].toHighlight(),
                    onStartWorkout = { onStartWorkoutPlan(workoutPlans[page]) },
                    ctaLabel = ctaLabel,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun StopwatchTimerCard(modifier: Modifier = Modifier) {
    var mode by rememberSaveable { mutableStateOf(TimerMode.Stopwatch) }
    var stopwatchRunning by rememberSaveable { mutableStateOf(false) }
    var stopwatchElapsedMs by rememberSaveable { mutableStateOf(0L) }
    var stopwatchStartMs by rememberSaveable { mutableStateOf(0L) }
    var lapEnabled by rememberSaveable { mutableStateOf(true) }
    var intervalsEnabled by rememberSaveable { mutableStateOf(false) }
    val lapEntries = remember { mutableStateListOf<LapEntry>() }
    var lapMarkMs by rememberSaveable { mutableStateOf(0L) }
    var lapCount by rememberSaveable { mutableStateOf(0) }
    var intervalCount by rememberSaveable { mutableStateOf(0) }
    var intervalLengthMs by rememberSaveable { mutableStateOf(30_000L) }
    var lastIntervalIndex by rememberSaveable { mutableStateOf(0) }
    var timerRunning by rememberSaveable { mutableStateOf(false) }
    var timerTotalMs by rememberSaveable { mutableStateOf(3 * 60 * 1000L) }
    var timerRemainingMs by rememberSaveable { mutableStateOf(3 * 60 * 1000L) }
    var timerStartMs by rememberSaveable { mutableStateOf(0L) }
    var customMinutesText by rememberSaveable { mutableStateOf("03") }
    var customSecondsText by rememberSaveable { mutableStateOf("00") }
    val accent = if (mode == TimerMode.Stopwatch) {
        MaterialTheme.colorScheme.tertiary
    } else {
        MaterialTheme.colorScheme.secondary
    }
    val stopwatchProgress = ((stopwatchElapsedMs % 60_000L).toFloat() / 60_000f).coerceIn(0f, 1f)
    val timerProgress = if (timerTotalMs > 0L) {
        (timerRemainingMs.toFloat() / timerTotalMs.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }
    val intervalRemainingMs = if (intervalLengthMs > 0L) {
        val remainder = intervalLengthMs - (stopwatchElapsedMs % intervalLengthMs)
        if (remainder == intervalLengthMs) intervalLengthMs else remainder
    } else {
        0L
    }
    val intervalRemainingLabel = formatMinutesSeconds(intervalRemainingMs)
    val ringProgress by animateFloatAsState(
        targetValue = if (mode == TimerMode.Stopwatch) stopwatchProgress else timerProgress,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "stopwatchTimerRing"
    )
    val stopwatchLabel = formatMinutesSeconds(stopwatchElapsedMs)
    val timerLabel = formatMinutesSeconds(timerRemainingMs)
    val customMinutes = customMinutesText.toIntOrNull()?.coerceAtLeast(0) ?: 0
    val customSeconds = customSecondsText.toIntOrNull()?.coerceIn(0, 59) ?: 0
    val customTotalMs = (customMinutes * 60L + customSeconds) * 1000L
    val customSetEnabled = customTotalMs > 0L
    val onModeChange: (TimerMode) -> Unit = { newMode ->
        if (newMode != mode) {
            mode = newMode
        }
    }
    fun addLapEntry(isAuto: Boolean) {
        if (stopwatchElapsedMs <= 0L) return
        val lapMs = (stopwatchElapsedMs - lapMarkMs).coerceAtLeast(0L)
        lapMarkMs = stopwatchElapsedMs
        val label = if (isAuto) {
            val nextIndex = intervalCount + 1
            intervalCount = nextIndex
            "Interval $nextIndex"
        } else {
            val nextIndex = lapCount + 1
            lapCount = nextIndex
            "Lap $nextIndex"
        }
        lapEntries.add(
            0,
            LapEntry(
                label = label,
                lapMs = lapMs,
                totalMs = stopwatchElapsedMs,
                isAuto = isAuto
            )
        )
    }

    LaunchedEffect(stopwatchRunning, intervalsEnabled, intervalLengthMs) {
        if (!stopwatchRunning) return@LaunchedEffect
        while (stopwatchRunning) {
            val now = System.currentTimeMillis()
            stopwatchElapsedMs = now - stopwatchStartMs
            if (intervalsEnabled && intervalLengthMs > 0L) {
                val currentIndex = (stopwatchElapsedMs / intervalLengthMs).toInt()
                if (currentIndex > lastIntervalIndex && stopwatchElapsedMs >= intervalLengthMs) {
                    lastIntervalIndex = currentIndex
                    addLapEntry(true)
                }
            }
            delay(100L)
        }
    }

    LaunchedEffect(timerRunning, timerTotalMs) {
        if (!timerRunning) return@LaunchedEffect
        while (timerRunning) {
            val now = System.currentTimeMillis()
            val elapsed = now - timerStartMs
            val remaining = (timerTotalMs - elapsed).coerceAtLeast(0L)
            timerRemainingMs = remaining
            if (remaining == 0L) {
                timerRunning = false
            }
            delay(100L)
        }
    }
    val onToggleStopwatch = {
        if (stopwatchRunning) {
            stopwatchRunning = false
        } else {
            stopwatchStartMs = System.currentTimeMillis() - stopwatchElapsedMs
            lastIntervalIndex = if (intervalLengthMs > 0L) {
                (stopwatchElapsedMs / intervalLengthMs).toInt()
            } else {
                0
            }
            stopwatchRunning = true
        }
    }
    val onResetStopwatch = {
        stopwatchRunning = false
        stopwatchElapsedMs = 0L
        lapEntries.clear()
        lapMarkMs = 0L
        lapCount = 0
        intervalCount = 0
        lastIntervalIndex = 0
    }
    val onToggleLap = {
        lapEnabled = !lapEnabled
    }
    val onToggleIntervals = {
        val newValue = !intervalsEnabled
        intervalsEnabled = newValue
        if (newValue) {
            lastIntervalIndex = if (intervalLengthMs > 0L) {
                (stopwatchElapsedMs / intervalLengthMs).toInt()
            } else {
                0
            }
        }
    }
    val onLap = {
        if (lapEnabled && stopwatchRunning) {
            addLapEntry(false)
        }
    }
    val onToggleTimer = {
        if (timerRunning) {
            timerRunning = false
        } else {
            if (timerRemainingMs <= 0L) {
                timerRemainingMs = timerTotalMs
            }
            timerStartMs = System.currentTimeMillis() - (timerTotalMs - timerRemainingMs)
            timerRunning = true
        }
    }
    val onResetTimer = {
        timerRunning = false
        timerRemainingMs = timerTotalMs
    }
    val onSelectInterval: (Long) -> Unit = { intervalMs ->
        intervalLengthMs = intervalMs
        lastIntervalIndex = if (intervalMs > 0L) {
            (stopwatchElapsedMs / intervalMs).toInt()
        } else {
            0
        }
    }
    val onSelectTimerPreset: (Long) -> Unit = { preset ->
        timerRunning = false
        timerTotalMs = preset
        timerRemainingMs = preset
        val totalSeconds = preset / 1000L
        customMinutesText = (totalSeconds / 60L).toString().padStart(2, '0')
        customSecondsText = (totalSeconds % 60L).toString().padStart(2, '0')
    }
    val onSetCustomTimer = {
        if (customTotalMs > 0L) {
            timerRunning = false
            timerTotalMs = customTotalMs
            timerRemainingMs = customTotalMs
            customMinutesText = customMinutes.toString().padStart(2, '0')
            customSecondsText = customSeconds.toString().padStart(2, '0')
        }
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                        )
                    )
                )
        ) {
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 70.dp, y = (-60).dp)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                accent.copy(alpha = 0.22f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Stopwatch + Timer",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Intervals and countdowns, ready for training blocks.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(accent.copy(alpha = 0.15f), RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Timer,
                            contentDescription = null,
                            tint = accent
                        )
                    }
                }
                StopwatchTimerModeToggle(
                    mode = mode,
                    onModeChange = onModeChange,
                    accent = accent,
                    modifier = Modifier.fillMaxWidth()
                )
                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val isCompact = maxWidth < compactWidthThreshold
                    if (isCompact) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            StopwatchTimerDial(
                                timeLabel = if (mode == TimerMode.Stopwatch) stopwatchLabel else timerLabel,
                                subLabel = if (mode == TimerMode.Stopwatch) "min:sec" else "countdown",
                                accent = accent,
                                progress = ringProgress
                            )
                            StopwatchTimerDetails(
                                mode = mode,
                                accent = accent,
                                stopwatchRunning = stopwatchRunning,
                                stopwatchElapsedMs = stopwatchElapsedMs,
                                lapEnabled = lapEnabled,
                                intervalsEnabled = intervalsEnabled,
                                onToggleStopwatch = onToggleStopwatch,
                                onResetStopwatch = onResetStopwatch,
                                onToggleLap = onToggleLap,
                                onToggleIntervals = onToggleIntervals,
                                onLap = onLap,
                                lapEntries = lapEntries,
                                intervalLengthMs = intervalLengthMs,
                                intervalRemainingLabel = intervalRemainingLabel,
                                onSelectInterval = onSelectInterval,
                                timerRunning = timerRunning,
                                timerTotalMs = timerTotalMs,
                                timerRemainingMs = timerRemainingMs,
                                onToggleTimer = onToggleTimer,
                                onResetTimer = onResetTimer,
                                onSelectTimerPreset = onSelectTimerPreset,
                                customMinutesText = customMinutesText,
                                customSecondsText = customSecondsText,
                                onCustomMinutesChange = { value ->
                                    customMinutesText = value.filter { it.isDigit() }.take(2)
                                },
                                onCustomSecondsChange = { value ->
                                    customSecondsText = value.filter { it.isDigit() }.take(2)
                                },
                                onSetCustomTimer = onSetCustomTimer,
                                customSetEnabled = customSetEnabled,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            StopwatchTimerDial(
                                timeLabel = if (mode == TimerMode.Stopwatch) stopwatchLabel else timerLabel,
                                subLabel = if (mode == TimerMode.Stopwatch) "min:sec" else "countdown",
                                accent = accent,
                                progress = ringProgress
                            )
                            StopwatchTimerDetails(
                                mode = mode,
                                accent = accent,
                                stopwatchRunning = stopwatchRunning,
                                stopwatchElapsedMs = stopwatchElapsedMs,
                                lapEnabled = lapEnabled,
                                intervalsEnabled = intervalsEnabled,
                                onToggleStopwatch = onToggleStopwatch,
                                onResetStopwatch = onResetStopwatch,
                                onToggleLap = onToggleLap,
                                onToggleIntervals = onToggleIntervals,
                                onLap = onLap,
                                lapEntries = lapEntries,
                                intervalLengthMs = intervalLengthMs,
                                intervalRemainingLabel = intervalRemainingLabel,
                                onSelectInterval = onSelectInterval,
                                timerRunning = timerRunning,
                                timerTotalMs = timerTotalMs,
                                timerRemainingMs = timerRemainingMs,
                                onToggleTimer = onToggleTimer,
                                onResetTimer = onResetTimer,
                                onSelectTimerPreset = onSelectTimerPreset,
                                customMinutesText = customMinutesText,
                                customSecondsText = customSecondsText,
                                onCustomMinutesChange = { value ->
                                    customMinutesText = value.filter { it.isDigit() }.take(2)
                                },
                                onCustomSecondsChange = { value ->
                                    customSecondsText = value.filter { it.isDigit() }.take(2)
                                },
                                onSetCustomTimer = onSetCustomTimer,
                                customSetEnabled = customSetEnabled,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StopwatchTimerModeToggle(
    mode: TimerMode,
    onModeChange: (TimerMode) -> Unit,
    accent: Color,
    modifier: Modifier = Modifier
) {
    val indicatorProgress by animateFloatAsState(
        targetValue = if (mode == TimerMode.Timer) 1f else 0f,
        animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing),
        label = "stopwatchTimerToggle"
    )
    BoxWithConstraints(
        modifier = modifier
            .height(46.dp)
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                RoundedCornerShape(999.dp)
            )
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(999.dp))
    ) {
        val inset = 4.dp
        val indicatorWidth = (maxWidth - inset * 2) / 2
        val indicatorHeight = maxHeight - inset * 2
        val indicatorOffset = inset + indicatorWidth * indicatorProgress
        Box(
            modifier = Modifier
                .offset(x = indicatorOffset, y = inset)
                .width(indicatorWidth)
                .height(indicatorHeight)
                .background(
                    Brush.horizontalGradient(listOf(accent, accent.copy(alpha = 0.7f))),
                    RoundedCornerShape(999.dp)
                )
        )
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = inset),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TimerModeOption(
                label = "Stopwatch",
                selected = mode == TimerMode.Stopwatch,
                onClick = { onModeChange(TimerMode.Stopwatch) },
                modifier = Modifier.weight(1f)
            )
            TimerModeOption(
                label = "Timer",
                selected = mode == TimerMode.Timer,
                onClick = { onModeChange(TimerMode.Timer) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TimerModeOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun StopwatchTimerDial(
    timeLabel: String,
    subLabel: String,
    accent: Color,
    progress: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(104.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 8.dp.toPx()
            drawCircle(
                color = accent.copy(alpha = 0.18f),
                style = Stroke(width = stroke)
            )
            drawArc(
                color = accent,
                startAngle = -90f,
                sweepAngle = progress * 360f,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = timeLabel,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subLabel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StopwatchTimerDetails(
    mode: TimerMode,
    accent: Color,
    stopwatchRunning: Boolean,
    stopwatchElapsedMs: Long,
    lapEnabled: Boolean,
    intervalsEnabled: Boolean,
    onToggleStopwatch: () -> Unit,
    onResetStopwatch: () -> Unit,
    onToggleLap: () -> Unit,
    onToggleIntervals: () -> Unit,
    onLap: () -> Unit,
    lapEntries: List<LapEntry>,
    intervalLengthMs: Long,
    intervalRemainingLabel: String,
    onSelectInterval: (Long) -> Unit,
    timerRunning: Boolean,
    timerTotalMs: Long,
    timerRemainingMs: Long,
    onToggleTimer: () -> Unit,
    onResetTimer: () -> Unit,
    onSelectTimerPreset: (Long) -> Unit,
    customMinutesText: String,
    customSecondsText: String,
    onCustomMinutesChange: (String) -> Unit,
    onCustomSecondsChange: (String) -> Unit,
    onSetCustomTimer: () -> Unit,
    customSetEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        AnimatedVisibility(
            visible = mode == TimerMode.Stopwatch,
            enter = fadeIn(animationSpec = tween(180)) + scaleIn(
                animationSpec = tween(220),
                initialScale = 0.98f
            ),
            exit = fadeOut(animationSpec = tween(160)) + scaleOut(
                animationSpec = tween(160),
                targetScale = 0.98f
            )
        ) {
            StopwatchModeDetails(
                accent = accent,
                isRunning = stopwatchRunning,
                elapsedMs = stopwatchElapsedMs,
                lapEnabled = lapEnabled,
                intervalsEnabled = intervalsEnabled,
                lapEntries = lapEntries,
                intervalLengthMs = intervalLengthMs,
                intervalRemainingLabel = intervalRemainingLabel,
                onToggleRunning = onToggleStopwatch,
                onReset = onResetStopwatch,
                onToggleLap = onToggleLap,
                onToggleIntervals = onToggleIntervals,
                onLap = onLap,
                onSelectInterval = onSelectInterval
            )
        }
        AnimatedVisibility(
            visible = mode == TimerMode.Timer,
            enter = fadeIn(animationSpec = tween(180)) + scaleIn(
                animationSpec = tween(220),
                initialScale = 0.98f
            ),
            exit = fadeOut(animationSpec = tween(160)) + scaleOut(
                animationSpec = tween(160),
                targetScale = 0.98f
            )
        ) {
            TimerModeDetails(
                accent = accent,
                isRunning = timerRunning,
                totalMs = timerTotalMs,
                remainingMs = timerRemainingMs,
                onToggleRunning = onToggleTimer,
                onReset = onResetTimer,
                onSelectPreset = onSelectTimerPreset,
                customMinutesText = customMinutesText,
                customSecondsText = customSecondsText,
                onCustomMinutesChange = onCustomMinutesChange,
                onCustomSecondsChange = onCustomSecondsChange,
                onSetCustomTimer = onSetCustomTimer,
                customSetEnabled = customSetEnabled
            )
        }
    }
}

@Composable
private fun StopwatchModeDetails(
    accent: Color,
    isRunning: Boolean,
    elapsedMs: Long,
    lapEnabled: Boolean,
    intervalsEnabled: Boolean,
    lapEntries: List<LapEntry>,
    intervalLengthMs: Long,
    intervalRemainingLabel: String,
    onToggleRunning: () -> Unit,
    onReset: () -> Unit,
    onToggleLap: () -> Unit,
    onToggleIntervals: () -> Unit,
    onLap: () -> Unit,
    onSelectInterval: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val actionLabel = when {
        isRunning -> "Pause"
        elapsedMs > 0L -> "Resume"
        else -> "Start"
    }
    val actionIcon = if (isRunning) Icons.Rounded.Pause else Icons.Rounded.PlayArrow
    val intervalOptions = listOf(30_000L to "30s", 60_000L to "60s", 90_000L to "90s")
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Stopwatch",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Track intervals and log quick laps.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StopwatchTimerChip(
                label = "Lap mode",
                accent = accent,
                selected = lapEnabled,
                onClick = onToggleLap
            )
            StopwatchTimerChip(
                label = "Intervals",
                accent = accent,
                selected = intervalsEnabled,
                onClick = onToggleIntervals
            )
        }
        if (intervalsEnabled) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                intervalOptions.forEach { (intervalMs, label) ->
                    StopwatchTimerChip(
                        label = label,
                        accent = accent,
                        selected = intervalLengthMs == intervalMs,
                        onClick = { onSelectInterval(intervalMs) }
                    )
                }
            }
            Text(
                text = "Next interval in $intervalRemainingLabel",
                style = MaterialTheme.typography.labelSmall,
                color = accent
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            FilledTonalButton(
                onClick = onToggleRunning,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = accent.copy(alpha = 0.16f),
                    contentColor = accent
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = actionIcon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "$actionLabel Stopwatch",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (lapEnabled) {
                TextButton(
                    onClick = onLap,
                    enabled = isRunning,
                    colors = ButtonDefaults.textButtonColors(contentColor = accent)
                ) {
                    Text("Lap")
                }
            }
            if (elapsedMs > 0L) {
                TextButton(
                    onClick = onReset,
                    colors = ButtonDefaults.textButtonColors(contentColor = accent)
                ) {
                    Text("Reset")
                }
            }
        }
        if (lapEntries.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Recent laps",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                lapEntries.take(3).forEach { entry ->
                    StopwatchLapRow(entry = entry, accent = accent)
                }
            }
        }
    }
}

@Composable
private fun TimerModeDetails(
    accent: Color,
    isRunning: Boolean,
    totalMs: Long,
    remainingMs: Long,
    onToggleRunning: () -> Unit,
    onReset: () -> Unit,
    onSelectPreset: (Long) -> Unit,
    customMinutesText: String,
    customSecondsText: String,
    onCustomMinutesChange: (String) -> Unit,
    onCustomSecondsChange: (String) -> Unit,
    onSetCustomTimer: () -> Unit,
    customSetEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    val actionLabel = when {
        isRunning -> "Pause"
        remainingMs < totalMs -> "Resume"
        else -> "Start"
    }
    val actionIcon = if (isRunning) Icons.Rounded.Pause else Icons.Rounded.Timer
    val presets = listOf(3 * 60 * 1000L to "03:00", 5 * 60 * 1000L to "05:00")
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Timer",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Set a focused countdown for sets or recovery.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            presets.forEach { (presetMs, label) ->
                StopwatchTimerChip(
                    label = label,
                    accent = accent,
                    selected = totalMs == presetMs,
                    onClick = { onSelectPreset(presetMs) }
                )
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = customMinutesText,
                onValueChange = onCustomMinutesChange,
                label = { Text("Min") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.width(84.dp)
            )
            Text(
                text = ":",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedTextField(
                value = customSecondsText,
                onValueChange = onCustomSecondsChange,
                label = { Text("Sec") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.width(84.dp)
            )
            FilledTonalButton(
                onClick = onSetCustomTimer,
                enabled = customSetEnabled,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = accent.copy(alpha = 0.16f),
                    contentColor = accent
                ),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text("Set")
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            FilledTonalButton(
                onClick = onToggleRunning,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = accent.copy(alpha = 0.16f),
                    contentColor = accent
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = actionIcon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "$actionLabel Timer",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (remainingMs != totalMs) {
                TextButton(
                    onClick = onReset,
                    colors = ButtonDefaults.textButtonColors(contentColor = accent)
                ) {
                    Text("Reset")
                }
            }
        }
    }
}

@Composable
private fun StopwatchLapRow(entry: LapEntry, accent: Color, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = entry.label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (entry.isAuto) accent else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Total ${formatMinutesSeconds(entry.totalMs)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = formatMinutesSeconds(entry.lapMs),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun StopwatchTimerChip(
    label: String,
    accent: Color,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AssistChip(
        onClick = onClick,
        label = { Text(label) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = if (selected) accent else accent.copy(alpha = 0.14f),
            labelColor = if (selected) Color.White else accent
        ),
        modifier = modifier
    )
}

private fun formatMinutesSeconds(totalMs: Long): String {
    val totalSeconds = (totalMs / 1000L).coerceAtLeast(0L)
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    return "%02d:%02d".format(minutes, seconds)
}

@Composable
private fun WorkoutPlanCard(
    highlight: WorkoutPlanHighlight,
    onStartWorkout: () -> Unit,
    ctaLabel: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        BoxWithConstraints {
            val isCompact = maxWidth < compactWidthThreshold
            if (isCompact) {
                Column {
                    WorkoutImagePanel(highlight = highlight)
                    WorkoutDetailPanel(highlight = highlight, onStartWorkout = onStartWorkout, ctaLabel = ctaLabel)
                }
            } else {
                Row {
                    WorkoutImagePanel(
                        highlight = highlight,
                        modifier = Modifier.weight(0.42f)
                    )
                    WorkoutDetailPanel(
                        highlight = highlight,
                        onStartWorkout = onStartWorkout,
                        ctaLabel = ctaLabel,
                        modifier = Modifier.weight(0.58f)
                    )
                }
            }
        }
    }
}

@Composable
private fun WorkoutImagePanel(
    highlight: WorkoutPlanHighlight,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .heightIn(min = 160.dp)
            .background(
                Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.surfaceVariant,
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                            Color.Transparent
                        )
                    )
                )
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(999.dp))
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(999.dp))
        ) {
            Text(
                highlight.badge,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
        Icon(
            imageVector = Icons.Rounded.FitnessCenter,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(56.dp)
                .align(Alignment.Center)
        )
    }
}

@Composable
private fun WorkoutDetailPanel(
    highlight: WorkoutPlanHighlight,
    onStartWorkout: () -> Unit,
    ctaLabel: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    highlight.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    highlight.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.FitnessCenter,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        highlight.description?.takeIf { it.isNotBlank() }?.let { description ->
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        highlight.exercisePreview?.takeIf { it.isNotBlank() }?.let { exercisePreview ->
            Text(
                exercisePreview,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Rounded.Timer, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(highlight.exerciseCount, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.width(6.dp))
            Icon(imageVector = Icons.Rounded.LocalFireDepartment, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(highlight.setCount, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Button(
            onClick = onStartWorkout,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Icon(
                imageVector = Icons.Rounded.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(ctaLabel)
        }
    }
}

@Composable
private fun ProgressOverviewSection(days: List<ProgressDay>, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Progress Overview",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        "Last 7 Days",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Icon(
                        imageVector = Icons.Rounded.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
        ) {
            ProgressBarChart(days = days, modifier = Modifier.padding(16.dp))
        }
    }
}

@Composable
private fun ProgressBarChart(days: List<ProgressDay>, modifier: Modifier = Modifier) {
    val maxBarHeight = 120.dp
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        days.forEach { day ->
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (day.isActive) {
                    Text(
                        day.value,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Spacer(modifier = Modifier.height(14.dp))
                }
                Box(
                    modifier = Modifier
                        .height(maxBarHeight)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Box(
                        modifier = Modifier
                            .height(maxBarHeight * day.progress)
                            .width(22.dp)
                            .background(
                                if (day.isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                            )
                    )
                }
                Text(
                    day.label.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (day.isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun RecentActivitySection(
    activities: List<ActivityItem>,
    onSeeAll: () -> Unit,
    onSelectWorkout: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Recent Activity",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                "See All",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { onSeeAll() }
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            activities.forEach { activity ->
                RecentActivityRow(
                    activity = activity,
                    onClick = activity.workoutId?.let { { onSelectWorkout(it) } }
                )
            }
        }
    }
}

@Composable
private fun RecentActivityRow(
    activity: ActivityItem,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val cardModifier = if (onClick != null) {
        modifier
            .fillMaxWidth()
            .clickable { onClick() }
    } else {
        modifier.fillMaxWidth()
    }
    Card(
        modifier = cardModifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(activity.accent.copy(alpha = 0.15f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = activity.icon,
                    contentDescription = null,
                    tint = activity.accent
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    activity.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    activity.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    activity.primary,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    activity.secondary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun HomeFloatingActionButton(
    onCreateWorkoutPlan: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn(animationSpec = tween(180)) + scaleIn(animationSpec = tween(180)),
            exit = fadeOut(animationSpec = tween(160)) + scaleOut(animationSpec = tween(160))
        ) {
            androidx.compose.material3.ExtendedFloatingActionButton(
                onClick = {
                    expanded = false
                    onCreateWorkoutPlan()
                },
                icon = { Icon(Icons.Rounded.FitnessCenter, contentDescription = null) },
                text = { Text("Create workout plan") },
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(16.dp)
            )
        }
        androidx.compose.material3.FloatingActionButton(
            onClick = { expanded = !expanded },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = CircleShape
        ) {
            Icon(
                imageVector = if (expanded) Icons.Rounded.Close else Icons.Rounded.Add,
                contentDescription = "Toggle create menu",
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun BottomNavBar(
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
        tonalElevation = 2.dp,
        shadowElevation = 12.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            bottomNavItems.forEachIndexed { index, item ->
                val isSelected = index == selectedIndex
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .selectable(
                            selected = isSelected,
                            onClick = { onSelect(index) },
                            role = Role.Tab
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        item.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun CreateWorkoutScreen(
    onBack: () -> Unit,
    isSubmitting: Boolean,
    initialDate: LocalDate,
    onCreateWorkout: (date: String, notes: String?, timezone: String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedDate by rememberSaveable { mutableStateOf(initialDate) }
    var notes by rememberSaveable { mutableStateOf("") }
    var timezone by rememberSaveable { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    )
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                    Column {
                        Text("Create workout", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("Pick a date and add notes. You can attach exercises next.", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        OutlinedTextField(
                            value = selectedDate.toString(),
                            onValueChange = {},
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Date") },
                            readOnly = true,
                            trailingIcon = {
                                IconButton(onClick = { showDatePicker = true }) {
                                    Icon(Icons.Rounded.CalendarMonth, contentDescription = "Pick date")
                                }
                            }
                        )
                        OutlinedTextField(
                            value = timezone,
                            onValueChange = { timezone = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Timezone (optional)") },
                            placeholder = { Text("e.g. America/New_York") },
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Notes (optional)") },
                            minLines = 3
                        )
                        FilledTonalButton(
                            onClick = {
                                onCreateWorkout(selectedDate.toString(), notes.ifBlank { null }, timezone.ifBlank { null })
                            },
                            enabled = !isSubmitting
                        ) {
                            if (isSubmitting) {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .size(18.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(if (isSubmitting) "Creating..." else "Create workout")
                        }
                    }
                }
            }
        }

        if (showDatePicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = selectedDate.toEpochMillis()
            )
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            datePickerState.selectedDateMillis?.let { selectedDate = it.toLocalDate() }
                            showDatePicker = false
                        }
                    ) { Text("Apply") }
                },
                dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}

@Composable
private fun CreateWorkoutPlanScreen(
    onBack: () -> Unit,
    isSubmitting: Boolean,
    onCreateWorkoutPlan: (
        name: String,
        description: String?,
        exercises: List<String>,
        equipment: List<String>,
        muscleGroups: List<String>,
        numberOfExercises: Int?,
        sets: Int?,
        type: String?
    ) -> Unit,
    showSuccess: Boolean,
    onSuccessComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var name by rememberSaveable { mutableStateOf("") }
    var type by rememberSaveable { mutableStateOf("") }
    val muscleGroups = remember { mutableStateListOf<String>() }
    var description by rememberSaveable { mutableStateOf("") }
    var setsText by rememberSaveable { mutableStateOf("") }
    var nameError by rememberSaveable { mutableStateOf(false) }

    val accent = Color(0xFF00E676)
    val accentDark = Color(0xFF00C853)
    val background = Color(0xFF051A10)
    val glass = Color(0x73142319)
    val glassBorder = Color(0x14FFFFFF)
    val textDim = Color(0xFFA0B4AB)
    val typeOptions = listOf("Strength", "Cardio")
    val focusOptions = listOf("Upper Body", "Lower Body", "Chest", "Back", "Legs", "Biceps", "Triceps", "Shoulders", "Abs", "Cardio")
    val exercises = remember { mutableStateListOf<String>() }
    val equipment = remember { mutableStateListOf<String>() }
    var showExerciseInput by rememberSaveable { mutableStateOf(false) }
    var showEquipmentInput by rememberSaveable { mutableStateOf(false) }
    var newExerciseText by rememberSaveable { mutableStateOf("") }
    var newEquipmentText by rememberSaveable { mutableStateOf("") }
    val exerciseFocusRequester = remember { FocusRequester() }
    val equipmentFocusRequester = remember { FocusRequester() }
    val fieldTextStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.White)
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        focusedContainerColor = Color.White.copy(alpha = 0.05f),
        unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
        focusedBorderColor = accent.copy(alpha = 0.5f),
        unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
        cursorColor = accent,
        focusedPlaceholderColor = Color.White.copy(alpha = 0.2f),
        unfocusedPlaceholderColor = Color.White.copy(alpha = 0.2f),
        errorBorderColor = MaterialTheme.colorScheme.error,
        errorContainerColor = Color.White.copy(alpha = 0.05f)
    )

    var showSuccessOverlay by remember { mutableStateOf(false) }
    LaunchedEffect(showSuccess) {
        if (showSuccess) {
            showSuccessOverlay = true
            delay(1200)
            onSuccessComplete()
            showSuccessOverlay = false
        }
    }

    LaunchedEffect(showExerciseInput) {
        if (showExerciseInput) {
            exerciseFocusRequester.requestFocus()
        }
    }
    LaunchedEffect(showEquipmentInput) {
        if (showEquipmentInput) {
            equipmentFocusRequester.requestFocus()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(background)
    ) {
        val density = LocalDensity.current
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(accent.copy(alpha = 0.12f), Color.Transparent),
                        center = Offset.Zero,
                        radius = with(density) { 420.dp.toPx() }
                    )
                )
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(accent.copy(alpha = 0.08f), Color.Transparent),
                        center = Offset(
                            with(density) { 320.dp.toPx() },
                            with(density) { 720.dp.toPx() }
                        ),
                        radius = with(density) { 520.dp.toPx() }
                    )
                )
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 140.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            stickyHeader {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = background.copy(alpha = 0.8f),
                    shadowElevation = 12.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            IconButton(onClick = onBack) {
                                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White)
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(width = 32.dp, height = 6.dp)
                                        .clip(CircleShape)
                                        .background(accent)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(width = 10.dp, height = 6.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.2f))
                                )
                                Box(
                                    modifier = Modifier
                                        .size(width = 10.dp, height = 6.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.2f))
                                )
                            }
                            IconButton(onClick = {}) {
                                Icon(Icons.Rounded.MoreVert, contentDescription = "More options", tint = Color.White.copy(alpha = 0.7f))
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "New Plan",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Design your ultimate routine.",
                            style = MaterialTheme.typography.bodySmall,
                            color = textDim
                        )
                    }
                }
            }
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = WorkoutCardShape,
                    colors = CardDefaults.cardColors(containerColor = glass),
                    border = BorderStroke(1.dp, glassBorder)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.FitnessCenter,
                            contentDescription = null,
                            tint = accent,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(72.dp)
                                .alpha(0.12f)
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(accent, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Basics".uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 2.sp,
                                    color = accent
                                )
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "Workout Name".uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.5.sp,
                                    color = textDim
                                )
                                OutlinedTextField(
                                    value = name,
                                    onValueChange = {
                                        name = it
                                        if (nameError) nameError = it.isBlank()
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = { Text("e.g. Chest Day", color = Color.White.copy(alpha = 0.2f)) },
                                    singleLine = true,
                                    isError = nameError,
                                    shape = WorkoutFieldShape,
                                    textStyle = fieldTextStyle,
                                    colors = textFieldColors
                                )
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "Type".uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.5.sp,
                                    color = textDim
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(18.dp))
                                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(18.dp))
                                        .padding(4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    typeOptions.forEach { option ->
                                        val isSelected = type == option
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(if (isSelected) accent else Color.Transparent)
                                                .selectable(
                                                    selected = isSelected,
                                                    onClick = { type = option },
                                                    role = Role.RadioButton
                                                )
                                                .padding(vertical = 10.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = option,
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) background else Color.White.copy(alpha = 0.65f)
                                            )
                                        }
                                    }
                                }
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "Focus Area".uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.5.sp,
                                    color = textDim
                                )
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(focusOptions) { option ->
                                        val isSelected = muscleGroups.contains(option)
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(
                                                    if (isSelected) accent.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f)
                                                )
                                                .border(
                                                    1.dp,
                                                    if (isSelected) accent.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.12f),
                                                    RoundedCornerShape(16.dp)
                                                )
                                                .toggleable(
                                                    value = isSelected,
                                                    role = Role.Checkbox,
                                                    onValueChange = { checked ->
                                                        if (checked) {
                                                            muscleGroups.add(option)
                                                        } else {
                                                            muscleGroups.remove(option)
                                                        }
                                                    }
                                                )
                                                .padding(horizontal = 16.dp, vertical = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = option,
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                                color = if (isSelected) accent else Color.White.copy(alpha = 0.7f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(140.dp),
                        shape = WorkoutCardShape,
                        colors = CardDefaults.cardColors(containerColor = glass),
                        border = BorderStroke(1.dp, glassBorder)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(18.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Repeat,
                                contentDescription = null,
                                tint = accent.copy(alpha = 0.7f),
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(22.dp)
                            )
                            Column(
                                modifier = Modifier.align(Alignment.BottomStart),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Total Sets".uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.5.sp,
                                    color = textDim
                                )
                                BasicTextField(
                                    value = setsText,
                                    onValueChange = { value ->
                                        setsText = value.filter { it.isDigit() }
                                    },
                                    textStyle = MaterialTheme.typography.headlineMedium.copy(color = Color.White),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    decorationBox = { innerTextField ->
                                        if (setsText.isBlank()) {
                                            Text(
                                                text = "0",
                                                style = MaterialTheme.typography.headlineMedium,
                                                color = Color.White.copy(alpha = 0.2f)
                                            )
                                        }
                                        innerTextField()
                                    }
                                )
                            }
                        }
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(140.dp)
                            .clip(WorkoutCardShape)
                            .border(
                                BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                                WorkoutCardShape
                            )
                            .clickable { }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = "Add goal",
                                tint = Color.White.copy(alpha = 0.4f),
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Add Goal",
                                style = MaterialTheme.typography.labelMedium,
                                color = textDim
                            )
                        }
                    }
                }
            }
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = WorkoutCardShape,
                    colors = CardDefaults.cardColors(containerColor = glass),
                    border = BorderStroke(1.dp, glassBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(accent.copy(alpha = 0.7f), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Programming".uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp,
                                color = accent
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Exercises".uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.5.sp,
                                    color = textDim
                                )
                                if (!showExerciseInput) {
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(16.dp))
                                            .clickable { showExerciseInput = true }
                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(Color.White.copy(alpha = 0.08f))
                                                .border(1.dp, Color.White.copy(alpha = 0.12f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.Add,
                                                contentDescription = "Add exercise",
                                                tint = Color.White.copy(alpha = 0.7f),
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Add more",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = Color.White.copy(alpha = 0.7f)
                                        )
                                    }
                                } else {
                                    TextButton(onClick = {
                                        showExerciseInput = false
                                        newExerciseText = ""
                                    }) {
                                        Text("Done", color = accent)
                                    }
                                }
                            }
                            if (exercises.isEmpty()) {
                                Text(
                                    text = "No exercises yet. Tap Add more to include some.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.45f)
                                )
                            }
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(exercises) { exercise ->
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(accent.copy(alpha = 0.2f))
                                            .border(1.dp, accent.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                                            .padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = exercise,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = accent
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clip(CircleShape)
                                                .clickable {
                                                    exercises.remove(exercise)
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.Close,
                                                contentDescription = "Remove $exercise",
                                                tint = Color.White.copy(alpha = 0.7f),
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                }
                            }
                            if (showExerciseInput) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = newExerciseText,
                                        onValueChange = { newExerciseText = it },
                                        modifier = Modifier
                                            .weight(1f)
                                            .focusRequester(exerciseFocusRequester),
                                        placeholder = { Text("Add an exercise", color = Color.White.copy(alpha = 0.2f)) },
                                        singleLine = true,
                                        shape = WorkoutFieldShape,
                                        textStyle = fieldTextStyle,
                                        colors = textFieldColors
                                    )
                                    Button(
                                        onClick = {
                                            val trimmed = newExerciseText.trim()
                                            if (trimmed.isNotEmpty() && trimmed !in exercises) {
                                                exercises.add(trimmed)
                                            }
                                            newExerciseText = ""
                                        },
                                        enabled = newExerciseText.isNotBlank(),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = accent,
                                            contentColor = background
                                        ),
                                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                                    ) {
                                        Text("Add", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Equipment Needed".uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.5.sp,
                                    color = textDim
                                )
                                if (!showEquipmentInput) {
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(16.dp))
                                            .clickable { showEquipmentInput = true }
                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(Color.White.copy(alpha = 0.08f))
                                                .border(1.dp, Color.White.copy(alpha = 0.12f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.Add,
                                                contentDescription = "Add equipment",
                                                tint = Color.White.copy(alpha = 0.7f),
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Add more",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = Color.White.copy(alpha = 0.7f)
                                        )
                                    }
                                } else {
                                    TextButton(onClick = {
                                        showEquipmentInput = false
                                        newEquipmentText = ""
                                    }) {
                                        Text("Done", color = accent)
                                    }
                                }
                            }
                            if (equipment.isEmpty()) {
                                Text(
                                    text = "No equipment yet. Add items if needed.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.45f)
                                )
                            }
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(equipment) { item ->
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(accent.copy(alpha = 0.2f))
                                            .border(1.dp, accent.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                                            .padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = item,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = accent
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clip(CircleShape)
                                                .clickable { equipment.remove(item) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.Close,
                                                contentDescription = "Remove $item",
                                                tint = Color.White.copy(alpha = 0.7f),
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                }
                            }
                            if (showEquipmentInput) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = newEquipmentText,
                                        onValueChange = { newEquipmentText = it },
                                        modifier = Modifier
                                            .weight(1f)
                                            .focusRequester(equipmentFocusRequester),
                                        placeholder = { Text("Add equipment", color = Color.White.copy(alpha = 0.2f)) },
                                        singleLine = true,
                                        shape = WorkoutFieldShape,
                                        textStyle = fieldTextStyle,
                                        colors = textFieldColors
                                    )
                                    Button(
                                        onClick = {
                                            val trimmed = newEquipmentText.trim()
                                            if (trimmed.isNotEmpty() && trimmed !in equipment) {
                                                equipment.add(trimmed)
                                            }
                                            newEquipmentText = ""
                                        },
                                        enabled = newEquipmentText.isNotBlank(),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = accent,
                                            contentColor = background
                                        ),
                                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                                    ) {
                                        Text("Add", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = WorkoutCardShape,
                    colors = CardDefaults.cardColors(containerColor = glass),
                    border = BorderStroke(1.dp, glassBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(accent.copy(alpha = 0.7f), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Description".uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp,
                                color = accent
                            )
                        }
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("What is the goal of this workout?", color = Color.White.copy(alpha = 0.2f)) },
                            minLines = 3,
                            shape = WorkoutFieldShape,
                            textStyle = fieldTextStyle,
                            colors = textFieldColors
                        )
                    }
                }
            }
        }

        val buttonEnabled = !isSubmitting
        val buttonColors = if (buttonEnabled) {
            listOf(accent, accentDark)
        } else {
            listOf(accent.copy(alpha = 0.4f), accentDark.copy(alpha = 0.4f))
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, background.copy(alpha = 0.95f), background)
                    )
                )
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Brush.linearGradient(buttonColors))
                    .border(1.dp, accent.copy(alpha = 0.2f), RoundedCornerShape(28.dp))
                    .clickable(enabled = buttonEnabled) {
                        nameError = name.isBlank()
                        if (nameError) return@clickable
                        val exerciseList = exercises.toList()
                        val equipmentList = equipment.toList()
                        val numberOfExercises = exerciseList.size.takeIf { it > 0 }
                        val sets = setsText.toIntOrNull()
                        val selectedMuscleGroups = muscleGroups.map { it.trim() }.filter { it.isNotBlank() }
                        onCreateWorkoutPlan(
                            name,
                            description.ifBlank { null },
                            exerciseList,
                            equipmentList,
                            selectedMuscleGroups,
                            numberOfExercises,
                            sets,
                            type.ifBlank { null }
                        )
                        name = ""
                        type = ""
                        muscleGroups.clear()
                        description = ""
                        setsText = ""
                        exercises.clear()
                        equipment.clear()
                        showExerciseInput = false
                        showEquipmentInput = false
                        newExerciseText = ""
                        newEquipmentText = ""
                    },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = background,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            tint = background
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isSubmitting) "Creating..." else "Create Workout Plan",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = background
                    )
                }
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(top = 82.dp)
                    .size(width = 120.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.1f))
            )
        }

        AnimatedVisibility(
            visible = showSuccessOverlay,
            enter = fadeIn(animationSpec = tween(250)) + scaleIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(200))
        ) {
            WorkoutPlanSuccessOverlay(
                accent = accent,
                background = background,
                message = "Workout plan created"
            )
        }
    }
}

@Composable
private fun WorkoutPlanSuccessOverlay(
    accent: Color,
    background: Color,
    message: String,
    modifier: Modifier = Modifier
) {
    var animateIn by remember { mutableStateOf(false) }
    val sweepAngle by animateFloatAsState(
        targetValue = if (animateIn) 300f else 0f,
        animationSpec = tween(durationMillis = 720, easing = FastOutSlowInEasing),
        label = "successSweep"
    )
    val checkScale by animateFloatAsState(
        targetValue = if (animateIn) 1f else 0.7f,
        animationSpec = tween(durationMillis = 420, delayMillis = 160, easing = FastOutSlowInEasing),
        label = "successScale"
    )

    LaunchedEffect(Unit) {
        animateIn = true
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(background.copy(alpha = 0.86f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier.size(140.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawArc(
                        color = accent,
                        startAngle = -90f,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .scale(checkScale)
                        .background(accent.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
            Text(
                text = message,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Added to your library",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.6f)
            )
        }
    }
}

private class SetInputState(
    val id: String = UUID.randomUUID().toString(),
    reps: String = "",
    weight: String = "",
    rir: String = ""
) {
    var reps by mutableStateOf(reps)
    var weight by mutableStateOf(weight)
    var rir by mutableStateOf(rir)
    var repsError by mutableStateOf(false)

    fun hasAnyInput(): Boolean = reps.isNotBlank() || weight.isNotBlank() || rir.isNotBlank()
}

private class SetEditState(
    val setId: String? = null,
    reps: String = "",
    weight: String = "",
    rir: String = ""
) {
    var reps by mutableStateOf(reps)
    var weight by mutableStateOf(weight)
    var rir by mutableStateOf(rir)
    var repsError by mutableStateOf(false)

    fun hasAnyInput(): Boolean = reps.isNotBlank() || weight.isNotBlank() || rir.isNotBlank()
}

private val WorkoutForestBg = Color(0xFF0A140F)
private val WorkoutForestGlow = Color(0xFF153223)
private val WorkoutForestCard = Color(0xB20F1C16)
private val WorkoutVibrantGreen = Color(0xFF22C55E)
private val WorkoutVibrantGreenDark = Color(0xFF16A34A)
private val WorkoutTextHigh = Color(0xFFF0FDF4)
private val WorkoutTextDim = Color(0xFF86A694)
private val WorkoutCardShape = RoundedCornerShape(28.dp)
private val WorkoutFieldShape = RoundedCornerShape(20.dp)

@Composable
private fun WorkoutSetInputList(
    setInputs: SnapshotStateList<SetInputState>,
    isPrimary: Boolean,
    isActionRunning: Boolean,
    textFieldColors: androidx.compose.material3.TextFieldColors,
    onRemoveSet: (SetInputState) -> Unit,
    onAddSet: () -> Unit
) {
    setInputs.forEachIndexed { setIndex, entry ->
        androidx.compose.runtime.key(entry.id) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Set ${setIndex + 1}".uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        color = if (isPrimary) WorkoutVibrantGreen else WorkoutTextDim
                    )
                    if (setInputs.size > 1) {
                        IconButton(onClick = { onRemoveSet(entry) }) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Remove set",
                                tint = WorkoutTextDim
                            )
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.MoreVert,
                            contentDescription = null,
                            tint = WorkoutTextDim
                        )
                    }
                }
                val textStyle = MaterialTheme.typography.titleMedium.copy(
                    color = WorkoutTextHigh,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Weight".uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp,
                            color = WorkoutTextDim
                        )
                        OutlinedTextField(
                            value = entry.weight,
                            onValueChange = { entry.weight = it },
                            placeholder = { Text("0") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                            shape = WorkoutFieldShape,
                            colors = textFieldColors,
                            textStyle = textStyle
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Reps".uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp,
                            color = WorkoutTextDim
                        )
                        OutlinedTextField(
                            value = entry.reps,
                            onValueChange = {
                                entry.reps = it
                                entry.repsError = false
                            },
                            placeholder = { Text("0") },
                            singleLine = true,
                            isError = entry.repsError,
                            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                            shape = WorkoutFieldShape,
                            colors = textFieldColors,
                            textStyle = textStyle
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "RIR".uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp,
                            color = WorkoutTextDim
                        )
                        OutlinedTextField(
                            value = entry.rir,
                            onValueChange = { entry.rir = it },
                            placeholder = { Text("0") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                            shape = WorkoutFieldShape,
                            colors = textFieldColors,
                            textStyle = textStyle
                        )
                    }
                }
                if (setIndex < setInputs.lastIndex) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color.White.copy(alpha = 0.06f))
                    )
                }
            }
        }
    }
    val addSetColor = if (isPrimary) WorkoutVibrantGreen else WorkoutTextDim
    val addSetBg = if (isPrimary) WorkoutVibrantGreen.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.05f)
    val addSetBorder = if (isPrimary) WorkoutVibrantGreen.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.12f)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(addSetBg)
            .border(1.dp, addSetBorder, RoundedCornerShape(20.dp))
            .clickable(enabled = !isActionRunning) { onAddSet() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Rounded.Add, contentDescription = null, tint = addSetColor)
            Text(
                text = "Add Set",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = addSetColor
            )
        }
    }
}

@Composable
private fun StartWorkoutScreen(
    planId: String,
    state: FitnessUiState,
    onBack: () -> Unit,
    onStartWorkout: (String) -> Unit,
    onSaveWorkout: (String, List<WorkoutSetEntry>) -> Unit,
    modifier: Modifier = Modifier
) {
    val plan = remember(state.workoutPlans, planId) { state.workoutPlans.firstOrNull { it.id == planId } }
    val activeWorkoutId = state.activeWorkoutId
    val workout = remember(state.selectedWorkout, activeWorkoutId) {
        state.selectedWorkout?.takeIf { it.id == activeWorkoutId }
    }
    val sortedWorkoutItems = remember(workout?.items) {
        workout?.items?.sortedBy { it.order ?: 0 } ?: emptyList()
    }
    val setInputsByItem = remember(activeWorkoutId) { mutableStateMapOf<String, SnapshotStateList<SetInputState>>() }
    var localError by remember(activeWorkoutId) { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val vibrator = remember(context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    LaunchedEffect(planId, state.activeWorkoutPlanId, activeWorkoutId) {
        if (state.activeWorkoutPlanId != planId || activeWorkoutId == null) {
            onStartWorkout(planId)
        }
    }

    LaunchedEffect(workout?.items) {
        workout?.items?.forEach { item ->
            if (!setInputsByItem.containsKey(item.id)) {
                setInputsByItem[item.id] = mutableStateListOf(SetInputState())
            }
        }
    }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = WorkoutTextHigh,
        unfocusedTextColor = WorkoutTextHigh,
        focusedContainerColor = Color(0x66000000),
        unfocusedContainerColor = Color(0x66000000),
        focusedBorderColor = WorkoutVibrantGreen,
        unfocusedBorderColor = Color.White.copy(alpha = 0.08f),
        cursorColor = WorkoutVibrantGreen,
        focusedPlaceholderColor = WorkoutTextHigh.copy(alpha = 0.2f),
        unfocusedPlaceholderColor = WorkoutTextHigh.copy(alpha = 0.2f),
        errorBorderColor = MaterialTheme.colorScheme.error,
        errorContainerColor = Color(0x66000000)
    )
    val livePulse = rememberInfiniteTransition(label = "startWorkoutLivePulse")
    val livePulseAlpha by livePulse.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "startWorkoutLivePulseAlpha"
    )
    val triggerVibration = {
        vibrator?.let { device ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                device.vibrate(VibrationEffect.createWaveform(longArrayOf(0L, 120L, 80L, 120L, 80L, 120L), -1))
            } else {
                @Suppress("DEPRECATION")
                device.vibrate(longArrayOf(0L, 120L, 80L, 120L, 80L, 120L), -1)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(WorkoutForestBg)
    ) {
        val density = LocalDensity.current
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(WorkoutForestGlow, WorkoutForestBg),
                        center = Offset(with(density) { 200.dp.toPx() }, with(density) { (-40).dp.toPx() }),
                        radius = with(density) { 420.dp.toPx() }
                    )
                )
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(WorkoutVibrantGreen.copy(alpha = 0.12f), Color.Transparent),
                        center = Offset(with(density) { 380.dp.toPx() }, with(density) { 760.dp.toPx() }),
                        radius = with(density) { 520.dp.toPx() }
                    )
                )
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 140.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = WorkoutTextHigh)
                    }
                    Column {
                        Text(
                            text = "Active Workout",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = WorkoutTextHigh
                        )
                        Text(
                            text = plan?.name ?: "Workout Session",
                            style = MaterialTheme.typography.bodySmall,
                            color = WorkoutTextDim
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(WorkoutVibrantGreen.copy(alpha = livePulseAlpha), CircleShape)
                        )
                        Text(
                            text = "Live",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp,
                            color = WorkoutVibrantGreen
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(containerColor = WorkoutForestCard),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(
                                imageVector = Icons.Rounded.Info,
                                contentDescription = null,
                                tint = WorkoutVibrantGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Session Overview".uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp,
                                color = WorkoutVibrantGreen
                            )
                        }
                        Text(
                            text = plan?.description?.takeIf { it.isNotBlank() }
                                ?: "Maintain high intensity and track your RIR carefully.",
                            style = MaterialTheme.typography.bodySmall,
                            color = WorkoutTextDim
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AssistChip(
                                onClick = {},
                                enabled = false,
                                label = { Text("${workout?.items?.size ?: 0} exercises") },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = Color.White.copy(alpha = 0.06f),
                                    labelColor = WorkoutTextHigh
                                )
                            )
                            plan?.type?.takeIf { it.isNotBlank() }?.let { type ->
                                AssistChip(
                                    onClick = {},
                                    enabled = false,
                                    label = { Text(type) },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = Color.White.copy(alpha = 0.06f),
                                        labelColor = WorkoutTextHigh
                                    )
                                )
                            }
                        }
                    }
                }
            }

            if (workout == null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = WorkoutCardShape,
                        colors = CardDefaults.cardColors(containerColor = WorkoutForestCard),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = WorkoutVibrantGreen)
                            Text(
                                text = if (state.isActionRunning) "Setting up your session..." else "Loading workout details...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = WorkoutTextHigh
                            )
                            if (!state.isActionRunning) {
                                TextButton(onClick = { onStartWorkout(planId) }) {
                                    Text("Try again", color = WorkoutVibrantGreen)
                                }
                            }
                        }
                    }
                }
            } else {
                itemsIndexed(sortedWorkoutItems, key = { _, item -> item.id }) { index, item ->
                    val isPrimary = index == 0
                    val setInputs = setInputsByItem[item.id]
                    val defaultTimerMs = 3 * 60_000L
                    var timerRunning by rememberSaveable(item.id) { mutableStateOf(false) }
                    var timerRemainingMs by rememberSaveable(item.id) { mutableStateOf(0L) }
                    var timerDurationMs by rememberSaveable(item.id) { mutableStateOf(defaultTimerMs) }
                    var showTimerDialog by rememberSaveable(item.id) { mutableStateOf(false) }
                    val timerDisplayMs = if (timerRunning || timerRemainingMs > 0L) timerRemainingMs else timerDurationMs
                    val timerLabel = formatMinutesSeconds(timerDisplayMs)

                    LaunchedEffect(timerRunning) {
                        if (!timerRunning) return@LaunchedEffect
                        var lastTick = System.currentTimeMillis()
                        while (timerRunning && timerRemainingMs > 0L) {
                            delay(100L)
                            val now = System.currentTimeMillis()
                            val elapsed = now - lastTick
                            lastTick = now
                            timerRemainingMs = (timerRemainingMs - elapsed).coerceAtLeast(0L)
                        }
                        if (timerRunning && timerRemainingMs == 0L) {
                            timerRunning = false
                            triggerVibration()
                        }
                    }
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(30.dp),
                        colors = CardDefaults.cardColors(containerColor = WorkoutForestCard),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                    ) {
                        Box {
                            Box(
                                modifier = Modifier
                                    .width(6.dp)
                                    .fillMaxHeight()
                                    .background(if (isPrimary) WorkoutVibrantGreen else Color.White.copy(alpha = 0.12f))
                                    .align(Alignment.CenterStart)
                            )
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 22.dp, end = 18.dp, top = 18.dp, bottom = 18.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                val fullTitle = item.name ?: "Exercise"
                                var titleRevealToken by remember(item.id) { mutableStateOf(0) }
                                LaunchedEffect(titleRevealToken) {
                                    if (titleRevealToken == 0) return@LaunchedEffect
                                    delay(1400)
                                    titleRevealToken = 0
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(end = 12.dp)
                                    ) {
                                        Text(
                                            text = fullTitle,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = WorkoutTextHigh,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { titleRevealToken += 1 }
                                        )
                                        AnimatedVisibility(
                                            visible = titleRevealToken > 0,
                                            enter = fadeIn(animationSpec = tween(150)),
                                            exit = fadeOut(animationSpec = tween(150))
                                        ) {
                                            Text(
                                                text = fullTitle,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = WorkoutTextDim
                                            )
                                        }
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        val timerAccent = if (timerRunning) WorkoutVibrantGreen else WorkoutTextHigh.copy(alpha = 0.8f)
                                        val timerSurface = if (timerRunning) WorkoutVibrantGreen.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.06f)
                                        val timerBorder = if (timerRunning) WorkoutVibrantGreen.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.08f)
                                        Surface(
                                            shape = RoundedCornerShape(20.dp),
                                            color = timerSurface,
                                            border = BorderStroke(1.dp, timerBorder),
                                            modifier = Modifier.combinedClickable(
                                                enabled = !state.isActionRunning,
                                                onClick = {
                                                    if (timerRunning) {
                                                        timerRunning = false
                                                        timerRemainingMs = 0L
                                                    } else {
                                                        timerRemainingMs = timerDurationMs
                                                        timerRunning = true
                                                    }
                                                },
                                                onLongClick = { showTimerDialog = true }
                                            )
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.Timer,
                                                    contentDescription = null,
                                                    tint = timerAccent,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Text(
                                                    text = timerLabel,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = timerAccent
                                                )
                                            }
                                        }
                                        val setTotal = setInputs?.size ?: 0
                                        if (setTotal > 0) {
                                            Surface(
                                                shape = RoundedCornerShape(20.dp),
                                                color = WorkoutVibrantGreen.copy(alpha = 0.12f),
                                                border = BorderStroke(1.dp, WorkoutVibrantGreen.copy(alpha = 0.4f))
                                            ) {
                                                Text(
                                                    text = "Set 1/$setTotal",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = WorkoutVibrantGreen,
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                if (setInputs == null) {
                                    Text(
                                        text = "Preparing sets...",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = WorkoutTextDim
                                    )
                                } else {
                                    WorkoutSetInputList(
                                        setInputs = setInputs,
                                        isPrimary = isPrimary,
                                        isActionRunning = state.isActionRunning,
                                        textFieldColors = textFieldColors,
                                        onRemoveSet = { setInputs.remove(it) },
                                        onAddSet = { setInputs.add(SetInputState()) }
                                    )
                                }
                            }
                        }
                    }
                    if (showTimerDialog) {
                        var minutesText by rememberSaveable(item.id) {
                            mutableStateOf((timerDurationMs / 60_000L).toString().padStart(2, '0'))
                        }
                        var secondsText by rememberSaveable(item.id) {
                            mutableStateOf(((timerDurationMs / 1000L) % 60L).toString().padStart(2, '0'))
                        }
                        AlertDialog(
                            onDismissRequest = { showTimerDialog = false },
                            title = { Text("Set timer") },
                            text = {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        OutlinedTextField(
                                            value = minutesText,
                                            onValueChange = { minutesText = it.filter(Char::isDigit).take(2) },
                                            label = { Text("Minutes") },
                                            singleLine = true,
                                            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                                            modifier = Modifier.weight(1f)
                                        )
                                        OutlinedTextField(
                                            value = secondsText,
                                            onValueChange = { secondsText = it.filter(Char::isDigit).take(2) },
                                            label = { Text("Seconds") },
                                            singleLine = true,
                                            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    Text(
                                        text = "Long-press the timer chip anytime to update it.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        val minutes = minutesText.toLongOrNull()?.coerceAtLeast(0L) ?: 0L
                                        val seconds = secondsText.toLongOrNull()?.coerceIn(0L, 59L) ?: 0L
                                        timerDurationMs = (minutes * 60L + seconds) * 1000L
                                        timerRemainingMs = 0L
                                        timerRunning = false
                                        showTimerDialog = false
                                    }
                                ) {
                                    Text("Save")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showTimerDialog = false }) {
                                    Text("Cancel")
                                }
                            }
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(colors = listOf(Color.Transparent, WorkoutForestBg.copy(alpha = 0.95f), WorkoutForestBg))
                )
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                localError?.let { message ->
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                val buttonEnabled = workout != null && !state.isActionRunning
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(if (buttonEnabled) WorkoutVibrantGreen else WorkoutVibrantGreen.copy(alpha = 0.4f))
                        .border(1.dp, WorkoutVibrantGreen.copy(alpha = 0.25f), RoundedCornerShape(28.dp))
                        .clickable(enabled = buttonEnabled) {
                            val entries = mutableListOf<WorkoutSetEntry>()
                            var hasError = false
                            setInputsByItem.forEach { (itemId, inputs) ->
                                inputs.forEach { entry ->
                                    if (entry.hasAnyInput()) {
                                        val reps = entry.reps.trim().toIntOrNull()
                                        if (reps == null) {
                                            entry.repsError = true
                                            hasError = true
                                        } else {
                                            entry.repsError = false
                                            entries.add(
                                                WorkoutSetEntry(
                                                    itemId = itemId,
                                                    reps = reps,
                                                    weight = entry.weight.toDoubleOrNull(),
                                                    rir = entry.rir.toDoubleOrNull()
                                                )
                                            )
                                        }
                                    } else {
                                        entry.repsError = false
                                    }
                                }
                            }
                            if (hasError) {
                                localError = "Reps are required for each set."
                                return@clickable
                            }
                            if (entries.isEmpty()) {
                                localError = "Add at least one set to save your workout."
                                return@clickable
                            }
                            localError = null
                            onSaveWorkout(activeWorkoutId ?: return@clickable, entries)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (state.isActionRunning) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = WorkoutForestBg,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                tint = WorkoutForestBg
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (state.isActionRunning) "Saving..." else "Save Workout",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = WorkoutForestBg
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkoutHistoryScreen(
    state: FitnessUiState,
    onBack: () -> Unit,
    onSelectWorkout: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val workouts = remember(state.workouts) {
        state.workouts
            .filter { workout -> workout.items.sumOf { it.sets.size } > 0 }
            .sortedByDescending { it.date ?: "" }
    }
    val accent = Color(0xFF4DD0E1)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    )
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                    Column {
                        Text(
                            "Workout History",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "${workouts.size} sessions logged",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            if (workouts.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("No workouts yet", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "Start a workout to see your history here.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(workouts, key = { it.id }) { workout ->
                    WorkoutHistoryCard(
                        workout = workout,
                        accent = accent,
                        onClick = { onSelectWorkout(workout.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun WorkoutHistoryCard(
    workout: Workout,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val setCount = remember(workout.items) { workout.items.sumOf { it.sets.size } }
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(accent.copy(alpha = 0.18f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.CalendarMonth,
                    contentDescription = null,
                    tint = accent
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    workout.notes?.takeIf { it.isNotBlank() } ?: "Workout Session",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    workout.date ?: "Unknown date",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${workout.items.size} exercises",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "$setCount sets",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun WorkoutDetailScreen(
    workoutId: String,
    state: FitnessUiState,
    onBack: () -> Unit,
    onSaveEdits: (workoutId: String, updates: List<WorkoutSetUpdateEntry>, newSets: List<WorkoutSetEntry>) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val workout = remember(state.selectedWorkout, state.workouts, workoutId) {
        state.selectedWorkout?.takeIf { it.id == workoutId }
            ?: state.workouts.firstOrNull { it.id == workoutId }
    }
    val sortedWorkoutItems = remember(workout?.items) {
        workout?.items?.sortedBy { it.order ?: 0 } ?: emptyList()
    }
    val context = LocalContext.current
    val vibrator = remember(context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }
    val triggerVibration = {
        vibrator?.let { device ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                device.vibrate(VibrationEffect.createWaveform(longArrayOf(0L, 120L, 80L, 120L, 80L, 120L), -1))
            } else {
                @Suppress("DEPRECATION")
                device.vibrate(longArrayOf(0L, 120L, 80L, 120L, 80L, 120L), -1)
            }
        }
    }
    var isEditing by rememberSaveable(workoutId) { mutableStateOf(false) }
    val setEditsByItem = remember(workout?.id) { mutableStateMapOf<String, SnapshotStateList<SetEditState>>() }
    var localError by remember(workout?.id) { mutableStateOf<String?>(null) }

    LaunchedEffect(workoutId, state.selectedWorkout?.id) {
        if (state.selectedWorkout?.id != workoutId) {
            onRefresh()
        }
    }

    LaunchedEffect(workout?.items) {
        workout?.items?.forEach { item ->
            if (!setEditsByItem.containsKey(item.id)) {
                val list = mutableStateListOf<SetEditState>()
                if (item.sets.isNotEmpty()) {
                    item.sets.forEach { set ->
                        list.add(
                            SetEditState(
                                setId = set.id,
                                reps = set.reps?.toString() ?: "",
                                weight = set.weight?.toString() ?: "",
                                rir = set.rir?.toString() ?: ""
                            )
                        )
                    }
                } else {
                    list.add(SetEditState())
                }
                setEditsByItem[item.id] = list
            }
        }
    }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = WorkoutTextHigh,
        unfocusedTextColor = WorkoutTextHigh,
        focusedContainerColor = Color(0x66000000),
        unfocusedContainerColor = Color(0x66000000),
        focusedBorderColor = WorkoutVibrantGreen,
        unfocusedBorderColor = Color.White.copy(alpha = 0.08f),
        cursorColor = WorkoutVibrantGreen,
        focusedPlaceholderColor = WorkoutTextHigh.copy(alpha = 0.2f),
        unfocusedPlaceholderColor = WorkoutTextHigh.copy(alpha = 0.2f),
        errorBorderColor = MaterialTheme.colorScheme.error,
        errorContainerColor = Color(0x66000000)
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(WorkoutForestBg)
    ) {
        val density = LocalDensity.current
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(WorkoutForestGlow, WorkoutForestBg),
                        center = Offset(with(density) { 200.dp.toPx() }, with(density) { (-40).dp.toPx() }),
                        radius = with(density) { 420.dp.toPx() }
                    )
                )
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(WorkoutVibrantGreen.copy(alpha = 0.12f), Color.Transparent),
                        center = Offset(with(density) { 380.dp.toPx() }, with(density) { 760.dp.toPx() }),
                        radius = with(density) { 520.dp.toPx() }
                    )
                )
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = WorkoutTextHigh)
                    }
                    Column {
                        Text(
                            text = "Active Workout",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = WorkoutTextHigh
                        )
                        Text(
                            workout?.date ?: "Loading...",
                            style = MaterialTheme.typography.bodySmall,
                            color = WorkoutTextDim
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    val detailPulse = rememberInfiniteTransition(label = "workoutDetailLivePulse")
                    val detailPulseAlpha by detailPulse.animateFloat(
                        initialValue = 0.4f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(durationMillis = 900, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "workoutDetailLivePulseAlpha"
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(WorkoutVibrantGreen.copy(alpha = detailPulseAlpha), CircleShape)
                        )
                        Text(
                            text = "Live",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp,
                            color = WorkoutVibrantGreen
                        )
                        IconButton(onClick = { isEditing = !isEditing }) {
                            Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = "Toggle edit",
                                tint = if (isEditing) WorkoutVibrantGreen else WorkoutTextHigh
                            )
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = WorkoutCardShape,
                    colors = CardDefaults.cardColors(containerColor = WorkoutForestCard),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(
                                imageVector = Icons.Rounded.Info,
                                contentDescription = null,
                                tint = WorkoutVibrantGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Session Overview".uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp,
                                color = WorkoutVibrantGreen
                            )
                        }
                        Text(
                            text = workout?.notes?.takeIf { it.isNotBlank() }
                                ?: "Review your session details. Tap the pencil to edit.",
                            style = MaterialTheme.typography.bodySmall,
                            color = WorkoutTextDim
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AssistChip(
                                onClick = {},
                                enabled = false,
                                label = { Text("${workout?.items?.size ?: 0} exercises") },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = Color.White.copy(alpha = 0.06f),
                                    labelColor = WorkoutTextHigh
                                )
                            )
                            AssistChip(
                                onClick = {},
                                enabled = false,
                                label = { Text("Logged") },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = Color.White.copy(alpha = 0.06f),
                                    labelColor = WorkoutTextHigh
                                )
                            )
                        }
                    }
                }
            }

            if (workout == null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = WorkoutCardShape,
                        colors = CardDefaults.cardColors(containerColor = WorkoutForestCard),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (state.isActionRunning) {
                                CircularProgressIndicator(color = WorkoutVibrantGreen)
                                Text("Loading workout details...", color = WorkoutTextHigh)
                            } else {
                                Text("Could not load details.", color = WorkoutTextHigh)
                                TextButton(onClick = onRefresh) {
                                    Text("Try again", color = WorkoutVibrantGreen)
                                }
                            }
                        }
                    }
                }
            } else if (workout.items.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = WorkoutCardShape,
                        colors = CardDefaults.cardColors(containerColor = WorkoutForestCard),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("No exercises yet", style = MaterialTheme.typography.titleMedium, color = WorkoutTextHigh)
                            Text(
                                "Add an exercise above to start logging sets.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = WorkoutTextDim
                            )
                        }
                    }
                }
            } else {
                itemsIndexed(sortedWorkoutItems, key = { _, item -> item.id }) { index, item ->
                    val isPrimary = index == 0
                    val setEdits = setEditsByItem[item.id]
                    val defaultTimerMs = 3 * 60_000L
                    var timerRunning by rememberSaveable(item.id) { mutableStateOf(false) }
                    var timerRemainingMs by rememberSaveable(item.id) { mutableStateOf(0L) }
                    var timerDurationMs by rememberSaveable(item.id) { mutableStateOf(defaultTimerMs) }
                    var showTimerDialog by rememberSaveable(item.id) { mutableStateOf(false) }
                    val timerDisplayMs = if (timerRunning || timerRemainingMs > 0L) timerRemainingMs else timerDurationMs
                    val timerLabel = formatMinutesSeconds(timerDisplayMs)

                    LaunchedEffect(timerRunning) {
                        if (!timerRunning) return@LaunchedEffect
                        var lastTick = System.currentTimeMillis()
                        while (timerRunning && timerRemainingMs > 0L) {
                            delay(100L)
                            val now = System.currentTimeMillis()
                            val elapsed = now - lastTick
                            lastTick = now
                            timerRemainingMs = (timerRemainingMs - elapsed).coerceAtLeast(0L)
                        }
                        if (timerRunning && timerRemainingMs == 0L) {
                            timerRunning = false
                            triggerVibration()
                        }
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(30.dp),
                        colors = CardDefaults.cardColors(containerColor = WorkoutForestCard),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                    ) {
                        Box {
                            Box(
                                modifier = Modifier
                                    .width(6.dp)
                                    .fillMaxHeight()
                                    .background(if (isPrimary) WorkoutVibrantGreen else Color.White.copy(alpha = 0.12f))
                                    .align(Alignment.CenterStart)
                            )
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 22.dp, end = 18.dp, top = 18.dp, bottom = 18.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                val fullTitle = item.name ?: "Exercise"
                                var titleRevealToken by remember(item.id) { mutableStateOf(0) }
                                LaunchedEffect(titleRevealToken) {
                                    if (titleRevealToken == 0) return@LaunchedEffect
                                    delay(1400)
                                    titleRevealToken = 0
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(end = 12.dp)
                                    ) {
                                        Text(
                                            text = fullTitle,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = WorkoutTextHigh,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { titleRevealToken += 1 }
                                        )
                                        AnimatedVisibility(
                                            visible = titleRevealToken > 0,
                                            enter = fadeIn(animationSpec = tween(150)),
                                            exit = fadeOut(animationSpec = tween(150))
                                        ) {
                                            Text(
                                                text = fullTitle,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = WorkoutTextDim
                                            )
                                        }
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        val timerAccent = if (timerRunning) WorkoutVibrantGreen else WorkoutTextHigh.copy(alpha = 0.8f)
                                        val timerSurface = if (timerRunning) WorkoutVibrantGreen.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.06f)
                                        val timerBorder = if (timerRunning) WorkoutVibrantGreen.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.08f)
                                        Surface(
                                            shape = RoundedCornerShape(20.dp),
                                            color = timerSurface,
                                            border = BorderStroke(1.dp, timerBorder),
                                            modifier = Modifier.combinedClickable(
                                                enabled = !state.isActionRunning,
                                                onClick = {
                                                    if (timerRunning) {
                                                        timerRunning = false
                                                        timerRemainingMs = 0L
                                                    } else {
                                                        timerRemainingMs = timerDurationMs
                                                        timerRunning = true
                                                    }
                                                },
                                                onLongClick = { showTimerDialog = true }
                                            )
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.Timer,
                                                    contentDescription = null,
                                                    tint = timerAccent,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Text(
                                                    text = timerLabel,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = timerAccent
                                                )
                                            }
                                        }
                                        val setTotal = setEdits?.size ?: 0
                                        if (setTotal > 0) {
                                            Surface(
                                                shape = RoundedCornerShape(20.dp),
                                                color = WorkoutVibrantGreen.copy(alpha = 0.12f),
                                                border = BorderStroke(1.dp, WorkoutVibrantGreen.copy(alpha = 0.4f))
                                            ) {
                                                Text(
                                                    text = "Set 1/$setTotal",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = WorkoutVibrantGreen,
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                if (setEdits == null) {
                                    Text(
                                        text = "Preparing sets...",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = WorkoutTextDim
                                    )
                                } else {
                                    setEdits.forEachIndexed { setIndex, entry ->
                                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "Set ${setIndex + 1}".uppercase(),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Black,
                                                    letterSpacing = 2.sp,
                                                    color = if (isPrimary) WorkoutVibrantGreen else WorkoutTextDim
                                                )
                                                if (isEditing && setEdits.size > 1) {
                                                    IconButton(onClick = { setEdits.remove(entry) }) {
                                                        Icon(
                                                            imageVector = Icons.Rounded.Close,
                                                            contentDescription = "Remove set",
                                                            tint = WorkoutTextDim
                                                        )
                                                    }
                                                } else {
                                                    Icon(
                                                        imageVector = Icons.Rounded.MoreVert,
                                                        contentDescription = null,
                                                        tint = WorkoutTextDim
                                                    )
                                                }
                                            }
                                            val textStyle = MaterialTheme.typography.titleMedium.copy(
                                                color = WorkoutTextHigh,
                                                fontWeight = FontWeight.Bold,
                                                textAlign = TextAlign.Center
                                            )
                                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                                Column(
                                                    modifier = Modifier.weight(1f),
                                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Text(
                                                        text = "Weight".uppercase(),
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        letterSpacing = 1.5.sp,
                                                        color = WorkoutTextDim
                                                    )
                                                    OutlinedTextField(
                                                        value = entry.weight,
                                                        onValueChange = { entry.weight = it },
                                                        placeholder = { Text("0") },
                                                        singleLine = true,
                                                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                                                        shape = WorkoutFieldShape,
                                                        colors = textFieldColors,
                                                        textStyle = textStyle,
                                                        readOnly = !isEditing
                                                    )
                                                }
                                                Column(
                                                    modifier = Modifier.weight(1f),
                                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Text(
                                                        text = "Reps".uppercase(),
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        letterSpacing = 1.5.sp,
                                                        color = WorkoutTextDim
                                                    )
                                                    OutlinedTextField(
                                                        value = entry.reps,
                                                        onValueChange = {
                                                            entry.reps = it
                                                            entry.repsError = false
                                                        },
                                                        placeholder = { Text("0") },
                                                        singleLine = true,
                                                        isError = entry.repsError,
                                                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                                                        shape = WorkoutFieldShape,
                                                        colors = textFieldColors,
                                                        textStyle = textStyle,
                                                        readOnly = !isEditing
                                                    )
                                                }
                                                Column(
                                                    modifier = Modifier.weight(1f),
                                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Text(
                                                        text = "RIR".uppercase(),
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        letterSpacing = 1.5.sp,
                                                        color = WorkoutTextDim
                                                    )
                                                    OutlinedTextField(
                                                        value = entry.rir,
                                                        onValueChange = { entry.rir = it },
                                                        placeholder = { Text("0") },
                                                        singleLine = true,
                                                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                                                        shape = WorkoutFieldShape,
                                                        colors = textFieldColors,
                                                        textStyle = textStyle,
                                                        readOnly = !isEditing
                                                    )
                                                }
                                            }
                                            if (setIndex < setEdits.lastIndex) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(1.dp)
                                                        .background(Color.White.copy(alpha = 0.06f))
                                                )
                                            }
                                        }
                                    }
                                    val addSetColor = if (isPrimary) WorkoutVibrantGreen else WorkoutTextDim
                                    val addSetBg = if (isPrimary) WorkoutVibrantGreen.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.05f)
                                    val addSetBorder = if (isPrimary) WorkoutVibrantGreen.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.12f)
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(addSetBg)
                                            .border(1.dp, addSetBorder, RoundedCornerShape(20.dp))
                                            .clickable(enabled = isEditing && !state.isActionRunning) {
                                                setEdits.add(SetEditState())
                                            }
                                            .padding(vertical = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(Icons.Rounded.Add, contentDescription = null, tint = addSetColor)
                                            Text(
                                                text = "Add Set",
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = addSetColor
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (showTimerDialog) {
                        var minutesText by rememberSaveable(item.id) {
                            mutableStateOf((timerDurationMs / 60_000L).toString().padStart(2, '0'))
                        }
                        var secondsText by rememberSaveable(item.id) {
                            mutableStateOf(((timerDurationMs / 1000L) % 60L).toString().padStart(2, '0'))
                        }
                        AlertDialog(
                            onDismissRequest = { showTimerDialog = false },
                            title = { Text("Set timer") },
                            text = {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        OutlinedTextField(
                                            value = minutesText,
                                            onValueChange = { minutesText = it.filter(Char::isDigit).take(2) },
                                            label = { Text("Minutes") },
                                            singleLine = true,
                                            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                                            modifier = Modifier.weight(1f)
                                        )
                                        OutlinedTextField(
                                            value = secondsText,
                                            onValueChange = { secondsText = it.filter(Char::isDigit).take(2) },
                                            label = { Text("Seconds") },
                                            singleLine = true,
                                            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    Text(
                                        text = "Long-press the timer chip anytime to update it.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        val minutes = minutesText.toLongOrNull()?.coerceAtLeast(0L) ?: 0L
                                        val seconds = secondsText.toLongOrNull()?.coerceIn(0L, 59L) ?: 0L
                                        timerDurationMs = (minutes * 60L + seconds) * 1000L
                                        timerRemainingMs = 0L
                                        timerRunning = false
                                        showTimerDialog = false
                                    }
                                ) {
                                    Text("Save")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showTimerDialog = false }) {
                                    Text("Cancel")
                                }
                            }
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Brush.verticalGradient(colors = listOf(Color.Transparent, WorkoutForestBg.copy(alpha = 0.95f), WorkoutForestBg)))
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                localError?.let { message ->
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                val buttonEnabled = isEditing && !state.isActionRunning
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(if (buttonEnabled) WorkoutVibrantGreen else WorkoutVibrantGreen.copy(alpha = 0.4f))
                        .border(1.dp, WorkoutVibrantGreen.copy(alpha = 0.25f), RoundedCornerShape(28.dp))
                        .clickable(enabled = buttonEnabled) {
                            val updates = mutableListOf<WorkoutSetUpdateEntry>()
                            val newSets = mutableListOf<WorkoutSetEntry>()
                            var hasError = false
                            setEditsByItem.forEach { (itemId, entries) ->
                                entries.forEach { entry ->
                                    if (!entry.hasAnyInput()) {
                                        entry.repsError = false
                                        return@forEach
                                    }
                                    val reps = entry.reps.trim().toIntOrNull()
                                    if (reps == null) {
                                        entry.repsError = true
                                        hasError = true
                                    } else {
                                        entry.repsError = false
                                        if (entry.setId == null) {
                                            newSets.add(
                                                WorkoutSetEntry(
                                                    itemId = itemId,
                                                    reps = reps,
                                                    weight = entry.weight.toDoubleOrNull(),
                                                    rir = entry.rir.toDoubleOrNull()
                                                )
                                            )
                                        } else {
                                            updates.add(
                                                WorkoutSetUpdateEntry(
                                                    itemId = itemId,
                                                    setId = entry.setId,
                                                    reps = reps,
                                                    weight = entry.weight.toDoubleOrNull(),
                                                    rir = entry.rir.toDoubleOrNull()
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                            if (hasError) {
                                localError = "Reps are required for each set."
                                return@clickable
                            }
                            if (updates.isEmpty() && newSets.isEmpty()) {
                                localError = "No changes to save."
                                return@clickable
                            }
                            localError = null
                            onSaveEdits(workoutId, updates, newSets)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (state.isActionRunning) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = WorkoutForestBg,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                tint = WorkoutForestBg
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (state.isActionRunning) "Saving..." else "Save Workout",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = WorkoutForestBg
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AddWorkoutItemForm(
    workoutId: String,
    exercises: List<Exercise>,
    onAddItem: (workoutId: String, exerciseId: String, notes: String?, order: Int?) -> Unit,
    isActionRunning: Boolean
) {
    var selectedExerciseId by remember(workoutId, exercises) { mutableStateOf(exercises.firstOrNull()?.id) }
    var notes by remember(workoutId) { mutableStateOf("") }
    var orderText by remember(workoutId) { mutableStateOf("") }
    var expanded by remember(workoutId) { mutableStateOf(false) }
    var textFieldWidth by remember { mutableStateOf(0) }
    val density = LocalDensity.current

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Box {
            OutlinedTextField(
                value = exercises.firstOrNull { it.id == selectedExerciseId }?.name ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Exercise") },
                trailingIcon = {
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = "Toggle exercise dropdown")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned { textFieldWidth = it.size.width }
                    .clickable { expanded = !expanded }
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.width(with(density) { textFieldWidth.toDp() })
            ) {
                exercises.forEach { exercise ->
                    DropdownMenuItem(
                        text = { Text(exercise.name) },
                        onClick = {
                            selectedExerciseId = exercise.id
                            expanded = false
                        }
                    )
                }
            }
        }

        OutlinedTextField(
            value = orderText,
            onValueChange = { orderText = it },
            label = { Text("Order (optional)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
        )

        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Notes (optional)") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )

        FilledTonalButton(
            onClick = {
                selectedExerciseId?.let { exerciseId ->
                    onAddItem(workoutId, exerciseId, notes.ifBlank { null }, orderText.toIntOrNull())
                    notes = ""
                    orderText = ""
                }
            },
            enabled = selectedExerciseId != null && !isActionRunning
        ) {
            Text("Attach to workout")
        }
    }
}

@Composable
private fun WorkoutItemCard(
    workoutId: String,
    item: WorkoutItem,
    onAddSet: (workoutId: String, itemId: String, reps: Int, weight: Double?, rir: Double?, rpe: Double?, notes: String?, isPr: Boolean) -> Unit,
    onEditSet: (WorkoutSet) -> Unit,
    isActionRunning: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(item.name ?: "Exercise", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    item.notes?.takeIf { it.isNotBlank() }?.let {
                        Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Text(
                    "Sets: ${item.sets.size}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (item.sets.isEmpty()) {
                Text("No sets logged yet.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    item.sets.forEach { set ->
                        SetRow(set = set, onEdit = { onEditSet(set) })
                    }
                }
            }
            AddSetForm(
                workoutId = workoutId,
                itemId = item.id,
                onAddSet = onAddSet,
                isActionRunning = isActionRunning
            )
        }
    }
}

@Composable
private fun SetRow(set: WorkoutSet, onEdit: (() -> Unit)? = null) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            val repsText = set.reps?.toString() ?: "-"
            val weightText = set.weight?.let { "${it}" } ?: "-"
            Text("Reps: $repsText • Weight: $weightText", style = MaterialTheme.typography.bodyMedium)
            val effort = when {
                set.rpe != null -> "RPE ${set.rpe}"
                set.rir != null -> "RIR ${set.rir}"
                else -> null
            }
            effort?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            set.notes?.takeIf { it.isNotBlank() }?.let {
                Text(it, style = MaterialTheme.typography.bodySmall)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            if (set.isPR == true) {
                AssistChip(
                    onClick = {},
                    enabled = false,
                    label = { Text("PR") },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
            onEdit?.let {
                IconButton(onClick = it) {
                    Icon(Icons.Rounded.Edit, contentDescription = "Edit set")
                }
            }
        }
    }
}

private data class EditSetState(
    val workoutId: String,
    val itemId: String,
    val set: WorkoutSet
)

@Composable
private fun EditSetDialog(
    editState: EditSetState,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onSave: (reps: Int?, weight: Double?, rir: Double?, notes: String?, isPr: Boolean?) -> Unit
) {
    var repsText by remember(editState) { mutableStateOf(editState.set.reps?.toString() ?: "") }
    var weightText by remember(editState) { mutableStateOf(editState.set.weight?.toString() ?: "") }
    var rirText by remember(editState) { mutableStateOf(editState.set.rir?.toString() ?: "") }
    var notesText by remember(editState) { mutableStateOf(editState.set.notes ?: "") }
    var isPr by remember(editState) { mutableStateOf(editState.set.isPR == true) }
    var repsError by remember(editState) { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit set") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = repsText,
                    onValueChange = {
                        repsText = it
                        repsError = false
                    },
                    label = { Text("Reps") },
                    singleLine = true,
                    isError = repsError,
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = weightText,
                    onValueChange = { weightText = it },
                    label = { Text("Weight") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = rirText,
                    onValueChange = { rirText = it },
                    label = { Text("RIR") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("Notes") },
                    minLines = 2
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    androidx.compose.material3.Switch(checked = isPr, onCheckedChange = { isPr = it })
                    Text("PR")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val reps = repsText.toIntOrNull()
                    if (reps == null) {
                        repsError = true
                        return@Button
                    }
                    onSave(
                        reps,
                        weightText.toDoubleOrNull(),
                        rirText.toDoubleOrNull(),
                        notesText.ifBlank { null },
                        isPr
                    )
                },
                enabled = !isSubmitting
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun SetChip(set: WorkoutSet) {
    val label = buildString {
        append(set.reps ?: "-")
        append(" reps")
        set.weight?.let { append(" @ $it") }
    }
    AssistChip(
        onClick = {},
        enabled = false,
        leadingIcon = {
            Icon(
                Icons.Rounded.FitnessCenter,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
        },
        label = { Text(label) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            labelColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
    )
}

@Composable
private fun AddSetForm(
    workoutId: String,
    itemId: String,
    onAddSet: (workoutId: String, itemId: String, reps: Int, weight: Double?, rir: Double?, rpe: Double?, notes: String?, isPr: Boolean) -> Unit,
    isActionRunning: Boolean
) {
    var repsText by remember(itemId) { mutableStateOf("") }
    var weightText by remember(itemId) { mutableStateOf("") }
    var rirText by remember(itemId) { mutableStateOf("") }
    var rpeText by remember(itemId) { mutableStateOf("") }
    var notes by remember(itemId) { mutableStateOf("") }
    var isPr by remember(itemId) { mutableStateOf(false) }
    var repsError by remember(itemId) { mutableStateOf(false) }

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val isCompact = maxWidth < compactWidthThreshold
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Add set", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            if (isCompact) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = repsText,
                        onValueChange = {
                            repsText = it
                            repsError = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Reps") },
                        singleLine = true,
                        isError = repsError,
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = weightText,
                        onValueChange = { weightText = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Weight") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                    )
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = repsText,
                        onValueChange = {
                            repsText = it
                            repsError = false
                        },
                        modifier = Modifier.weight(1f),
                        label = { Text("Reps") },
                        singleLine = true,
                        isError = repsError,
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = weightText,
                        onValueChange = { weightText = it },
                        modifier = Modifier.weight(1f),
                        label = { Text("Weight") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                    )
                }
            }
            if (isCompact) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = rirText,
                        onValueChange = { rirText = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("RIR") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = rpeText,
                        onValueChange = { rpeText = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("RPE") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                    )
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = rirText,
                        onValueChange = { rirText = it },
                        modifier = Modifier.weight(1f),
                        label = { Text("RIR") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = rpeText,
                        onValueChange = { rpeText = it },
                        modifier = Modifier.weight(1f),
                        label = { Text("RPE") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                    )
                }
            }
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Notes (optional)") },
                minLines = 2
            )
            if (isCompact) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        androidx.compose.material3.Switch(checked = isPr, onCheckedChange = { isPr = it })
                        Text("PR")
                    }
                    FilledTonalButton(
                        onClick = {
                            val reps = repsText.toIntOrNull()
                            if (reps == null) {
                                repsError = true
                                return@FilledTonalButton
                            }
                            onAddSet(
                                workoutId,
                                itemId,
                                reps,
                                weightText.toDoubleOrNull(),
                                rirText.toDoubleOrNull(),
                                rpeText.toDoubleOrNull(),
                                notes.ifBlank { null },
                                isPr
                            )
                            repsText = ""
                            weightText = ""
                            rirText = ""
                            rpeText = ""
                            notes = ""
                            isPr = false
                        },
                        enabled = !isActionRunning,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save set")
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        androidx.compose.material3.Switch(checked = isPr, onCheckedChange = { isPr = it })
                        Text("PR")
                    }
                    FilledTonalButton(
                        onClick = {
                            val reps = repsText.toIntOrNull()
                            if (reps == null) {
                                repsError = true
                                return@FilledTonalButton
                            }
                            onAddSet(
                                workoutId,
                                itemId,
                                reps,
                                weightText.toDoubleOrNull(),
                                rirText.toDoubleOrNull(),
                                rpeText.toDoubleOrNull(),
                                notes.ifBlank { null },
                                isPr
                            )
                            repsText = ""
                            weightText = ""
                            rirText = ""
                            rpeText = ""
                            notes = ""
                            isPr = false
                        },
                        enabled = !isActionRunning
                    ) {
                        Text("Save set")
                    }
                }
            }
        }
    }
}

@Composable
private fun CreateExerciseScreen(
    onBack: () -> Unit,
    isSubmitting: Boolean,
    onCreateExercise: (name: String, notes: String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var name by rememberSaveable { mutableStateOf("") }
    var notes by rememberSaveable { mutableStateOf("") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    )
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                    Column {
                        Text("Create exercise", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("Save exercises once, reuse them everywhere.", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Exercise name") },
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Notes (optional)") },
                            minLines = 3
                        )
                        FilledTonalButton(
                            onClick = {
                                onCreateExercise(name, notes.ifBlank { null })
                                name = ""
                                notes = ""
                            },
                            enabled = !isSubmitting
                        ) {
                            Text(if (isSubmitting) "Saving..." else "Save exercise")
                        }
                    }
                }
            }
        }
    }
}

private fun LocalDate.toEpochMillis(): Long =
    this.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

private fun Long.toLocalDate(): LocalDate =
    Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()
