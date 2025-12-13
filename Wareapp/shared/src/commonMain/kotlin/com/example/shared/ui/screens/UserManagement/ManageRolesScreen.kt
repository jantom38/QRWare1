package com.example.shared.ui.screens.UserManagement

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Search
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
import com.example.shared.data.model.PermissionResponse
import com.example.shared.data.model.RoleRequest
import com.example.shared.data.model.RoleResponse
import com.example.shared.ui.viewmodel.UserManagament.DialogState
import com.example.shared.ui.viewmodel.UserManagament.ManageRolesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageRolesScreen(
    navController: NavController,
    viewModel: ManageRolesViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Zarządzaj Rolami") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróć")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::requestCreateRole) {
                Icon(Icons.Default.Add, contentDescription = "Dodaj Rolę")
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
                uiState.isLoading && uiState.roles.isEmpty() -> {
                    CircularProgressIndicator()
                }
                uiState.error != null && uiState.roles.isEmpty() -> {
                    ErrorState(message = uiState.error!!)
                }
                uiState.roles.isEmpty() -> {
                    Text("Brak ról do wyświetlenia.")
                }
                else -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        OutlinedTextField(
                            value = uiState.searchQuery,
                            onValueChange = { viewModel.searchRoles(it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            placeholder = { Text("Szukaj ról...") },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = "Szukaj")
                            },
                            singleLine = true
                        )

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(uiState.roles, key = { it.id }) { role ->
                                RoleListItem(
                                    role = role,
                                    onEditClick = { viewModel.requestEditRole(role) },
                                    onDeleteClick = { viewModel.requestDeleteRole(role) }
                                )
                            }
                        }
                    }
                }
            }
        }

        val dialogState = uiState.showDialog
        if (dialogState != DialogState.None) {
            val roleToEdit = (dialogState as? DialogState.Edit)?.role

            if (dialogState is DialogState.Delete) {
                DeleteConfirmationDialog(
                    userName = "rolę ${dialogState.role.name}",
                    onConfirm = { viewModel.deleteRole(dialogState.role) },
                    onDismiss = viewModel::dismissDialog
                )
            } else if (dialogState is DialogState.Create || roleToEdit != null) {
                RoleEditDialog(
                    role = roleToEdit,
                    allPermissions = uiState.allPermissions,
                    isSaving = uiState.isLoading,
                    error = uiState.error,
                    onDismiss = viewModel::dismissDialog,
                    onSave = { roleRequest ->
                        viewModel.saveRole(roleRequest, roleToEdit?.id)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoleListItem(
    role: RoleResponse,
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
                imageVector = if (role.active) Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = if (role.active) "Aktywna" else "Nieaktywna",
                tint = if (role.active) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = role.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = role.description ?: "Brak opisu",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Uprawnienia: ${role.permissions.size}",
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
private fun RoleEditDialog(
    role: RoleResponse?,
    allPermissions: List<PermissionResponse>,
    isSaving: Boolean,
    error: String?,
    onDismiss: () -> Unit,
    onSave: (RoleRequest) -> Unit
) {
    var name by remember(role) { mutableStateOf(role?.name ?: "") }
    var description by remember(role) { mutableStateOf(role?.description ?: "") }
    var active by remember(role) { mutableStateOf(role?.active ?: true) }
    val selectedPermissions = remember(role) {
        mutableStateOf(role?.permissions?.toSet() ?: emptySet())
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = if (role == null) "Utwórz Nową Rolę" else "Edytuj Rolę",
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nazwa roli (np. ADMIN)") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = isSaving
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Opis (opcjonalnie)") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = isSaving
                )
                Spacer(Modifier.height(8.dp))
                FormSwitchRow(
                    text = "Rola aktywna",
                    checked = active,
                    onCheckedChange = { active = it },
                    enabled = !isSaving
                )
                Spacer(Modifier.height(16.dp))
                Text("Uprawnienia:", style = MaterialTheme.typography.titleMedium)

                Column(modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp)
                    .verticalScroll(rememberScrollState())
                ) {
                    allPermissions.sortedBy { it.name }.forEach { permission ->
                        PermissionCheckboxRow(
                            permission = permission,
                            isChecked = selectedPermissions.value.contains(permission.name),
                            onCheckedChange = {
                                val current = selectedPermissions.value.toMutableSet()
                                if (it) current.add(permission.name)
                                else current.remove(permission.name)
                                selectedPermissions.value = current
                            },
                            enabled = !isSaving
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
                if (error != null) {
                    Text(text = error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss, enabled = !isSaving) {
                        Text("Anuluj")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val request = RoleRequest(
                                name = name.uppercase(),
                                description = description.ifBlank { null },
                                permissions = selectedPermissions.value,
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

@Composable
private fun PermissionCheckboxRow(
    permission: PermissionResponse,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onCheckedChange(!isChecked) }
            .padding(vertical = 4.dp)
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            enabled = enabled
        )
        Spacer(Modifier.width(8.dp))
        Column {
            Text(permission.name, style = MaterialTheme.typography.bodyMedium)
            Text(permission.description ?: "${permission.action} on ${permission.resource}", style = MaterialTheme.typography.bodySmall)
        }
    }
}

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