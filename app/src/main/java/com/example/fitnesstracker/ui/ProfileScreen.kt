package com.example.fitnesstracker.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Straighten
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
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
import kotlinx.coroutines.delay

private val ProfileForestBg = Color(0xFF0A140F)
private val ProfileVibrantGreen = Color(0xFF22C55E)
private val ProfileCardColor = Color(0xB20F1C16)
private val ProfileTextHigh = Color(0xFFF0FDF4)
private val ProfileTextDim = Color(0xFF86A694)

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

    // Streak logic consistent with Home screen: use workouts size capped at 12 if no real logic exists,
    // or calculate based on dates. Let's use the simple logic for consistency with HomeScreen trace.
    val workoutsWithSets = remember(workouts) {
        workouts.filter { workout -> workout.items.sumOf { it.sets.size } > 0 }
    }
    val streakCount = remember(workoutsWithSets) {
        if (workoutsWithSets.isNotEmpty()) workoutsWithSets.size.coerceAtMost(12) else 12
    }

    var showEditDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ProfileForestBg)
    ) {
        val density = LocalDensity.current
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(ProfileVibrantGreen.copy(alpha = 0.12f), Color.Transparent),
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
            verticalArrangement = Arrangement.spacedBy(24.dp)
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
                StaggeredItem(delayMillis = 100) {
                    StatsRow(
                        totalWorkouts = totalWorkouts,
                        totalSets = totalSets,
                        streakCount = streakCount
                    )
                }
            }

            item {
                StaggeredItem(delayMillis = 200) {
                    SettingsSection()
                }
            }

            item {
                Spacer(modifier = Modifier.height(100.dp))
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
private fun StaggeredItem(
    delayMillis: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var visible by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!visible) {
            delay(delayMillis.toLong())
            visible = true
        }
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
private fun ProfileHeader(
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
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(
                                ProfileVibrantGreen.copy(alpha = 0.8f),
                                ProfileVibrantGreen.copy(alpha = 0.4f)
                            )
                        )
                    )
                    .border(2.dp, ProfileVibrantGreen.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                val initials = listOfNotNull(user?.firstName?.firstOrNull(), user?.lastName?.firstOrNull())
                    .joinToString("")
                    .uppercase()
                    .ifBlank { "U" }
                Text(
                    text = initials,
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = ProfileTextHigh
                )
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 6.dp, y = 6.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(ProfileVibrantGreen)
                    .clickable { onEditClick() }
                    .border(3.dp, ProfileForestBg, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Edit,
                    contentDescription = "Edit Profile",
                    tint = ProfileForestBg,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            val fullName = listOfNotNull(user?.firstName, user?.lastName).joinToString(" ").ifBlank { "Fitness User" }
            Text(
                text = fullName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = ProfileTextHigh
            )
            Text(
                text = user?.bio?.takeIf { it.isNotBlank() } ?: "Pushing limits one rep at a time.",
                style = MaterialTheme.typography.bodyMedium,
                color = ProfileVibrantGreen,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 32.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            value = totalWorkouts.toString(),
            label = "WORKOUTS",
            modifier = Modifier.weight(1f)
        )
        StatCard(
            value = streakCount.toString(),
            label = "DAY STREAK",
            modifier = Modifier.weight(1f),
            icon = Icons.Rounded.LocalFireDepartment,
            iconTint = Orange500
        )
        StatCard(
            value = totalSets.toString(),
            label = "TOTAL SETS",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCard(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    iconTint: Color? = null
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = ProfileCardColor),
        border = BorderStroke(1.dp, ProfileVibrantGreen.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (icon != null && iconTint != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (icon != null && iconTint != null) iconTint else ProfileVibrantGreen
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = ProfileTextDim,
                fontSize = 10.sp
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
            color = ProfileTextDim,
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
    iconTint: Color = ProfileVibrantGreen,
    textColor: Color = ProfileTextHigh,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ProfileCardColor.copy(alpha = 0.5f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 16.dp),
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
                    color = ProfileVibrantGreen
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = ProfileTextDim
            )
        }
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
        focusedTextColor = ProfileTextHigh,
        unfocusedTextColor = ProfileTextHigh,
        focusedContainerColor = Color(0x66000000),
        unfocusedContainerColor = Color(0x66000000),
        focusedBorderColor = ProfileVibrantGreen,
        unfocusedBorderColor = Color.White.copy(alpha = 0.08f),
        cursorColor = ProfileVibrantGreen,
        focusedLabelColor = ProfileVibrantGreen,
        unfocusedLabelColor = ProfileTextDim
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ProfileForestBg,
        title = { Text("Edit Profile", color = ProfileTextHigh) },
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
                    containerColor = ProfileVibrantGreen,
                    contentColor = ProfileForestBg
                )
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = ProfileForestBg,
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
                colors = ButtonDefaults.textButtonColors(contentColor = ProfileTextDim)
            ) {
                Text("Cancel")
            }
        }
    )
}
