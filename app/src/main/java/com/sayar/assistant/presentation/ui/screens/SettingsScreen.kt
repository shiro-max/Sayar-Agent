package com.sayar.assistant.presentation.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sayar.assistant.R
import com.sayar.assistant.data.repository.AiProvider
import com.sayar.assistant.data.repository.AppLanguage
import com.sayar.assistant.data.repository.ThemeMode
import com.sayar.assistant.presentation.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // AI Provider Section
            SettingsSection(
                title = stringResource(R.string.settings_ai_provider),
                icon = Icons.Default.SmartToy
            ) {
                AiProviderSelector(
                    selectedProvider = settings.aiProvider,
                    onProviderSelected = viewModel::updateAiProvider
                )

                Spacer(modifier = Modifier.height(12.dp))

                when (settings.aiProvider) {
                    AiProvider.GEMINI -> {
                        ApiKeyField(
                            label = stringResource(R.string.settings_gemini_api_key),
                            value = settings.geminiApiKey,
                            onValueChange = viewModel::updateGeminiApiKey,
                            placeholder = "AIza..."
                        )
                    }
                    AiProvider.OPENAI -> {
                        ApiKeyField(
                            label = stringResource(R.string.settings_openai_api_key),
                            value = settings.openAiApiKey,
                            onValueChange = viewModel::updateOpenAiApiKey,
                            placeholder = "sk-..."
                        )
                    }
                    AiProvider.OLLAMA -> {
                        OutlinedTextField(
                            value = settings.ollamaUrl,
                            onValueChange = viewModel::updateOllamaUrl,
                            label = { Text(stringResource(R.string.settings_ollama_url)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            placeholder = { Text("http://192.168.1.x:11434") }
                        )
                    }
                }
            }

            // Teacher Context Section
            SettingsSection(
                title = stringResource(R.string.settings_teacher_context),
                icon = Icons.Default.School
            ) {
                OutlinedTextField(
                    value = settings.teacherGrade,
                    onValueChange = viewModel::updateTeacherGrade,
                    label = { Text(stringResource(R.string.settings_teacher_grade)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("Grade 5") }
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = settings.teacherSubject,
                    onValueChange = viewModel::updateTeacherSubject,
                    label = { Text(stringResource(R.string.settings_teacher_subject)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("Mathematics") }
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.settings_teacher_context_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Appearance Section
            SettingsSection(
                title = stringResource(R.string.settings_appearance),
                icon = Icons.Default.Palette
            ) {
                ThemeModeSelector(
                    selectedMode = settings.themeMode,
                    onModeSelected = viewModel::updateThemeMode
                )
            }

            // Language Section
            SettingsSection(
                title = stringResource(R.string.settings_language),
                icon = Icons.Default.Language
            ) {
                LanguageSelector(
                    selectedLanguage = settings.language,
                    onLanguageSelected = viewModel::updateLanguage
                )
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            content()
        }
    }
}

@Composable
private fun AiProviderSelector(
    selectedProvider: AiProvider,
    onProviderSelected: (AiProvider) -> Unit
) {
    Column {
        AiProvider.entries.forEach { provider ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onProviderSelected(provider) }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = provider == selectedProvider,
                    onClick = { onProviderSelected(provider) }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = when (provider) {
                            AiProvider.GEMINI -> "Google Gemini"
                            AiProvider.OPENAI -> "OpenAI"
                            AiProvider.OLLAMA -> "Ollama (Local)"
                        },
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = when (provider) {
                            AiProvider.GEMINI -> stringResource(R.string.settings_gemini_desc)
                            AiProvider.OPENAI -> stringResource(R.string.settings_openai_desc)
                            AiProvider.OLLAMA -> stringResource(R.string.settings_ollama_desc)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ApiKeyField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    var passwordVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        placeholder = { Text(placeholder) },
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Key,
                contentDescription = null
            )
        },
        trailingIcon = {
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(
                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = if (passwordVisible) "Hide API key" else "Show API key"
                )
            }
        }
    )
}

@Composable
private fun ThemeModeSelector(
    selectedMode: ThemeMode,
    onModeSelected: (ThemeMode) -> Unit
) {
    Column {
        ThemeMode.entries.forEach { mode ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onModeSelected(mode) }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = mode == selectedMode,
                    onClick = { onModeSelected(mode) }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when (mode) {
                        ThemeMode.SYSTEM -> stringResource(R.string.settings_theme_system)
                        ThemeMode.LIGHT -> stringResource(R.string.settings_theme_light)
                        ThemeMode.DARK -> stringResource(R.string.settings_theme_dark)
                    },
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
private fun LanguageSelector(
    selectedLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit
) {
    Column {
        AppLanguage.entries.forEach { language ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onLanguageSelected(language) }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = language == selectedLanguage,
                    onClick = { onLanguageSelected(language) }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when (language) {
                        AppLanguage.ENGLISH -> "English"
                        AppLanguage.MYANMAR -> "မြန်မာ (Myanmar)"
                    },
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}
