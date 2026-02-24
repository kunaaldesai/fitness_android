package com.example.fitnesstracker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Straighten
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitnesstracker.data.remote.User
import com.example.fitnesstracker.ui.theme.Orange500

@Composable
fun ProfileScreen(
    state: FitnessUiState,
    onEditProfile: (String, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val user = state.user
    val workouts = state.workouts

    val totalWorkouts = workouts.size
    val totalSets = workouts.sumOf { it.items.sumOf { item -> item.sets.size } }
    // Calculate streak
    val streakCount = if (workouts.isNotEmpty()) workouts.size.coerceAtMost(12) else 0

    val stats = listOf(
        StatSummary(
            title = "Workouts",
            value = totalWorkouts.toString(),
            accent = WorkoutVibrantGreen,
            icon = Icons.Rounded.FitnessCenter,
            progress = 0.7f // Example progress
        ),
        StatSummary(
            title = "Total Sets",
            value = totalSets.toString(),
            accent = MaterialTheme.colorScheme.primary,
            icon = Icons.Rounded.BarChart,
            progress = 0.5f
        ),
        StatSummary(
            title = "Streak",
            value = "$streakCount Days",
            accent = Orange500,
            icon = Icons.Rounded.LocalFireDepartment,
            progress = 0.8f
        )
    )

    var showEditDialog by remember { mutableStateOf(false) }

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
                        colors = listOf(WorkoutVibrantGreen.copy(alpha = 0.12f), Color.Transparent),
                        center = androidx.compose.ui.geometry.Offset(
                            with(density) { 200.dp.toPx() },
                            with(density) { 300.dp.toPx() }
                        ),
                        radius = with(density) { 500.dp.toPx() }
                    )
                )
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                StaggeredItem(delayMillis = 0) {
                    ProfileHeaderSection(
                        user = user,
                        onEditClick = { showEditDialog = true }
                    )
                }
            }

            item {
                StaggeredItem(delayMillis = 100) {
                    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                        Text(
                            "Statistics",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = WorkoutTextHigh,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        StatsSummaryRow(stats = stats)
                    }
                }
            }

            item {
                StaggeredItem(delayMillis = 200) {
                    SettingsSection()
                }
            }
        }

        if (showEditDialog) {
            EditProfileDialog(
                user = user,
                isSubmitting = state.isActionRunning,
                onDismiss = { showEditDialog = false },
                onSubmit = { f, l, b ->
                    onEditProfile(f, l, b)
                    showEditDialog = false
                }
            )
        }
    }
}

@Composable
private fun ProfileHeaderSection(
    user: User?,
    onEditClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box {
            BigProfileAvatar(user = user, onEditClick = onEditClick)
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            val fullName = listOfNotNull(user?.firstName, user?.lastName).joinToString(" ").ifBlank { "Fitness User" }
            Text(
                text = fullName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = WorkoutTextHigh
            )
            Text(
                text = user?.bio?.takeIf { it.isNotBlank() } ?: "Pushing limits one rep at a time.",
                style = MaterialTheme.typography.bodyMedium,
                color = WorkoutVibrantGreen,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun BigProfileAvatar(
    user: User?,
    onEditClick: () -> Unit
) {
    val initials = listOfNotNull(user?.firstName?.firstOrNull(), user?.lastName?.firstOrNull())
        .joinToString("")
        .uppercase()
        .ifBlank { "U" }

    Box {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                        )
                    )
                )
                .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 4.dp, y = 4.dp)
                .size(40.dp)
                .clip(CircleShape)
                .background(WorkoutVibrantGreen)
                .clickable { onEditClick() }
                .border(3.dp, WorkoutForestBg, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Edit,
                contentDescription = "Edit Profile",
                tint = WorkoutForestBg,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun SettingsSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "ACCOUNT SETTINGS",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = WorkoutTextDim,
            letterSpacing = 1.5.sp
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SettingsItem(
                icon = Icons.Rounded.Person,
                label = "Edit Profile",
                onClick = {}
            )
            SettingsItem(
                icon = Icons.Rounded.Notifications,
                label = "Notifications",
                onClick = {}
            )
            SettingsItem(
                icon = Icons.Rounded.Straighten,
                label = "Unit System",
                trailingText = "Metric",
                onClick = {}
            )
            HorizontalDivider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 8.dp))
            SettingsItem(
                icon = Icons.AutoMirrored.Rounded.Logout,
                label = "Sign Out",
                iconTint = Color(0xFFEF4444),
                textColor = Color(0xFFEF4444),
                onClick = {}
            )
        }
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    label: String,
    trailingText: String? = null,
    iconTint: Color = WorkoutVibrantGreen,
    textColor: Color = WorkoutTextHigh,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(iconTint.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = textColor,
            modifier = Modifier.weight(1f)
        )
        if (trailingText != null) {
            Text(
                text = trailingText,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = WorkoutVibrantGreen
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = null,
            tint = WorkoutTextDim
        )
    }
}

@Composable
private fun EditProfileDialog(
    user: User?,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (String, String, String) -> Unit
) {
    var firstName by remember { mutableStateOf(user?.firstName ?: "") }
    var lastName by remember { mutableStateOf(user?.lastName ?: "") }
    var bio by remember { mutableStateOf(user?.bio ?: "") }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = WorkoutTextHigh,
        unfocusedTextColor = WorkoutTextHigh,
        focusedContainerColor = Color(0x66000000),
        unfocusedContainerColor = Color(0x66000000),
        focusedBorderColor = WorkoutVibrantGreen,
        unfocusedBorderColor = Color.White.copy(alpha = 0.08f),
        cursorColor = WorkoutVibrantGreen,
        focusedLabelColor = WorkoutVibrantGreen,
        unfocusedLabelColor = WorkoutTextDim
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = WorkoutForestCard,
        title = { Text("Edit Profile", color = WorkoutTextHigh) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("First Name") },
                    singleLine = true,
                    colors = textFieldColors,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Last Name") },
                    singleLine = true,
                    colors = textFieldColors,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Bio") },
                    minLines = 3,
                    colors = textFieldColors,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(firstName, lastName, bio) },
                enabled = !isSubmitting,
                colors = ButtonDefaults.buttonColors(
                    containerColor = WorkoutVibrantGreen,
                    contentColor = WorkoutForestBg
                )
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = WorkoutForestBg,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = WorkoutTextDim)
            ) {
                Text("Cancel")
            }
        }
    )
}
