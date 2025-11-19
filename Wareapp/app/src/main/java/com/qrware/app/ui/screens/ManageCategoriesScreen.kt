package com.qrware.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.qrware.app.data.dto.CategoryDTO
import com.qrware.app.data.model.CreateCategoryRequest
import com.qrware.app.data.model.UpdateCategoryRequest
import com.qrware.app.di.AppContainer
import com.qrware.app.ui.viewmodel.ProductsManagement.CategoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageCategoriesScreen(
    navController: NavController,
    appContainer: AppContainer
) {
    val viewModel: CategoryViewModel = viewModel(
        factory = appContainer.categoryViewModelFactory
    )
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<CategoryDTO?>(null) }
    var selectedParentCategory by remember { mutableStateOf<CategoryDTO?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var showHierarchical by remember { mutableStateOf(true) }
    var expandedCategories by remember { mutableStateOf(setOf<Long>()) }

    LaunchedEffect(uiState.error, uiState.successMessage) {
        if (uiState.error != null || uiState.successMessage != null) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Zarządzanie Kategoriami") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wróć")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showHierarchical = !showHierarchical }
                    ) {
                        Icon(
                            if (showHierarchical) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (showHierarchical) "Widok listy" else "Widok drzewa"
                        )
                    }
                    IconButton(onClick = { 
                        selectedParentCategory = null
                        showAddDialog = true 
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Dodaj kategorię główną")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Pasek wyszukiwania
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { 
                    searchQuery = it
                    if (it.isBlank()) {
                        viewModel.loadCategories()
                    } else {
                        viewModel.searchCategories(it)
                    }
                },
                label = { Text("Szukaj kategorii") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Filtry
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.loadCategories() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Wszystkie")
                }
                Button(
                    onClick = { viewModel.loadActiveCategories() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Aktywne")
                }
                Button(
                    onClick = { viewModel.loadRootCategories() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Główne")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Komunikaty błędów/sukcesu
            uiState.error?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Text(
                        text = error,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            uiState.successMessage?.let { message ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Text(
                        text = message,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Lista kategorii
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (showHierarchical) {
                        // Widok hierarchiczny - tylko kategorie główne
                        val rootCategories = uiState.categories.filter { it.parent == null }
                        items(rootCategories) { category ->
                            HierarchicalCategoryCard(
                                category = category,
                                allCategories = uiState.categories,
                                expandedCategories = expandedCategories,
                                level = 0,
                                onExpandToggle = { categoryId ->
                                    expandedCategories = if (expandedCategories.contains(categoryId)) {
                                        expandedCategories - categoryId
                                    } else {
                                        expandedCategories + categoryId
                                    }
                                },
                                onEdit = { categoryToEdit ->
                                    selectedCategory = categoryToEdit
                                    showEditDialog = true
                                },
                                onDelete = { categoryToDelete ->
                                    viewModel.deleteCategory(categoryToDelete.id)
                                },
                                onToggleActive = { categoryToToggle ->
                                    viewModel.toggleCategoryActive(categoryToToggle.id)
                                },
                                onAddSubcategory = { parentCategory ->
                                    selectedParentCategory = parentCategory
                                    showAddDialog = true
                                }
                            )
                        }
                    } else {
                        // Widok listy - wszystkie kategorie
                        items(uiState.categories) { category ->
                            CategoryCard(
                                category = category,
                                onEdit = {
                                    selectedCategory = category
                                    showEditDialog = true
                                },
                                onDelete = {
                                    viewModel.deleteCategory(category.id)
                                },
                                onToggleActive = {
                                    viewModel.toggleCategoryActive(category.id)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialog dodawania kategorii
    if (showAddDialog) {
        AddCategoryDialog(
            onDismiss = { 
                showAddDialog = false
                selectedParentCategory = null
            },
            onConfirm = { request ->
                // Dodaj parentId jeśli jest ustawione
                val finalRequest = if (selectedParentCategory != null) {
                    request.copy(parentId = selectedParentCategory!!.id)
                } else {
                    request
                }
                viewModel.createCategory(finalRequest)
                showAddDialog = false
                selectedParentCategory = null
            },
            initialParentCategory = selectedParentCategory
        )
    }

    // Dialog edycji kategorii
    if (showEditDialog && selectedCategory != null) {
        EditCategoryDialog(
            category = selectedCategory!!,
            onDismiss = { 
                showEditDialog = false
                selectedCategory = null
            },
            onConfirm = { request ->
                viewModel.updateCategory(selectedCategory!!.id, request)
                showEditDialog = false
                selectedCategory = null
            }
        )
    }
}

@Composable
fun CategoryCard(
    category: CategoryDTO,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleActive: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Kod: ${category.code}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    category.description?.let { description ->
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    category.fullPath?.let { path ->
                        Text(
                            text = "Ścieżka: $path",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    if (category.requiresSpecialHandling == true) {
                        Text(
                            text = "⚠️ Wymaga specjalnego przechowywania",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    if (category.storageTemperatureMin != null || category.storageTemperatureMax != null) {
                        Text(
                            text = "🌡️ Temperatura: ${category.storageTemperatureMin ?: "?"}°C - ${category.storageTemperatureMax ?: "?"}°C",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    if (category.storageHumidityMin != null || category.storageHumidityMax != null) {
                        Text(
                            text = "💧 Wilgotność: ${category.storageHumidityMin ?: "?"}% - ${category.storageHumidityMax ?: "?"}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
                
                Row {
                    Badge(
                        containerColor = if (category.active) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.error
                    ) {
                        Text(if (category.active) "Aktywna" else "Nieaktywna")
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edytuj")
                }

                IconButton(onClick = onToggleActive) {
                    Icon(
                        if (category.active) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (category.active) "Dezaktywuj" else "Aktywuj"
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Usuń")
                }
            }
        }
    }
}

@Composable
fun AddCategoryDialog(
    onDismiss: () -> Unit,
    onConfirm: (CreateCategoryRequest) -> Unit,
    initialParentCategory: CategoryDTO? = null
) {
    var code by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var icon by remember { mutableStateOf("") }
    var color by remember { mutableStateOf("") }
    var sortOrder by remember { mutableStateOf("") }
    var requiresSpecialHandling by remember { mutableStateOf(false) }
    var storageTemperatureMin by remember { mutableStateOf("") }
    var storageTemperatureMax by remember { mutableStateOf("") }
    var storageHumidityMin by remember { mutableStateOf("") }
    var storageHumidityMax by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(if (initialParentCategory != null) 
                "Dodaj podkategorię do: ${initialParentCategory.name}" 
                else "Dodaj kategorię główną"
            ) 
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.height(400.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        label = { Text("Kod kategorii*") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nazwa kategorii*") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Opis") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2
                    )
                }
                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = icon,
                            onValueChange = { icon = it },
                            label = { Text("Ikona") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = color,
                            onValueChange = { color = it },
                            label = { Text("Kolor") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            placeholder = { Text("#FF5722") }
                        )
                    }
                }
                item {
                    OutlinedTextField(
                        value = sortOrder,
                        onValueChange = { sortOrder = it },
                        label = { Text("Kolejność sortowania") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = requiresSpecialHandling,
                            onCheckedChange = { requiresSpecialHandling = it }
                        )
                        Text("Wymaga specjalnego przechowywania", modifier = Modifier.padding(start = 8.dp))
                    }
                }
                item {
                    Text("Warunki przechowywania", style = MaterialTheme.typography.titleSmall)
                }
                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = storageTemperatureMin,
                            onValueChange = { storageTemperatureMin = it },
                            label = { Text("Temp. min (°C)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = storageTemperatureMax,
                            onValueChange = { storageTemperatureMax = it },
                            label = { Text("Temp. max (°C)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }
                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = storageHumidityMin,
                            onValueChange = { storageHumidityMin = it },
                            label = { Text("Wilg. min (%)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = storageHumidityMax,
                            onValueChange = { storageHumidityMax = it },
                            label = { Text("Wilg. max (%)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        CreateCategoryRequest(
                            code = code,
                            name = name,
                            description = description.takeIf { it.isNotBlank() },
                            icon = icon.takeIf { it.isNotBlank() },
                            color = color.takeIf { it.isNotBlank() },
                            sortOrder = sortOrder.toIntOrNull(),
                            requiresSpecialHandling = requiresSpecialHandling,
                            storageTemperatureMin = storageTemperatureMin.toIntOrNull(),
                            storageTemperatureMax = storageTemperatureMax.toIntOrNull(),
                            storageHumidityMin = storageHumidityMin.toIntOrNull(),
                            storageHumidityMax = storageHumidityMax.toIntOrNull()
                        )
                    )
                },
                enabled = code.isNotBlank() && name.isNotBlank()
            ) {
                Text("Dodaj")
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
fun EditCategoryDialog(
    category: CategoryDTO,
    onDismiss: () -> Unit,
    onConfirm: (UpdateCategoryRequest) -> Unit
) {
    var name by remember { mutableStateOf(category.name) }
    var description by remember { mutableStateOf(category.description ?: "") }
    var icon by remember { mutableStateOf(category.icon ?: "") }
    var color by remember { mutableStateOf(category.color ?: "") }
    var sortOrder by remember { mutableStateOf(category.sortOrder?.toString() ?: "") }
    var requiresSpecialHandling by remember { mutableStateOf(category.requiresSpecialHandling ?: false) }
    var storageTemperatureMin by remember { mutableStateOf(category.storageTemperatureMin?.toString() ?: "") }
    var storageTemperatureMax by remember { mutableStateOf(category.storageTemperatureMax?.toString() ?: "") }
    var storageHumidityMin by remember { mutableStateOf(category.storageHumidityMin?.toString() ?: "") }
    var storageHumidityMax by remember { mutableStateOf(category.storageHumidityMax?.toString() ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edytuj kategorię") },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.height(400.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nazwa kategorii*") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Opis") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2
                    )
                }
                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = icon,
                            onValueChange = { icon = it },
                            label = { Text("Ikona") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = color,
                            onValueChange = { color = it },
                            label = { Text("Kolor") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            placeholder = { Text("#FF5722") }
                        )
                    }
                }
                item {
                    OutlinedTextField(
                        value = sortOrder,
                        onValueChange = { sortOrder = it },
                        label = { Text("Kolejność sortowania") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = requiresSpecialHandling,
                            onCheckedChange = { requiresSpecialHandling = it }
                        )
                        Text("Wymaga specjalnego przechowywania", modifier = Modifier.padding(start = 8.dp))
                    }
                }
                item {
                    Text("Warunki przechowywania", style = MaterialTheme.typography.titleSmall)
                }
                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = storageTemperatureMin,
                            onValueChange = { storageTemperatureMin = it },
                            label = { Text("Temp. min (°C)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = storageTemperatureMax,
                            onValueChange = { storageTemperatureMax = it },
                            label = { Text("Temp. max (°C)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }
                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = storageHumidityMin,
                            onValueChange = { storageHumidityMin = it },
                            label = { Text("Wilg. min (%)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = storageHumidityMax,
                            onValueChange = { storageHumidityMax = it },
                            label = { Text("Wilg. max (%)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        UpdateCategoryRequest(
                            name = name.takeIf { it != category.name },
                            description = description.takeIf { it != (category.description ?: "") },
                            icon = icon.takeIf { it != (category.icon ?: "") },
                            color = color.takeIf { it != (category.color ?: "") },
                            sortOrder = sortOrder.toIntOrNull().takeIf { it != category.sortOrder },
                            requiresSpecialHandling = requiresSpecialHandling.takeIf { it != (category.requiresSpecialHandling ?: false) },
                            storageTemperatureMin = storageTemperatureMin.toIntOrNull().takeIf { it != category.storageTemperatureMin },
                            storageTemperatureMax = storageTemperatureMax.toIntOrNull().takeIf { it != category.storageTemperatureMax },
                            storageHumidityMin = storageHumidityMin.toIntOrNull().takeIf { it != category.storageHumidityMin },
                            storageHumidityMax = storageHumidityMax.toIntOrNull().takeIf { it != category.storageHumidityMax }
                        )
                    )
                },
                enabled = name.isNotBlank()
            ) {
                Text("Zapisz")
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
fun HierarchicalCategoryCard(
    category: CategoryDTO,
    allCategories: List<CategoryDTO>,
    expandedCategories: Set<Long>,
    level: Int = 0,
    onExpandToggle: (Long) -> Unit,
    onEdit: (CategoryDTO) -> Unit,
    onDelete: (CategoryDTO) -> Unit,
    onToggleActive: (CategoryDTO) -> Unit,
    onAddSubcategory: (CategoryDTO) -> Unit
) {
    val children = allCategories.filter { it.parent?.id == category.id }
    val isExpanded = expandedCategories.contains(category.id)
    
    Column {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = (level * 16).dp),
            colors = CardDefaults.cardColors(
                containerColor = if (level == 0) 
                    MaterialTheme.colorScheme.surface 
                else 
                    MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (level == 0) 4.dp else 2.dp
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        // Przycisk rozwijania jeśli ma dzieci
                        if (children.isNotEmpty()) {
                            IconButton(onClick = { onExpandToggle(category.id) }) {
                                Icon(
                                    if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = if (isExpanded) "Zwiń" else "Rozwiń"
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.width(48.dp))
                        }
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${category.name} (${category.code})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = if (level == 0) FontWeight.Bold else FontWeight.Normal
                            )
                            category.fullPath?.let { path ->
                                Text(
                                    text = "Ścieżka: $path",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                            category.description?.let { description ->
                                Text(
                                    text = description,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            if (category.requiresSpecialHandling == true) {
                                Text(
                                    text = "⚠️ Wymaga specjalnego przechowywania",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                            if (category.storageTemperatureMin != null || category.storageTemperatureMax != null) {
                                Text(
                                    text = "🌡️ Temperatura: ${category.storageTemperatureMin ?: "?"}°C - ${category.storageTemperatureMax ?: "?"}°C",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                            if (category.storageHumidityMin != null || category.storageHumidityMax != null) {
                                Text(
                                    text = "💧 Wilgotność: ${category.storageHumidityMin ?: "?"}% - ${category.storageHumidityMax ?: "?"}%",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                            if (children.isNotEmpty()) {
                                Text(
                                    text = "📁 Podkategorie: ${children.size}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                    
                    Row {
                        // Przycisk dodawania podkategorii
                        IconButton(onClick = { onAddSubcategory(category) }) {
                            Icon(Icons.Default.Add, contentDescription = "Dodaj podkategorię")
                        }
                        
                        IconButton(onClick = { onToggleActive(category) }) {
                            Icon(
                                if (category.active) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (category.active) "Dezaktywuj" else "Aktywuj",
                                tint = if (category.active) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.outline
                            )
                        }
                        
                        IconButton(onClick = { onEdit(category) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edytuj kategorię")
                        }
                        
                        IconButton(onClick = { onDelete(category) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Usuń kategorię")
                        }
                    }
                }
            }
        }
        
        // Renderowanie dzieci jeśli rozwinięte
        if (isExpanded && children.isNotEmpty()) {
            children.forEach { child ->
                HierarchicalCategoryCard(
                    category = child,
                    allCategories = allCategories,
                    expandedCategories = expandedCategories,
                    level = level + 1,
                    onExpandToggle = onExpandToggle,
                    onEdit = onEdit,
                    onDelete = onDelete,
                    onToggleActive = onToggleActive,
                    onAddSubcategory = onAddSubcategory
                )
            }
        }
    }
}