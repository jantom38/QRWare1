package com.qrware.app.ui.screens.UserManagement

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

internal object AdminRoutes {
    const val USER_LIST = "manage_users_list"
    const val ADD_USER = "manage_users_add"
    const val MANAGE_ROLES = "manage_roles"
    const val MANAGE_PERMISSIONS = "manage_permissions"
    const val CHANGE_PASSWORD = "change_password"
    const val FORGOT_PASSWORD = "forgot_password"
}

private data class AdminTile(
    val title: String,
    val icon: ImageVector,
    val route: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageUsersScreen(navController: NavController) {

    val tiles = listOf(
        AdminTile("Użytkownicy", Icons.Default.People, AdminRoutes.USER_LIST),
        AdminTile("Role", Icons.Default.Shield, AdminRoutes.MANAGE_ROLES),
        AdminTile("Uprawnienia", Icons.Default.Security, AdminRoutes.MANAGE_PERMISSIONS),
        AdminTile("Zmień Hasło", Icons.Default.Lock, AdminRoutes.CHANGE_PASSWORD),
        AdminTile("Reset Hasła", Icons.Default.LockReset, AdminRoutes.FORGOT_PASSWORD)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel Administratora") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróć")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(tiles) { tile ->
                AdminActionTile(
                    title = tile.title,
                    icon = tile.icon,
                    onClick = { navController.navigate(tile.route) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminActionTile(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .aspectRatio(1f),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}