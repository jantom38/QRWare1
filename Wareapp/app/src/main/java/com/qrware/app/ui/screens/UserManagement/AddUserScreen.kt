package com.qrware.app.ui.screens.UserManagement

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.qrware.app.ui.viewmodel.AddUserUiState
import com.qrware.app.ui.viewmodel.AddUserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddUserScreen(
    navController: NavController,
    viewModel: AddUserViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Efekt do obsługi nawigacji powrotnej po sukcesie
    LaunchedEffect(uiState.createSuccess) {
        if (uiState.createSuccess) {
            Toast.makeText(context, "Utworzono użytkownika", Toast.LENGTH_SHORT).show()
            viewModel.onCreateSuccessConsumed() // Zresetuj flagę
            navController.popBackStack() // Wróć do listy
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dodaj Nowego Użytkownika") },
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
            AddUserForm(
                uiState = uiState,
                viewModel = viewModel
            )
        }
    }
}

@Composable
private fun AddUserForm(
    uiState: AddUserUiState,
    viewModel: AddUserViewModel
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
            value = formState.username,
            onValueChange = viewModel::onUsernameChange,
            label = { Text("Nazwa użytkownika (login)") },
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
            value = formState.password,
            onValueChange = viewModel::onPasswordChange,
            label = { Text("Hasło (min. 8 znaków)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            readOnly = uiState.isSaving,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        Divider(modifier = Modifier.padding(vertical = 8.dp))

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
            label = { Text("Role (np. USER, ADMIN)") },
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
            onClick = viewModel::createUser,
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
                Text("Utwórz użytkownika")
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