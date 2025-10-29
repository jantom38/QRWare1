// Ścieżka: app/src/main/java/com/qrware/app/ui/screens/UserManagement/ManagePermissionsScreen.kt
package com.qrware.app.ui.screens.UserManagement

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.qrware.app.data.model.PermissionRequest
import com.qrware.app.data.model.PermissionResponse
import com.qrware.app.ui.viewmodel.ManagePermissionsViewModel
import com.qrware.app.ui.viewmodel.PermissionDialogState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagePermissionsScreen(
    navController: NavController,
    viewModel: ManagePermissionsViewModel // Wstrzykiwany
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Zarządzaj Uprawnieniami") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróć")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::requestCreatePermission) {
                Icon(Icons.Default.Add, contentDescription = "Dodaj Uprawnienie")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when {
                uiState.isLoading && uiState.permissions.isEmpty() -> {
                    CircularProgressIndicator()
                }
                uiState.error != null && uiState.permissions.isEmpty() -> {
                    ErrorState(message = uiState.error!!)
                }
                uiState.permissions.isEmpty() -> {
                    Text("Brak uprawnień do wyświetlenia.")
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.permissions, key = { it.id }) { permission ->
                            PermissionListItem(
                                permission = permission,
                                onEditClick = { viewModel.requestEditPermission(permission) },
                                onDeleteClick = { viewModel.requestDeletePermission(permission) }
                            )
                        }
                    }
                }
            }
        }

        // --- Obsługa Okien Dialogowych ---
        val dialogState = uiState.showDialog
        if (dialogState != PermissionDialogState.None) {
            val permToEdit = (dialogState as? PermissionDialogState.Edit)?.permission

            if (dialogState is PermissionDialogState.Delete) {
                DeleteConfirmationDialog(
                    userName = "uprawnienie ${dialogState.permission.name}",
                    onConfirm = { viewModel.deletePermission(dialogState.permission) },
                    onDismiss = viewModel::dismissDialog
                )
            } else if (dialogState is PermissionDialogState.Create || permToEdit != null) {
                PermissionEditDialog(
                    permission = permToEdit,
                    isSaving = uiState.isLoading,
                    error = uiState.error,
                    onDismiss = viewModel::dismissDialog,
                    onSave = { permRequest ->
                        viewModel.savePermission(permRequest, permToEdit?.id)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PermissionListItem(
    permission: PermissionResponse,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (permission.active) Icons.Default.CheckCircle else Icons.Default.Error,
                contentDescription = if (permission.active) "Aktywne" else "Nieaktywne",
                tint = if (permission.active) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = permission.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = permission.description ?: "Brak opisu",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Zasób: ${permission.resource}, Akcja: ${permission.action}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            IconButton(onClick = onEditClick) {
                Icon(Icons.Default.Edit, contentDescription = "Edytuj")
            }
            IconButton(onClick = onDeleteClick) {
                Icon(Icons.Default.Delete, contentDescription = "Usuń", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PermissionEditDialog(
    permission: PermissionResponse?,
    isSaving: Boolean,
    error: String?,
    onDismiss: () -> Unit,
    onSave: (PermissionRequest) -> Unit
) {
    var name by remember(permission) { mutableStateOf(permission?.name ?: "") }
    var description by remember(permission) { mutableStateOf(permission?.description ?: "") }
    var resource by remember(permission) { mutableStateOf(permission?.resource ?: "") }
    var action by remember(permission) { mutableStateOf(permission?.action ?: "") }
    var active by remember(permission) { mutableStateOf(permission?.active ?: true) }

    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = if (permission == null) "Utwórz Uprawnienie" else "Edytuj Uprawnienie",
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nazwa (np. USER_READ)") }, modifier = Modifier.fillMaxWidth(), readOnly = isSaving)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Opis (opcjonalnie)") }, modifier = Modifier.fillMaxWidth(), readOnly = isSaving)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = resource, onValueChange = { resource = it }, label = { Text("Zasób (np. USER)") }, modifier = Modifier.fillMaxWidth(), readOnly = isSaving)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = action, onValueChange = { action = it }, label = { Text("Akcja (np. READ)") }, modifier = Modifier.fillMaxWidth(), readOnly = isSaving)
                Spacer(Modifier.height(8.dp))

                FormSwitchRow(
                    text = "Uprawnienie aktywne",
                    checked = active,
                    onCheckedChange = { active = it },
                    enabled = !isSaving
                )

                Spacer(Modifier.height(16.dp))
                if (error != null) {
                    Text(text = error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss, enabled = !isSaving) { Text("Anuluj") }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val request = PermissionRequest(
                                name = name.uppercase(),
                                description = description.ifBlank { null },
                                resource = resource.uppercase(),
                                action = action.uppercase(),
                                active = active
                            )
                            onSave(request)
                        },
                        enabled = !isSaving
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                        } else {
                            Text("Zapisz")
                        }
                    }
                }
            }
        }
    }
}

// --- Komponenty pomocnicze (współdzielone) ---
// (Można je wydzielić do wspólnego pliku, jeśli są używane w wielu miejscach)

@Composable
private fun FormSwitchRow(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text, style = MaterialTheme.typography.bodyLarge)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled
        )
    }
}

@Composable
private fun DeleteConfirmationDialog(
    userName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Potwierdź usunięcie") },
        text = { Text("Czy na pewno chcesz trwale usunąć $userName? Tej operacji nie można cofnąć.") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Usuń")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Anuluj")
            }
        }
    )
}

@Composable
private fun ErrorState(message: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = "Błąd",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(64.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error
        )
    }
}