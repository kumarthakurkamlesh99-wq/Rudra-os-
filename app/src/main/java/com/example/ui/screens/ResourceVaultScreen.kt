package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.ResourceEntity
import com.example.ui.components.GlassCard
import com.example.ui.components.SectionHeader
import com.example.ui.theme.*
import com.example.ui.viewmodel.RudraViewModel
import kotlinx.coroutines.launch

@Composable
fun ResourceVaultScreen(
    viewModel: RudraViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val allResources by viewModel.allResources.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedSubjectFilter by remember { mutableStateOf("All") }
    var showAddDialog by remember { mutableStateOf(false) }

    val subjects = listOf("All", "Physics", "Chemistry", "Biology", "All Subjects")

    val filteredResources = remember(searchQuery, selectedSubjectFilter, allResources) {
        allResources.filter { res ->
            val matchSubject = selectedSubjectFilter == "All" || res.subjectName.equals(selectedSubjectFilter, ignoreCase = true)
            val matchQuery = searchQuery.isBlank() || res.title.contains(searchQuery, ignoreCase = true) || res.description.contains(searchQuery, ignoreCase = true) || res.tags.contains(searchQuery, ignoreCase = true)
            matchSubject && matchQuery
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // Search bar & Add Button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search formulas, PYQ notes, links...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("resource_search_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Button(
                    onClick = { showAddDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                    modifier = Modifier.testTag("add_resource_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                }
            }
        }

        // Subject Filter chips
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(subjects) { sub ->
                    FilterChip(
                        selected = selectedSubjectFilter == sub,
                        onClick = { selectedSubjectFilter = sub },
                        label = { Text(sub, fontSize = 12.sp) }
                    )
                }
            }
        }

        // Resources List
        if (filteredResources.isEmpty()) {
            item {
                GlassCard {
                    Text(
                        text = if (searchQuery.isNotBlank() || selectedSubjectFilter != "All") "No matching resources found." else "No resources saved yet. Tap '+' above to add formula sheets, notes, or links.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(filteredResources) { res ->
                ResourceItemCard(
                    resource = res,
                    onToggleFavorite = { fav ->
                        coroutineScope.launch {
                            viewModel.repository.toggleResourceFavorite(res.id, fav)
                        }
                    },
                    onOpen = {
                        if (res.urlOrPath.startsWith("http://") || res.urlOrPath.startsWith("https://")) {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(res.urlOrPath)).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            context.startActivity(intent)
                        }
                    },
                    onDelete = {
                        coroutineScope.launch {
                            viewModel.repository.deleteResource(res.id)
                        }
                    }
                )
            }
        }
    }

    if (showAddDialog) {
        AddResourceDialog(
            onDismiss = { showAddDialog = false },
            onSave = { newRes ->
                coroutineScope.launch {
                    viewModel.repository.insertResource(newRes)
                }
                showAddDialog = false
            }
        )
    }
}

@Composable
fun ResourceItemCard(
    resource: ResourceEntity,
    onToggleFavorite: (Boolean) -> Unit,
    onOpen: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = AccentNavy.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = resource.resourceType,
                            style = MaterialTheme.typography.labelSmall,
                            color = AccentElectricBlue,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    if (resource.subjectName.isNotBlank()) {
                        Text(
                            text = resource.subjectName,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = { onToggleFavorite(!resource.isFavorite) },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (resource.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "Favorite",
                        tint = if (resource.isFavorite) ScoreYellow else MaterialTheme.colorScheme.outline
                    )
                }
            }

            Text(
                text = resource.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (resource.description.isNotBlank()) {
                Text(
                    text = resource.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (resource.tags.isNotBlank()) {
                    Text(
                        text = "#${resource.tags}",
                        style = MaterialTheme.typography.labelSmall,
                        color = AccentCyan
                    )
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Row {
                    Button(
                        onClick = onOpen,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Open", fontSize = 12.sp)
                    }

                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
                    }
                }
            }
        }
    }
}

@Composable
fun AddResourceDialog(
    onDismiss: () -> Unit,
    onSave: (ResourceEntity) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var urlOrPath by remember { mutableStateOf("") }
    var subjectName by remember { mutableStateOf("Physics") }
    var resourceType by remember { mutableStateOf(ResourceEntity.TYPE_LINK) }
    var tags by remember { mutableStateOf("") }

    val types = listOf(
        ResourceEntity.TYPE_LINK,
        ResourceEntity.TYPE_FORMULA_SHEET,
        ResourceEntity.TYPE_BSEB_NOTE,
        ResourceEntity.TYPE_QUESTION_BANK
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Study Resource") },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Resource Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = urlOrPath,
                        onValueChange = { urlOrPath = it },
                        label = { Text("Web URL or Doc Path") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = subjectName,
                        onValueChange = { subjectName = it },
                        label = { Text("Subject (Physics/Chem/Bio/All)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description / Notes") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = tags,
                        onValueChange = { tags = it },
                        label = { Text("Tags (comma separated)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(
                            ResourceEntity(
                                title = title,
                                description = description,
                                urlOrPath = urlOrPath,
                                subjectName = subjectName,
                                resourceType = resourceType,
                                tags = tags
                            )
                        )
                    }
                }
            ) {
                Text("Save Resource")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
