// Ścieżka: app/src/main/java/com/qrware/app/ui/screens/UserManagement/ListUsersScreen.kt
package com.qrware.app.ui.screens.UserManagement

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
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

    // ZMIANA: Odśwież listę, gdy ekran staje się widoczny
    // (np. po powrocie z ekranu dodawania/edycji)
    // Używamy 'true' jako klucza, aby wykonało się raz przy wejściu
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
                // 1. Stan ładowania
                uiState.isLoading && uiState.users.isEmpty() -> {
                    CircularProgressIndicator()
                }

                // 2. Stan błędu
                uiState.error != null && uiState.users.isEmpty() -> { // Pokaż błąd tylko jeśli lista jest pusta
                    ErrorState(message = uiState.error!!)
                }

                // 3. Lista jest pusta
                uiState.users.isEmpty() -> {
                    Text("Brak użytkowników do wyświetlenia.")
                }

                // 4. Stan sukcesu - wyświetlamy listę
                else -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Pole wyszukiwania
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
                                        // ZMIANA: Nawigacja do ekranu edycji użytkownika
                                        // TODO: Zastąp "admin_edit_user" właściwą ścieżką
                                        navController.navigate("admin_edit_user/${user.id}")
                                    },
                                    // ZMIANA: Przekazanie akcji usuwania
                                    onDeleteClick = {
                                        viewModel.requestDeleteUser(user)
                                    }
                                )
                            }

                            // Wskaźnik ładowania "więcej" na dole
                            if (uiState.isLoading && uiState.users.isNotEmpty()) {
                                item {
                                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator()
                                    }
                                }
                            }
                        }
                    }

                    // Logika "Infinite Scroll"
                    LaunchedEffect(listState) {
                        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
                            .filter { index ->
                                index != null && index >= uiState.users.size - 5 // Załaduj, gdy zostało 5 elementów do końca
                            }
                            .collect {
                                viewModel.loadNextPage()
                            }
                    }
                }
            }

            // ZMIANA: Wyświetlanie okna dialogowego potwierdzenia usunięcia
            if (uiState.showDeleteDialog) {
                DeleteConfirmationDialog(
                    userName = uiState.userToDelete?.username ?: "użytkownika",
                    onConfirm = { viewModel.confirmDeleteUser() },
                    onDismiss = { viewModel.dismissDeleteDialog() }
                )
            }

            // ZMIANA: Mały SnackBar dla błędów (gdy lista nie jest pusta)
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
        }
    }
}

/**
 * ZMIANA: Komponent dla pojedynczego elementu na liście użytkowników
 * (dodano przycisk usuwania).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UserListItem(
    user: AdminUserResponse,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit // NOWY PARAMETR
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
            // Wskaźnik statusu (aktywny / zablokowany)
            Icon(
                imageVector = Icons.Default.Warning, // Zmień ikonę w zależności od statusu
                contentDescription = if (user.active && user.accountNonLocked) "Aktywny" else "Nieaktywny/Zablokowany",
                tint = if (user.active && user.accountNonLocked) Color(0xFF4CAF50) else Color(0xFFF44336) // Lepsze kolory Green/Red
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

            // NOWY PRZYCISK: Usuwanie
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

/**
 * Komponent dla stanu błędu.
 */
@Composable
private fun ErrorState(message: String) {
    // ... (bez zmian)
}


// --- NOWY KOMPONENT ---
/**
 * Okno dialogowe potwierdzające usunięcie użytkownika.
 */
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