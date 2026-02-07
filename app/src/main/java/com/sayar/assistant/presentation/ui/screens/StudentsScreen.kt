package com.sayar.assistant.presentation.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sayar.assistant.R
import com.sayar.assistant.domain.model.Student
import com.sayar.assistant.presentation.viewmodel.StudentsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentsScreen(
    onNavigateBack: () -> Unit,
    viewModel: StudentsViewModel = hiltViewModel()
) {
    val students by viewModel.students.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingStudent by remember { mutableStateOf<Student?>(null) }
    var deletingStudent by remember { mutableStateOf<Student?>(null) }

    if (showAddDialog || editingStudent != null) {
        StudentDialog(
            student = editingStudent,
            onDismiss = { showAddDialog = false; editingStudent = null },
            onSave = { student ->
                if (editingStudent != null) viewModel.updateStudent(student)
                else viewModel.addStudent(student)
                showAddDialog = false; editingStudent = null
            }
        )
    }

    deletingStudent?.let { student ->
        AlertDialog(
            onDismissRequest = { deletingStudent = null },
            title = { Text(stringResource(R.string.delete)) },
            text = { Text("Delete ${student.name}?") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteStudent(student); deletingStudent = null }) {
                    Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingStudent = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.students)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, "Add student")
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::updateSearchQuery,
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                placeholder = { Text(stringResource(R.string.students_search)) },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                singleLine = true
            )

            if (students.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.students_empty), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(students, key = { it.id }) { student ->
                        StudentCard(
                            student = student,
                            onEdit = { editingStudent = student },
                            onDelete = { deletingStudent = student }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StudentCard(student: Student, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(student.name, style = MaterialTheme.typography.titleMedium)
                Text("Roll: ${student.rollNumber} | ${student.grade}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                student.parentContact?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall)
                }
            }
            IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, "Edit") }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error) }
        }
    }
}

@Composable
private fun StudentDialog(student: Student?, onDismiss: () -> Unit, onSave: (Student) -> Unit) {
    var name by remember { mutableStateOf(student?.name ?: "") }
    var rollNumber by remember { mutableStateOf(student?.rollNumber ?: "") }
    var grade by remember { mutableStateOf(student?.grade ?: "") }
    var parentContact by remember { mutableStateOf(student?.parentContact ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (student == null) stringResource(R.string.students_add) else stringResource(R.string.edit)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = rollNumber, onValueChange = { rollNumber = it }, label = { Text("Roll Number") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = grade, onValueChange = { grade = it }, label = { Text("Grade") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = parentContact, onValueChange = { parentContact = it }, label = { Text("Parent Contact") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank() && rollNumber.isNotBlank() && grade.isNotBlank()) {
                        onSave(Student(
                            id = student?.id ?: java.util.UUID.randomUUID().toString(),
                            name = name, rollNumber = rollNumber, grade = grade,
                            parentContact = parentContact.ifBlank { null }
                        ))
                    }
                },
                enabled = name.isNotBlank() && rollNumber.isNotBlank() && grade.isNotBlank()
            ) { Text(stringResource(R.string.save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}
