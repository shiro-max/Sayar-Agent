package com.sayar.assistant.presentation.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.sayar.assistant.R
import com.sayar.assistant.presentation.viewmodel.AuthViewModel
import com.sayar.assistant.presentation.viewmodel.DriveSetupState
import com.sayar.assistant.presentation.viewmodel.DriveTestViewModel
import com.sayar.assistant.presentation.viewmodel.DriveTestState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToTimetable: () -> Unit,
    onNavigateToStudents: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToChat: () -> Unit,
    onSignOut: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
    driveTestViewModel: DriveTestViewModel = hiltViewModel()
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val driveSetupState by viewModel.driveSetupState.collectAsState()
    val driveTestState by driveTestViewModel.testState.collectAsState()

    var showTestDialog by remember { mutableStateOf(false) }

    // Drive Test Dialog
    if (showTestDialog) {
        DriveTestDialog(
            state = driveTestState,
            onRunTest = { driveTestViewModel.runConnectionTest() },
            onDismiss = {
                showTestDialog = false
                driveTestViewModel.resetState()
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        currentUser?.photoUrl?.let { photoUrl ->
                            AsyncImage(
                                model = photoUrl,
                                contentDescription = "Profile",
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                        }
                        Column {
                            Text(
                                text = currentUser?.displayName ?: stringResource(R.string.home),
                                style = MaterialTheme.typography.titleMedium
                            )
                            currentUser?.email?.let { email ->
                                Text(
                                    text = email,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.signOut()
                        onSignOut()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = stringResource(R.string.sign_out)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Drive Status Card
            DriveStatusCard(
                state = driveSetupState,
                onRetry = { viewModel.retryDriveSetup() },
                onTest = { showTestDialog = true }
            )

            HomeMenuCard(
                title = stringResource(R.string.chat_title),
                description = stringResource(R.string.chat_description),
                icon = Icons.Default.Chat,
                onClick = onNavigateToChat
            )

            HomeMenuCard(
                title = stringResource(R.string.timetable),
                description = "View and manage your class schedule",
                icon = Icons.Default.CalendarMonth,
                onClick = onNavigateToTimetable
            )

            HomeMenuCard(
                title = stringResource(R.string.students),
                description = "Manage student information",
                icon = Icons.Default.People,
                onClick = onNavigateToStudents
            )

            HomeMenuCard(
                title = stringResource(R.string.settings),
                description = "Configure AI and app settings",
                icon = Icons.Default.Settings,
                onClick = onNavigateToSettings
            )
        }
    }
}

@Composable
private fun DriveStatusCard(
    state: DriveSetupState,
    onRetry: () -> Unit,
    onTest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (icon, iconTint, statusText, backgroundColor) = when (state) {
        is DriveSetupState.Idle -> Quadruple(
            Icons.Default.Cloud,
            MaterialTheme.colorScheme.outline,
            "Cloud storage initializing...",
            MaterialTheme.colorScheme.surfaceVariant
        )
        is DriveSetupState.Loading -> Quadruple(
            Icons.Default.Cloud,
            MaterialTheme.colorScheme.primary,
            "Setting up cloud storage...",
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        )
        is DriveSetupState.Success -> Quadruple(
            Icons.Default.CheckCircle,
            MaterialTheme.colorScheme.tertiary,
            "Cloud storage ready",
            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
        )
        is DriveSetupState.Error -> Quadruple(
            Icons.Default.Error,
            MaterialTheme.colorScheme.error,
            "Cloud setup failed",
            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
        )
        is DriveSetupState.Disabled -> Quadruple(
            Icons.Default.CloudOff,
            MaterialTheme.colorScheme.outline,
            "Cloud storage disabled",
            MaterialTheme.colorScheme.surfaceVariant
        )
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (state is DriveSetupState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodyMedium
                )
                if (state is DriveSetupState.Success) {
                    Text(
                        text = "Folder: ${state.folders.userEmail}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (state is DriveSetupState.Error) {
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            if (state is DriveSetupState.Error) {
                TextButton(onClick = onRetry) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Retry",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Retry")
                }
            }

            if (state is DriveSetupState.Success) {
                TextButton(onClick = onTest) {
                    Text("Test")
                }
            }
        }
    }
}

@Composable
private fun DriveTestDialog(
    state: DriveTestState,
    onRunTest: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {
            if (state !is DriveTestState.Running) onDismiss()
        },
        title = {
            Text("Drive Connection Test")
        },
        text = {
            Column {
                when (state) {
                    is DriveTestState.Idle -> {
                        Text("This will test the connection to Google Drive by:")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("1. Checking folder access")
                        Text("2. Uploading a test file")
                        Text("3. Deleting the test file")
                    }
                    is DriveTestState.Running -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(state.step)
                        }
                    }
                    is DriveTestState.Success -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = state.message,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        state.details.forEach { detail ->
                            Text(
                                text = detail,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    is DriveTestState.Error -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = state.message,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            when (state) {
                is DriveTestState.Idle -> {
                    Button(onClick = onRunTest) {
                        Text("Run Test")
                    }
                }
                is DriveTestState.Running -> {
                    // No button while running
                }
                is DriveTestState.Success, is DriveTestState.Error -> {
                    Button(onClick = onDismiss) {
                        Text("Close")
                    }
                }
            }
        },
        dismissButton = {
            if (state is DriveTestState.Idle) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
            if (state is DriveTestState.Error) {
                OutlinedButton(onClick = onRunTest) {
                    Text("Retry")
                }
            }
        }
    )
}

@Composable
private fun HomeMenuCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon with soft lavender background circle
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(14.dp)
                        .size(28.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
