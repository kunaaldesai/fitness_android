package com.example.fitnesstracker.ui

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Straighten
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fitnesstracker.data.remote.User
import com.example.fitnesstracker.ui.theme.Blue500
import com.example.fitnesstracker.ui.theme.Orange500
import com.example.fitnesstracker.ui.theme.Purple500

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

    val workoutsWithSets = remember(workouts) {
         workouts.filter { workout -> workout.items.sumOf { it.sets.size } > 0 }
    }
    val streakCount = remember(workoutsWithSets) {
        if (workoutsWithSets.isNotEmpty()) workoutsWithSets.size.coerceAtMost(12) else 0
    }

    var showEditDialog by remember { mutableStateOf(false) }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
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
                        ProfileHeader(
                            user = user,
                            onEditClick = { showEditDialog = true }
                        )
                    }
                }

                item {
                    StaggeredItem(delayMillis = 200) {
                        StatsRow(
                            totalWorkouts = totalWorkouts,
                            totalSets = totalSets,
                            streakCount = streakCount
                        )
                    }
                }

                item {
                     StaggeredItem(delayMillis = 260) {
                        SettingsSection()
                     }
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
private fun ProfileHeader(
    user: User?,
    onEditClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp, bottom = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box {
             val initials = listOfNotNull(user?.firstName?.firstOrNull(), user?.lastName?.firstOrNull())
                    .joinToString("")
                    .uppercase()
                    .ifBlank { "U" }

            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(
                        Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                            )
                        ),
                        shape = CircleShape
                    )
                    .border(4.dp, MaterialTheme.colorScheme.surface, CircleShape),
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
                    .size(36.dp)
                    .background(MaterialTheme.colorScheme.surface, CircleShape)
                    .padding(4.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .clickable { onEditClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Edit,
                    contentDescription = "Edit Profile",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
             val fullName = listOfNotNull(user?.firstName, user?.lastName).joinToString(" ").ifBlank { "Fitness User" }
            Text(
                text = fullName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
             Text(
                text = user?.bio?.takeIf { it.isNotBlank() } ?: "Pushing limits one rep at a time.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StatsRow(
    totalWorkouts: Int,
    totalSets: Int,
    streakCount: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatSummaryCard(
            summary = StatSummary(
                title = "WORKOUTS",
                value = totalWorkouts.toString(),
                accent = Orange500,
                icon = Icons.Rounded.FitnessCenter,
                progress = (totalWorkouts / 100f).coerceIn(0f, 1f)
            ),
            modifier = Modifier.weight(1f)
        )
         StatSummaryCard(
            summary = StatSummary(
                title = "STREAK",
                value = streakCount.toString(),
                accent = Purple500,
                icon = Icons.Rounded.LocalFireDepartment,
                progress = (streakCount / 30f).coerceIn(0f, 1f)
            ),
            modifier = Modifier.weight(1f)
        )
         StatSummaryCard(
            summary = StatSummary(
                title = "SETS",
                value = totalSets.toString(),
                accent = Blue500,
                icon = Icons.Rounded.FitnessCenter,
                progress = (totalSets / 500f).coerceIn(0f, 1f)
            ),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SettingsSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
             Text(
                text = "ACCOUNT SETTINGS",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
            )

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
             HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant)
             SettingsItem(
                icon = Icons.AutoMirrored.Rounded.Logout,
                label = "Sign Out",
                iconTint = MaterialTheme.colorScheme.error,
                textColor = MaterialTheme.colorScheme.error,
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
    iconTint: Color = MaterialTheme.colorScheme.primary,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
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
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = textColor,
            modifier = Modifier.weight(1f)
        )
        if (trailingText != null) {
            Text(
                text = trailingText,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Profile") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("First Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Last Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Bio") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(firstName, lastName, bio) },
                enabled = !isSubmitting
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                }
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
        }
    )
}
