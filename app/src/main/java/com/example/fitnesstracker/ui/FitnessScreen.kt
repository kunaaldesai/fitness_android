package com.example.fitnesstracker.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.material3.menuAnchor
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fitnesstracker.data.remote.Exercise
import com.example.fitnesstracker.data.remote.Workout
import com.example.fitnesstracker.data.remote.WorkoutItem
import com.example.fitnesstracker.data.remote.WorkoutSet
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun FitnessApp(viewModel: MainViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

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

    Scaffold(
        topBar = {
            FitnessTopBar(
                isBusy = state.isLoading || state.isActionRunning,
                onRefresh = viewModel::refreshEverything
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        FitnessScreen(
            state = state,
            onCreateWorkout = viewModel::createWorkout,
            onSelectWorkout = { viewModel.selectWorkout(it, force = true) },
            onAddItem = viewModel::addItemToWorkout,
            onAddSet = viewModel::addSet,
            onCreateExercise = viewModel::createExercise,
            modifier = Modifier.padding(padding)
        )
    }
}

@Composable
private fun FitnessTopBar(
    isBusy: Boolean,
    onRefresh: () -> Unit
) {
    TopAppBar(
        title = { Text("Fitness Tracker") },
        actions = {
            TextButton(onClick = onRefresh, enabled = !isBusy) {
                Text("Refresh")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FitnessScreen(
    state: FitnessUiState,
    onCreateWorkout: (date: String, notes: String?, timezone: String?) -> Unit,
    onSelectWorkout: (String) -> Unit,
    onAddItem: (workoutId: String, exerciseId: String, notes: String?, order: Int?) -> Unit,
    onAddSet: (workoutId: String, itemId: String, reps: Int, weight: Double?, rir: Double?, rpe: Double?, notes: String?, isPr: Boolean) -> Unit,
    onCreateExercise: (name: String, notes: String?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            UserSummaryCard(userName = state.user?.firstName ?: "kun", workoutsCount = state.workouts.size, isLoading = state.isLoading)
        }
        item {
            WorkoutCreatorCard(onCreateWorkout = onCreateWorkout, isSubmitting = state.isActionRunning)
        }
        item {
            ExerciseCreatorCard(onCreateExercise = onCreateExercise, isSubmitting = state.isActionRunning)
        }
        item {
            WorkoutsList(
                workouts = state.workouts,
                selectedWorkoutId = state.selectedWorkoutId,
                onSelectWorkout = onSelectWorkout,
                isLoading = state.isLoading
            )
        }
        state.selectedWorkout?.let { workout ->
            item {
                WorkoutDetailSection(
                    workout = workout,
                    exercises = state.exercises,
                    onAddItem = onAddItem,
                    onAddSet = onAddSet,
                    isActionRunning = state.isActionRunning
                )
            }
        }
    }
}

@Composable
private fun UserSummaryCard(
    userName: String,
    workoutsCount: Int,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Welcome back", style = MaterialTheme.typography.labelLarge)
                Text(
                    userName,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                )
                Text("Workouts in Firestore: $workoutsCount", style = MaterialTheme.typography.bodyMedium)
            }
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.width(32.dp), strokeWidth = 3.dp)
            }
        }
    }
}

@Composable
private fun WorkoutCreatorCard(
    onCreateWorkout: (date: String, notes: String?, timezone: String?) -> Unit,
    isSubmitting: Boolean
) {
    val formatter = DateTimeFormatter.ISO_DATE
    var dateText by remember { mutableStateOf(formatter.format(LocalDate.now())) }
    var notes by remember { mutableStateOf("") }
    var timezone by remember { mutableStateOf("") }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Log a workout", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = dateText,
                onValueChange = { dateText = it },
                label = { Text("Date (YYYY-MM-DD)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = timezone,
                onValueChange = { timezone = it },
                label = { Text("Timezone (optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )
            Button(
                onClick = { onCreateWorkout(dateText, notes.ifBlank { null }, timezone.ifBlank { null }) },
                enabled = !isSubmitting
            ) {
                Text("Create workout")
            }
        }
    }
}

@Composable
private fun ExerciseCreatorCard(
    onCreateExercise: (name: String, notes: String?) -> Unit,
    isSubmitting: Boolean
) {
    var name by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Save an exercise", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Exercise name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )
            Button(
                onClick = {
                    onCreateExercise(name, notes.ifBlank { null })
                    name = ""
                    notes = ""
                },
                enabled = !isSubmitting
            ) {
                Text("Save exercise")
            }
        }
    }
}

@Composable
private fun WorkoutsList(
    workouts: List<Workout>,
    selectedWorkoutId: String?,
    onSelectWorkout: (String) -> Unit,
    isLoading: Boolean
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Workouts", style = MaterialTheme.typography.titleMedium)
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.width(24.dp), strokeWidth = 3.dp)
                }
            }
            if (workouts.isEmpty()) {
                Text("No workouts yet. Create one to get started.")
            } else {
                workouts.forEach { workout ->
                    WorkoutListItem(
                        workout = workout,
                        isSelected = workout.id == selectedWorkoutId,
                        onSelect = { onSelectWorkout(workout.id) }
                    )
                    Divider()
                }
            }
        }
    }
}

@Composable
private fun WorkoutListItem(
    workout: Workout,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                workout.date ?: "No date",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
            )
            if (!workout.notes.isNullOrBlank()) {
                Text(
                    workout.notes ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        TextButton(onClick = onSelect) {
            Text(if (isSelected) "Open" else "View")
        }
    }
}

@Composable
private fun WorkoutDetailSection(
    workout: Workout,
    exercises: List<Exercise>,
    onAddItem: (workoutId: String, exerciseId: String, notes: String?, order: Int?) -> Unit,
    onAddSet: (workoutId: String, itemId: String, reps: Int, weight: Double?, rir: Double?, rpe: Double?, notes: String?, isPr: Boolean) -> Unit,
    isActionRunning: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Workout details", style = MaterialTheme.typography.titleMedium)
            Text(workout.date ?: "No date", style = MaterialTheme.typography.bodyMedium)
            if (!workout.notes.isNullOrBlank()) {
                Text(workout.notes ?: "", style = MaterialTheme.typography.bodySmall)
            }
            AddWorkoutItemForm(
                workoutId = workout.id,
                exercises = exercises,
                onAddItem = onAddItem,
                isActionRunning = isActionRunning
            )
            Spacer(modifier = Modifier.height(4.dp))
            if (workout.items.isEmpty()) {
                Text("No exercises logged in this workout yet.")
            } else {
                workout.items.sortedBy { it.order ?: 0 }.forEach { item ->
                    WorkoutItemCard(
                        workoutId = workout.id,
                        item = item,
                        onAddSet = onAddSet,
                        isActionRunning = isActionRunning
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Add exercise to workout", style = MaterialTheme.typography.titleSmall)
        if (exercises.isEmpty()) {
            Text("Save at least one exercise to attach it here.")
            return
        }
        var expanded by remember(workoutId, exercises) { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = exercises.firstOrNull { it.id == selectedExerciseId }?.name ?: "",
                onValueChange = {},
                label = { Text("Exercise") },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
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
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
        )
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Notes (optional)") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )
        Button(
            onClick = {
                selectedExerciseId?.let { exerciseId ->
                    onAddItem(workoutId, exerciseId, notes.ifBlank { null }, orderText.toIntOrNull())
                    notes = ""
                    orderText = ""
                }
            },
            enabled = selectedExerciseId != null && !isActionRunning
        ) {
            Text("Attach exercise")
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(item.name ?: "Exercise", style = MaterialTheme.typography.titleSmall)
            if (!item.notes.isNullOrBlank()) {
                Text(item.notes ?: "", style = MaterialTheme.typography.bodySmall)
            }
            if (item.sets.isEmpty()) {
                Text("No sets logged yet.")
            } else {
                item.sets.forEach { set ->
                    SetRow(set)
                    Divider()
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
            val weightText = set.weight?.toString() ?: "-"
            val repsText = set.reps?.toString() ?: "-"
            Text(
                "Reps: $repsText  Weight: $weightText",
                style = MaterialTheme.typography.bodyMedium
            )
            val rpeText = when {
                set.rpe != null -> "RPE ${set.rpe}"
                set.rir != null -> "RIR ${set.rir}"
                else -> ""
            }
            if (rpeText.isNotBlank()) {
                Text(rpeText, style = MaterialTheme.typography.bodySmall)
            }
            if (!set.notes.isNullOrBlank()) {
                Text(set.notes ?: "", style = MaterialTheme.typography.bodySmall)
            }
        }
        if (set.isPR == true) {
            Text("PR", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }
    }
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
        Text("Add set", style = MaterialTheme.typography.titleSmall)
        OutlinedTextField(
            value = repsText,
            onValueChange = {
                repsText = it
                repsError = false
            },
            label = { Text("Reps") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = repsError,
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
        )
        OutlinedTextField(
            value = weightText,
            onValueChange = { weightText = it },
            label = { Text("Weight (optional)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = rirText,
                onValueChange = { rirText = it },
                label = { Text("RIR") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
            )
            OutlinedTextField(
                value = rpeText,
                onValueChange = { rpeText = it },
                label = { Text("RPE") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
            )
        }
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Notes (optional)") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                androidx.compose.material3.Switch(checked = isPr, onCheckedChange = { isPr = it })
                Spacer(modifier = Modifier.width(8.dp))
                Text("PR")
            }
            Button(
                onClick = {
                    val reps = repsText.toIntOrNull()
                    if (reps == null) {
                        repsError = true
                        return@Button
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
