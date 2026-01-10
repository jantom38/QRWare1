package com.qrware.app.ui.screens.UserManagement

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.qrware.app.data.model.AdminUserResponse
import com.qrware.app.ui.viewmodel.UserManagament.ListUsersViewModel
import kotlinx.coroutines.flow.filter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListUsersScreen(
    navController: NavController,
    viewModel: ListUsersViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(true) {
        viewModel.refreshList()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lista Użytkowników") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróć")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate("admin_add_user")
                }
            ) {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = "Dodaj Użytkownika"
                )
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
                uiState.isLoading && uiState.users.isEmpty() -> {
                    CircularProgressIndicator()
                }

                uiState.error != null && uiState.users.isEmpty() -> {
                    ErrorState(message = uiState.error!!)
                }

                uiState.users.isEmpty() -> {
                    Text("Brak użytkowników do wyświetlenia.")
                }

                else -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        OutlinedTextField(
                            value = uiState.searchQuery,
                            onValueChange = { viewModel.searchUsers(it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            placeholder = { Text("Szukaj użytkowników...") },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = "Szukaj")
                            },
                            singleLine = true
                        )

                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(uiState.users, key = { it.id }) { user ->
                                UserListItem(
                                    user = user,
                                    onClick = {
                                        navController.navigate("admin_edit_user/${user.id}")
                                    },
                                    onDeleteClick = {
                                        viewModel.requestDeleteUser(user)
                                    },
                                    onResetPasswordClick = {
                                        viewModel.requestPasswordReset(user.email)
                                    }
                                )
                            }

                            if (uiState.isLoading && uiState.users.isNotEmpty()) {
                                item {
                                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator()
                                    }
                                }
                            }
                        }
                    }

                    LaunchedEffect(listState) {
                        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
                            .filter { index ->
                                index != null && index >= uiState.users.size - 5
                            }
                            .collect {
                                viewModel.loadNextPage()
                            }
                    }
                }
            }

            if (uiState.showDeleteDialog) {
                DeleteConfirmationDialog(
                    userName = uiState.userToDelete?.username ?: "użytkownika",
                    onConfirm = { viewModel.confirmDeleteUser() },
                    onDismiss = { viewModel.dismissDeleteDialog() }
                )
            }

            if (uiState.error != null && uiState.users.isNotEmpty()) {
                SnackbarHost(
                    hostState = remember { SnackbarHostState() }
                        .apply {
                            LaunchedEffect(uiState.error) {
                                showSnackbar(
                                    message = uiState.error!!,
                                    duration = SnackbarDuration.Short
                                )
                            }
                        },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
            
            if (uiState.successMessage != null) {
                SnackbarHost(
                    hostState = remember { SnackbarHostState() }
                        .apply {
                            LaunchedEffect(uiState.successMessage) {
                                showSnackbar(
                                    message = uiState.successMessage!!,
                                    duration = SnackbarDuration.Short
                                )
                                viewModel.clearSuccessMessage()
                            }
                        },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UserListItem(
    user: AdminUserResponse,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onResetPasswordClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = if (user.active && user.accountNonLocked) "Aktywny" else "Nieaktywny/Zablokowany",
                tint = if (user.active && user.accountNonLocked) Color(0xFF4CAF50) else Color(0xFFF44336)
            )

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${user.firstName} ${user.lastName}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Role: ${user.roles.joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall,
                    fontStyle = FontStyle.Italic
                )
            }
            Spacer(Modifier.width(8.dp))

            IconButton(onClick = onResetPasswordClick) {
                Icon(
                    imageVector = Icons.Default.LockReset,
                    contentDescription = "Resetuj hasło",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Usuń użytkownika",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
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

@Composable
private fun DeleteConfirmationDialog(
    userName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Potwierdź usunięcie") },
        text = { Text("Czy na pewno chcesz trwale usunąć użytkownika $userName? Tej operacji nie można cofnąć.") },
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