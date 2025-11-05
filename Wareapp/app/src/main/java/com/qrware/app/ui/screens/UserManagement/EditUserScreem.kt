package com.qrware.app.ui.screens.UserManagement

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.qrware.app.ui.viewmodel.UserManagament.EditUserUiState
import com.qrware.app.ui.viewmodel.UserManagament.EditUserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditUserScreen(
    navController: NavController,
    viewModel: EditUserViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Efekt do obsługi nawigacji powrotnej po sukcesie
    LaunchedEffect(uiState.updateSuccess) {
        if (uiState.updateSuccess) {
            Toast.makeText(context, "Zaktualizowano użytkownika", Toast.LENGTH_SHORT).show()
            viewModel.onUpdateSuccessConsumed() // Zresetuj flagę
            navController.popBackStack() // Wróć do listy
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.isLoading) "Ładowanie..." else "Edytuj: ${uiState.user?.username ?: ""}") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróć")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator()
                }
                uiState.error != null && uiState.user == null -> {
                    // Poważny błąd - nie udało się pobrać usera
                    ErrorState(message = uiState.error!!)
                }
                else -> {
                    // Wyświetl formularz
                    EditUserForm(
                        uiState = uiState,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@Composable
private fun EditUserForm(
    uiState: EditUserUiState,
    viewModel: EditUserViewModel
) {
    val formState = uiState.formState
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ----- Pola tekstowe -----
        OutlinedTextField(
            value = formState.firstName,
            onValueChange = viewModel::onFirstNameChange,
            label = { Text("Imię") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            readOnly = uiState.isSaving
        )

        OutlinedTextField(
            value = formState.lastName,
            onValueChange = viewModel::onLastNameChange,
            label = { Text("Nazwisko") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            readOnly = uiState.isSaving
        )

        OutlinedTextField(
            value = formState.email,
            onValueChange = viewModel::onEmailChange,
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            readOnly = uiState.isSaving
        )

        OutlinedTextField(
            value = formState.phone,
            onValueChange = viewModel::onPhoneChange,
            label = { Text("Telefon (opcjonalnie)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true,
            readOnly = uiState.isSaving
        )

        OutlinedTextField(
            value = formState.roles.joinToString(", "),
            onValueChange = viewModel::onRolesChange,
            label = { Text("Role (oddzielone przecinkami)") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = uiState.isSaving
        )

        // ----- Przełączniki (Switches) -----
        FormSwitchRow(
            text = "Konto aktywne",
            checked = formState.active,
            onCheckedChange = viewModel::onActiveChange,
            enabled = !uiState.isSaving
        )

        FormSwitchRow(
            text = "Email zweryfikowany",
            checked = formState.emailVerified,
            onCheckedChange = viewModel::onEmailVerifiedChange,
            enabled = !uiState.isSaving
        )

        Spacer(Modifier.height(16.dp))

        // ----- Przycisk Zapisu i Błędy -----
        if (uiState.error != null) {
            Text(
                text = uiState.error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Button(
            onClick = viewModel::saveUser,
            enabled = !uiState.isSaving,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (uiState.isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Zapisz zmiany")
            }
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

/**
 * Prosty komponent błędu (skopiowany z ListUsersScreen).
 */
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