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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToTimetable: () -> Unit,
    onNavigateToStudents: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onSignOut: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val driveSetupState by viewModel.driveSetupState.collectAsState()

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
                onRetry = { viewModel.retryDriveSetup() }
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
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
        is DriveSetupState.Success -> Quadruple(
            Icons.Default.CheckCircle,
            MaterialTheme.colorScheme.tertiary,
            "Cloud storage ready",
            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
        )
        is DriveSetupState.Error -> Quadruple(
            Icons.Default.Error,
            MaterialTheme.colorScheme.error,
            "Cloud setup failed",
            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
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
        shape = RoundedCornerShape(12.dp),
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
        }
    }
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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge
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
