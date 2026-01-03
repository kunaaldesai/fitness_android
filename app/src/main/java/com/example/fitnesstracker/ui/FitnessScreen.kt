@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.fitnesstracker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fitnesstracker.data.remote.Exercise
import com.example.fitnesstracker.data.remote.Workout
import com.example.fitnesstracker.data.remote.WorkoutItem
import com.example.fitnesstracker.data.remote.WorkoutSet
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val headerDateFormatter = DateTimeFormatter.ofPattern("EEEE, MMM d")
private val shortDateFormatter = DateTimeFormatter.ofPattern("MMM d")

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
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when (val screen = destination) {
            FitnessDestination.Home -> HomeScreen(
                state = state,
                onOpenCreateWorkout = { destination = FitnessDestination.CreateWorkout },
                onOpenWorkout = { id ->
                    destination = FitnessDestination.WorkoutDetail(id)
                    viewModel.selectWorkout(id, force = true)
                },
                onOpenCreateExercise = { destination = FitnessDestination.CreateExercise },
                onRefresh = viewModel::refreshEverything,
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
    onOpenCreateExercise: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedDate by rememberSaveable { mutableStateOf(LocalDate.now()) }
    var showDatePicker by remember { mutableStateOf(false) }
    val workoutsForDay = remember(state.workouts, selectedDate) {
        state.workouts.filter { it.date == selectedDate.toString() }
    }

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
                HomeHeader(
                    userName = state.user?.firstName ?: "Athlete",
                    selectedDate = selectedDate,
                    onAddWorkout = onOpenCreateWorkout,
                    onPickDate = { showDatePicker = true }
                )
            }
            item {
                QuickActionsRow(
                    isLoading = state.isLoading,
                    onRefresh = onRefresh,
                    onCreateExercise = onOpenCreateExercise
                )
            }
            item {
                Text(
                    "Sessions for ${selectedDate.format(headerDateFormatter)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            if (workoutsForDay.isEmpty()) {
                item {
                    EmptyStateCard(onOpenCreateWorkout = onOpenCreateWorkout)
                }
            } else {
                items(workoutsForDay, key = { it.id }) { workout ->
                    WorkoutSummaryCard(
                        workout = workout,
                        onOpenWorkout = { onOpenWorkout(workout.id) }
                    )
                }
            }
            item { Spacer(modifier = Modifier.height(12.dp)) }
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
                            datePickerState.selectedDateMillis?.let {
                                selectedDate = it.toLocalDate()
                            }
                            showDatePicker = false
                        }
                    ) { Text("Apply") }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}

@Composable
private fun HomeHeader(
    userName: String,
    selectedDate: LocalDate,
    onAddWorkout: () -> Unit,
    onPickDate: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp)
    ) {
        Row(
            modifier = Modifier.align(Alignment.CenterStart),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            ) {
                IconButton(onClick = onAddWorkout, modifier = Modifier.size(44.dp)) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "Create workout",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Column {
                Text("Welcome back", style = MaterialTheme.typography.labelLarge)
                Text(
                    userName,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
        Row(
            modifier = Modifier.align(Alignment.Center),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val dayLabel = if (selectedDate == LocalDate.now()) {
                "Today"
            } else {
                selectedDate.format(shortDateFormatter)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    selectedDate.format(headerDateFormatter),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    dayLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                IconButton(onClick = onPickDate) {
                    Icon(Icons.Rounded.CalendarMonth, contentDescription = "Pick a date")
                }
            }
        }
    }
}

@Composable
private fun QuickActionsRow(
    isLoading: Boolean,
    onRefresh: () -> Unit,
    onCreateExercise: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(18.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Dial in your plan", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(
                    "Log sets fast or prep new exercises.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onRefresh, enabled = !isLoading) {
                    Icon(Icons.Rounded.Refresh, contentDescription = "Refresh data")
                }
                FilledTonalButton(onClick = onCreateExercise) {
                    Icon(Icons.Rounded.FitnessCenter, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("New exercise")
                }
            }
        }
        if (isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun EmptyStateCard(onOpenCreateWorkout: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("No workouts logged for this day.", style = MaterialTheme.typography.titleMedium)
            Text(
                "Tap the plus to start a fresh workout and add exercises like a FitNotes flow.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            FilledTonalButton(onClick = onOpenCreateWorkout) {
                Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create workout")
            }
        }
    }
}

@Composable
private fun WorkoutSummaryCard(
    workout: Workout,
    onOpenWorkout: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        workout.date ?: "Date pending",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                    if (!workout.notes.isNullOrBlank()) {
                        Text(
                            workout.notes ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                TextButton(onClick = onOpenWorkout) {
                    Text("View")
                }
            }
            if (workout.items.isEmpty()) {
                Text(
                    "No exercises yet. Tap view to start logging sets.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                workout.items.forEach { item ->
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            item.name ?: "Exercise",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        if (item.sets.isEmpty()) {
                            Text(
                                "Sets pending",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                item.sets.take(3).forEach { set ->
                                    SetChip(set = set)
                                }
                                if (item.sets.size > 3) {
                                    AssistChip(
                                        onClick = {},
                                        enabled = false,
                                        label = { Text("+${item.sets.size - 3}") },
                                        colors = AssistChipDefaults.assistChipColors(
                                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                                        )
                                    )
                                }
                            }
                        }
                    }
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
                        Icon(Icons.Filled.ArrowDropDown, contentDescription = "Toggle exercise dropdown")
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

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Add set", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
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
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Notes (optional)") },
            minLines = 2
        )
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
