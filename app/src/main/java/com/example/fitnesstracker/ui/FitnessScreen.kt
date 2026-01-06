@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.fitnesstracker.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.DirectionsRun
import androidx.compose.material.icons.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Pool
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fitnesstracker.data.remote.Exercise
import com.example.fitnesstracker.data.remote.WorkoutItem
import com.example.fitnesstracker.data.remote.WorkoutSet
import com.example.fitnesstracker.ui.theme.Blue500
import com.example.fitnesstracker.ui.theme.Orange500
import com.example.fitnesstracker.ui.theme.Purple500
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.delay

private val compactWidthThreshold = 520.dp

private data class StatSummary(
    val title: String,
    val value: String,
    val accent: Color,
    val icon: ImageVector,
    val progress: Float
)

private data class WorkoutHighlight(
    val title: String,
    val subtitle: String,
    val duration: String,
    val calories: String,
    val badge: String
)

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
    val accent: Color
)

private data class NavItem(
    val label: String,
    val icon: ImageVector
)

private val bottomNavItems = listOf(
    NavItem("Home", Icons.Rounded.Home),
    NavItem("Explore", Icons.Rounded.Explore),
    NavItem("Analytics", Icons.Rounded.BarChart),
    NavItem("Profile", Icons.Rounded.Person)
)

sealed interface FitnessDestination {
    data object Home : FitnessDestination
    data object CreateWorkout : FitnessDestination
    data class WorkoutDetail(val id: String) : FitnessDestination
    data object CreateExercise : FitnessDestination
}

@Composable
fun FitnessApp(viewModel: MainViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var destination by remember { mutableStateOf<FitnessDestination>(FitnessDestination.Home) }
    val isHome = destination == FitnessDestination.Home

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

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (isHome) {
                HomeFloatingActionButton(onClick = { destination = FitnessDestination.CreateWorkout })
            }
        },
        bottomBar = {
            if (isHome) {
                BottomNavBar()
            }
        }
    ) { padding ->
        when (val screen = destination) {
            FitnessDestination.Home -> HomeScreen(
                state = state,
                onOpenCreateWorkout = { destination = FitnessDestination.CreateWorkout },
                onOpenWorkout = { id ->
                    destination = FitnessDestination.WorkoutDetail(id)
                    viewModel.selectWorkout(id, force = true)
                },
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

            is FitnessDestination.WorkoutDetail -> WorkoutDetailScreen(
                workoutId = screen.id,
                state = state,
                onBack = { destination = FitnessDestination.Home },
                onAddItem = viewModel::addItemToWorkout,
                onAddSet = viewModel::addSet,
                onCreateExercise = { destination = FitnessDestination.CreateExercise },
                onRefresh = { viewModel.selectWorkout(screen.id, force = true) },
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
    onOpenWorkout: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val displayName = state.user?.firstName?.takeIf { it.isNotBlank() } ?: "Alex"
    val streakCount = remember(state.workouts) {
        if (state.workouts.isNotEmpty()) state.workouts.size.coerceAtMost(12) else 12
    }
    val highlightWorkout = remember(state.workouts) { state.workouts.firstOrNull() }
    val highlight = remember(highlightWorkout) {
        WorkoutHighlight(
            title = highlightWorkout?.notes?.takeIf { it.isNotBlank() } ?: "Upper Body Power",
            subtitle = highlightWorkout?.items?.takeIf { it.isNotEmpty() }
                ?.let { "Strength - ${it.size} exercises" }
                ?: "Strength - Intermediate",
            duration = "45 min",
            calories = "320 Kcal",
            badge = if (highlightWorkout != null) "Scheduled" else "Suggested"
        )
    }
    val stats = listOf(
        StatSummary("Kcal", "450", Orange500, Icons.Rounded.LocalFireDepartment, 0.75f),
        StatSummary("Time", "45m", Blue500, Icons.Rounded.Timer, 0.5f),
        StatSummary("Steps", "3.2k", MaterialTheme.colorScheme.primary, Icons.Rounded.DirectionsWalk, 0.4f)
    )
    val progressDays = listOf(
        ProgressDay("Mon", "20 min", 0.4f),
        ProgressDay("Tue", "45 min", 0.7f),
        ProgressDay("Wed", "15 min", 0.3f),
        ProgressDay("Thu", "60 min", 0.85f, isActive = true),
        ProgressDay("Fri", "30 min", 0.5f),
        ProgressDay("Sat", "10 min", 0.2f),
        ProgressDay("Sun", "Rest", 0.1f)
    )
    val activityItems = remember(state.workouts) {
        if (state.workouts.isNotEmpty()) {
            val icons = listOf(Icons.Rounded.DirectionsRun, Icons.Rounded.SelfImprovement, Icons.Rounded.Pool)
            val accents = listOf(Orange500, Purple500, Blue500)
            state.workouts.sortedByDescending { it.date ?: "" }
                .take(3)
                .mapIndexed { index, workout ->
                    ActivityItem(
                        title = workout.notes?.takeIf { it.isNotBlank() } ?: "Workout Session",
                        subtitle = workout.date?.let { "Logged on $it" } ?: "Recent activity",
                        primary = "${workout.items.size} exercises",
                        secondary = "${workout.items.sumOf { it.sets.size }} sets",
                        icon = icons[index % icons.size],
                        accent = accents[index % accents.size]
                    )
                }
        } else {
            listOf(
                ActivityItem(
                    title = "Morning Run",
                    subtitle = "Yesterday, 6:30 AM",
                    primary = "5.2 km",
                    secondary = "32 min",
                    icon = Icons.Rounded.DirectionsRun,
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
                if (isCompact) {
                    item {
                        StaggeredItem(delayMillis = 80) {
                            DailyStreakCard(streakCount = streakCount)
                        }
                    }
                }
                item {
                    StaggeredItem(delayMillis = 140) {
                        StatsSummaryRow(stats = stats)
                    }
                }
                item {
                    StaggeredItem(delayMillis = 200) {
                        TodayWorkoutSection(
                            highlight = highlight,
                            onStartWorkout = {
                                highlightWorkout?.let { onOpenWorkout(it.id) } ?: onOpenCreateWorkout()
                            },
                            ctaLabel = if (highlightWorkout != null) "Start Workout" else "Create Workout"
                        )
                    }
                }
                item {
                    StaggeredItem(delayMillis = 260) {
                        ProgressOverviewSection(days = progressDays)
                    }
                }
                item {
                    StaggeredItem(delayMillis = 320) {
                        RecentActivitySection(activities = activityItems)
                    }
                }
                if (state.isLoading) {
                    item {
                        StaggeredItem(delayMillis = 380) {
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StaggeredItem(
    delayMillis: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(delayMillis.toLong())
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(durationMillis = 500)) +
            slideInVertically(animationSpec = tween(durationMillis = 500)) { it / 6 }
    ) {
        Box(modifier = modifier) {
            content()
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
private fun ProfileAvatar(
    userName: String,
    modifier: Modifier = Modifier
) {
    val initials = userName
        .split(" ")
        .filter { it.isNotBlank() }
        .mapNotNull { it.firstOrNull()?.uppercase() }
        .take(2)
        .joinToString("")
        .ifBlank { "A" }

    Box(modifier = modifier.size(52.dp)) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .align(Alignment.Center)
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                        )
                    ),
                    shape = CircleShape
                )
                .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                initials,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold
            )
        }
        Box(
            modifier = Modifier
                .size(10.dp)
                .align(Alignment.BottomEnd)
                .background(MaterialTheme.colorScheme.primary, shape = CircleShape)
                .border(2.dp, MaterialTheme.colorScheme.background, CircleShape)
        )
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
private fun StatSummaryCard(summary: StatSummary, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(summary.accent.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = summary.icon,
                    contentDescription = null,
                    tint = summary.accent,
                    modifier = Modifier.size(18.dp)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    summary.value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    summary.title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(999.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(summary.progress)
                        .background(summary.accent, RoundedCornerShape(999.dp))
                )
            }
        }
    }
}

@Composable
private fun TodayWorkoutSection(
    highlight: WorkoutHighlight,
    onStartWorkout: () -> Unit,
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
        Card(
            modifier = Modifier.fillMaxWidth(),
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
}

@Composable
private fun WorkoutImagePanel(
    highlight: WorkoutHighlight,
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
    highlight: WorkoutHighlight,
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
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Rounded.Timer, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(highlight.duration, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.width(6.dp))
            Icon(imageVector = Icons.Rounded.LocalFireDepartment, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(highlight.calories, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
private fun RecentActivitySection(activities: List<ActivityItem>, modifier: Modifier = Modifier) {
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
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            activities.forEach { activity ->
                RecentActivityRow(activity = activity)
            }
        }
    }
}

@Composable
private fun RecentActivityRow(activity: ActivityItem, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
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
private fun HomeFloatingActionButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    androidx.compose.material3.FloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        shape = CircleShape
    ) {
        Icon(imageVector = Icons.Rounded.Add, contentDescription = "Log workout", modifier = Modifier.size(28.dp))
    }
}

@Composable
private fun BottomNavBar(modifier: Modifier = Modifier) {
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
                val isSelected = index == 0
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
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
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
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
private fun WorkoutDetailScreen(
    workoutId: String,
    state: FitnessUiState,
    onBack: () -> Unit,
    onAddItem: (workoutId: String, exerciseId: String, notes: String?, order: Int?) -> Unit,
    onAddSet: (workoutId: String, itemId: String, reps: Int, weight: Double?, rir: Double?, rpe: Double?, notes: String?, isPr: Boolean) -> Unit,
    onCreateExercise: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val workout = remember(state.selectedWorkout, state.workouts, workoutId) {
        state.selectedWorkout?.takeIf { it.id == workoutId }
            ?: state.workouts.firstOrNull { it.id == workoutId }
    }

    LaunchedEffect(workoutId) {
        onRefresh()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                    )
                )
            )
    ) {
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
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
                    }
                    Column {
                        Text("Workout", style = MaterialTheme.typography.labelLarge)
                        Text(
                            workout?.date ?: "Loading...",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    if (state.isActionRunning || state.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 3.dp)
                    }
                }
            }

            workout?.notes?.takeIf { it.isNotBlank() }?.let { notes ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("Notes", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            Text(notes, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Attach exercise", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        if (state.exercises.isEmpty()) {
                            Text(
                                "Save an exercise first.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            FilledTonalButton(onClick = onCreateExercise) {
                                Icon(Icons.Rounded.FitnessCenter, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Create exercise")
                            }
                        } else if (workout != null) {
                            AddWorkoutItemForm(
                                workoutId = workout.id,
                                exercises = state.exercises,
                                onAddItem = onAddItem,
                                isActionRunning = state.isActionRunning
                            )
                        }
                    }
                }
            }

            if (workout == null) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Text("Loading workout details...")
                        }
                    }
                }
            } else if (workout.items.isEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("No exercises yet", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "Add an exercise above to start logging sets.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(workout.items.sortedBy { it.order ?: 0 }, key = { it.id }) { item ->
                    WorkoutItemCard(
                        workoutId = workout.id,
                        item = item,
                        onAddSet = onAddSet,
                        isActionRunning = state.isActionRunning
                    )
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
                        SetRow(set)
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
private fun SetRow(set: WorkoutSet) {
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
    }
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
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
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
