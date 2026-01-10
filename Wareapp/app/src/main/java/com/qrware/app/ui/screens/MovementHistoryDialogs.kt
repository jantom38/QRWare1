package com.qrware.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.qrware.app.data.dto.MovementHistoryDTO
import com.qrware.app.data.model.MovementType
import com.qrware.app.ui.viewmodel.MovementHistoryUiState
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiltersDialog(
    uiState: MovementHistoryUiState,
    onDismiss: () -> Unit,
    onApplyFilters: (MovementType?, LocalDate?, LocalDate?, Boolean) -> Unit
) {
    var selectedMovementType by remember { mutableStateOf(uiState.selectedMovementType) }
    var startDate by remember { mutableStateOf(uiState.selectedStartDate) }
    var endDate by remember { mutableStateOf(uiState.selectedEndDate) }
    var showPendingOnly by remember { mutableStateOf(uiState.showPendingApprovalOnly) }
    var showDatePicker by remember { mutableStateOf(false) }
    var datePickerType by remember { mutableStateOf("start") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Filtry",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = "Typ ruchu",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        onClick = { selectedMovementType = null },
                        label = { Text("Wszystkie") },
                        selected = selectedMovementType == null,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                val movementTypeCategories = listOf(
                    "Przyjęcia" to MovementType.getInboundTypes(),
                    "Wydania" to MovementType.getOutboundTypes(),
                    "Ruchy" to MovementType.getMovementTypes(),
                    "Korekty" to MovementType.getAdjustmentTypes(),
                    "Zamówienia" to MovementType.getOrderRelatedTypes(),
                    "Jakość" to MovementType.getQualityRelatedTypes()
                )

                movementTypeCategories.forEach { (categoryName, types) ->
                    if (types.isNotEmpty()) {
                        Text(
                            text = categoryName,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        )
                        
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            types.forEach { type ->
                                FilterChip(
                                    onClick = { 
                                        selectedMovementType = if (selectedMovementType == type) null else type 
                                    },
                                    label = { Text(type.displayName, style = MaterialTheme.typography.bodySmall) },
                                    selected = selectedMovementType == type
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Zakres dat",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { 
                            datePickerType = "start"
                            showDatePicker = true 
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.DateRange, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(startDate?.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) ?: "Od")
                    }

                    OutlinedButton(
                        onClick = { 
                            datePickerType = "end"
                            showDatePicker = true 
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.DateRange, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(endDate?.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) ?: "Do")
                    }
                }

                if (startDate != null || endDate != null) {
                    TextButton(
                        onClick = { 
                            startDate = null
                            endDate = null
                        },
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text("Wyczyść daty")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Status zatwierdzenia",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = showPendingOnly,
                            onClick = { showPendingOnly = !showPendingOnly },
                            role = Role.Checkbox
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = showPendingOnly,
                        onCheckedChange = { showPendingOnly = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Tylko oczekujące zatwierdzenia")
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Anuluj")
                    }

                    Button(
                        onClick = { 
                            onApplyFilters(selectedMovementType, startDate, endDate, showPendingOnly)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Zastosuj")
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val selectedDate = java.time.Instant.ofEpochMilli(millis)
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDate()
                            
                            if (datePickerType == "start") {
                                startDate = selectedDate
                            } else {
                                endDate = selectedDate
                            }
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Anuluj")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun SearchDialog(
    currentQuery: String,
    onDismiss: () -> Unit,
    onSearch: (String, String) -> Unit
) {
    var query by remember { mutableStateOf(currentQuery) }
    var searchIn by remember { mutableStateOf("reason") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Wyszukiwanie",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Szukana fraza") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Szukaj w:",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Column(modifier = Modifier.selectableGroup()) {
                    listOf(
                        "reason" to "Powód",
                        "notes" to "Notatki",
                        "both" to "Powód i notatki"
                    ).forEach { (value, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = searchIn == value,
                                    onClick = { searchIn = value },
                                    role = Role.RadioButton
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = searchIn == value,
                                onClick = { searchIn = value }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(label)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Anuluj")
                    }

                    Button(
                        onClick = { onSearch(query, searchIn) },
                        modifier = Modifier.weight(1f),
                        enabled = query.isNotBlank()
                    ) {
                        Text("Szukaj")
                    }
                }
            }
        }
    }
}

@Composable
fun ApprovalDialog(
    movement: MovementHistoryDTO,
    onDismiss: () -> Unit,
    onApprove: (String?) -> Unit
) {
    var comment by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Zatwierdzenie ruchu",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "${movement.getMovementIcon()} ${movement.movementType?.displayName ?: "Ruch magazynowy"}",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = movement.inventoryItem.product.name,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "SKU: ${movement.inventoryItem.product.sku}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (movement.isQuantityChange()) {
                            Text(
                                text = "Zmiana ilości: ${movement.getQuantityChangeText()}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        movement.reason?.let {
                            Text(
                                text = "Powód: $it",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Text(
                            text = "Data: ${movement.getFormattedMovementDate()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        movement.userName?.let {
                            Text(
                                text = "Użytkownik: $it",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Komentarz (opcjonalnie)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Anuluj")
                    }

                    Button(
                        onClick = { onApprove(comment.takeIf { it.isNotBlank() }) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Zatwierdź")
                    }
                }
            }
        }
    }
}

@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    content: @Composable () -> Unit
) {
    Column(modifier = modifier) {
        content()
    }
}