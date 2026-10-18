package com.example.studentmanagementapp.ui

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studentmanagementapp.data.local.StudentEntity
import com.example.studentmanagementapp.viewmodel.StudentViewModel
import com.example.studentmanagementapp.worker.SyncScheduler

@Composable
fun StudentScreen(
    viewModel: StudentViewModel,
    onLogout: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val allStudents by viewModel.students.collectAsState()

    val filteredStudents = remember(allStudents, viewModel.searchQuery) {
        if (viewModel.searchQuery.isBlank()) {
            allStudents
        } else {
            val query = viewModel.searchQuery.trim().lowercase()
            allStudents.filter {
                it.studentCode.lowercase().contains(query) ||
                it.name.lowercase().contains(query) ||
                it.major.lowercase().contains(query)
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openAddDialog() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Student"
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Student Management",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    NetworkStatusBadge(isOnline = viewModel.isOnline)
                    if (onLogout != null) {
                        TextButton(onClick = onLogout) {
                            Text(
                                text = "Logout",
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = viewModel.searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Search") },
                placeholder = { Text("Search student...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search"
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.syncStudents() },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Sync Now")
                }

                Button(
                    onClick = { viewModel.openAddDialog() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Student")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    viewModel.clearError()
                    SyncScheduler.syncNow(context)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Sync in Background")
            }

            Spacer(modifier = Modifier.height(12.dp))

            viewModel.errorMessage?.let { error ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (viewModel.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                if (filteredStudents.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (viewModel.searchQuery.isNotEmpty())
                                "No students match '${viewModel.searchQuery}'"
                            else
                                "No student data available.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(
                            items = filteredStudents,
                            key = { it.localId }
                        ) { student ->
                            StudentItemCard(
                                student = student,
                                onClick = { viewModel.selectStudent(student) },
                                onDelete = { viewModel.deleteStudent(student) }
                            )
                        }
                    }
                }
            }
        }
    }

    viewModel.selectedStudent?.let { student ->
        StudentDetailDialog(
            student = student,
            onDismiss = { viewModel.selectStudent(null) },
            onEdit = {
                viewModel.selectStudent(null)
                viewModel.openEditDialog(student)
            },
            onDelete = {
                viewModel.deleteStudent(student)
                viewModel.selectStudent(null)
            }
        )
    }

    if (viewModel.showAddEditDialog) {
        AddEditStudentDialog(
            studentToEdit = viewModel.studentToEdit,
            onDismiss = { viewModel.dismissAddEditDialog() },
            onSave = { code, name, major ->
                viewModel.saveStudent(code, name, major)
            }
        )
    }
}

@Composable
fun StudentItemCard(
    student: StudentEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Student Icon",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${student.studentCode} - ${student.name}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = student.major,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
                Text(
                    text = "Sync: ${student.syncStatus}",
                    color = if (student.syncStatus == "SYNCED") Color(0xFF2E7D32) else Color(0xFFE65100),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun NetworkStatusBadge(isOnline: Boolean) {
    Surface(
        color = if (isOnline) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isOnline) Color(0xFF4CAF50) else Color(0xFFE57373)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        color = if (isOnline) Color(0xFF4CAF50) else Color(0xFFE53935),
                        shape = CircleShape
                    )
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isOnline) "Online" else "Offline",
                color = if (isOnline) Color(0xFF2E7D32) else Color(0xFFC62828),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun StudentDetailDialog(
    student: StudentEntity,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Student Detail",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(text = "Local ID : ${student.localId}", fontSize = 15.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Server ID: ${student.serverId ?: "-"}", fontSize = 15.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Code     : ${student.studentCode}", fontSize = 15.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Name     : ${student.name}", fontSize = 15.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Major    : ${student.major}", fontSize = 15.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Sync     : ${student.syncStatus}", fontSize = 15.sp)
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onEdit,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit")
                }
                Button(
                    onClick = onDelete,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun AddEditStudentDialog(
    studentToEdit: StudentEntity?,
    onDismiss: () -> Unit,
    onSave: (code: String, name: String, major: String) -> Unit
) {
    var studentCode by remember { mutableStateOf(studentToEdit?.studentCode ?: "") }
    var name by remember { mutableStateOf(studentToEdit?.name ?: "") }
    var major by remember { mutableStateOf(studentToEdit?.major ?: "") }

    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (studentToEdit != null) "Edit Student" else "Add Student",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = studentCode,
                    onValueChange = {
                        studentCode = it
                        isError = false
                    },
                    label = { Text("Student Code") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        isError = false
                    },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = major,
                    onValueChange = {
                        major = it
                        isError = false
                    },
                    label = { Text("Major") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (isError) {
                    Text(
                        text = "กรุณากรอกข้อมูลให้ครบทุกช่อง",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            var isSubmitted by remember { mutableStateOf(false) }
            Button(
                enabled = !isSubmitted,
                onClick = {
                    if (studentCode.isNotBlank() && name.isNotBlank() && major.isNotBlank()) {
                        isSubmitted = true
                        onSave(studentCode.trim(), name.trim(), major.trim())
                    } else {
                        isError = true
                    }
                }
            ) {
                Text("SAVE")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
