package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.RemedyEntity
import com.example.data.local.StockMovementEntity
import com.example.data.model.MovementReason
import com.example.data.model.formatFullDate
import com.example.data.model.formatTimestamp
import com.example.ui.components.AdjustStockDialog
import com.example.ui.components.ExpiryBadge
import com.example.ui.components.PotencyBadge
import com.example.ui.components.StockStatusBadge
import com.example.ui.theme.MonoFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemedyDetailScreen(
    remedy: RemedyEntity?,
    movements: List<StockMovementEntity>,
    onBack: () -> Unit,
    onEdit: (RemedyEntity) -> Unit,
    onDelete: (RemedyEntity) -> Unit,
    onAdjustStock: (RemedyEntity, Int, MovementReason, String, String?, Long?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets(0),
                title = {
                    Text(
                        text = remedy?.name ?: stringResource(R.string.remedy_details),
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("detail_back_button"),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { innerPadding ->
        if (remedy == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Remedy not found",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.TopCenter,
            ) {
                RemedyDetailContent(
                    remedy = remedy,
                    movements = movements,
                    onEdit = { onEdit(remedy) },
                    onDelete = {
                        onDelete(remedy)
                        onBack()
                    },
                    onAdjustStock = { delta, reason, note, batch, exp ->
                        onAdjustStock(remedy, delta, reason, note, batch, exp)
                    },
                    modifier = Modifier.widthIn(max = 680.dp),
                )
            }
        }
    }
}

@Composable
fun RemedyDetailContent(
    remedy: RemedyEntity,
    movements: List<StockMovementEntity>,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAdjustStock: (Int, MovementReason, String, String?, Long?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showAdjustDialog by remember { mutableStateOf(false) }
    var adjustInitialRestock by remember { mutableStateOf(true) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // Primary Summary Card
        item(key = "detail_summary") {
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top,
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = remedy.name,
                                style = MaterialTheme.typography.headlineSmall,
                            )
                            if (remedy.commonName.isNotBlank()) {
                                Text(
                                    text = remedy.commonName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        PotencyBadge(potency = remedy.potency)
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        StockStatusBadge(remedy = remedy)
                        ExpiryBadge(expiryDateMillis = remedy.expiryDateMillis)
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(
                                text = "Current Stock",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = "${remedy.quantity} units",
                                style = MaterialTheme.typography.displaySmall.copy(
                                    fontFamily = MonoFontFamily,
                                ),
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Reorder Threshold",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = "≤ ${remedy.minStockLevel} units",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = MonoFontFamily,
                                ),
                            )
                        }
                    }

                    // Quick Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OutlinedButton(
                            onClick = {
                                adjustInitialRestock = false
                                showAdjustDialog = true
                            },
                            enabled = remedy.quantity > 0,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("detail_dispense_button"),
                        ) {
                            Icon(Icons.Filled.Remove, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(stringResource(R.string.dispense))
                        }
                        Button(
                            onClick = {
                                adjustInitialRestock = true
                                showAdjustDialog = true
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("detail_restock_button"),
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(stringResource(R.string.restock))
                        }
                    }
                }
            }
        }

        // Metadata Specs Card
        item(key = "detail_specs") {
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = "Dispensary Specifications",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    SpecRow("Form", remedy.remedyForm.label)
                    SpecRow("Pack Size", remedy.bottleSize.ifBlank { "—" })
                    SpecRow("Brand", remedy.brand.ifBlank { "—" })
                    SpecRow("Batch / Lot", remedy.batchNumber.ifBlank { "—" })
                    SpecRow(
                        "Expiry Date",
                        remedy.expiryDateMillis?.let { formatFullDate(it) } ?: "Not set",
                    )
                    SpecRow("Rack / Shelf", remedy.shelfLocation.ifBlank { "—" })
                    SpecRow("Last Updated", formatTimestamp(remedy.updatedAt))
                    if (remedy.notes.isNotBlank()) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Text(
                            text = "Clinical & Storage Notes",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = remedy.notes,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }

                    Spacer(Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OutlinedButton(
                            onClick = onEdit,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("detail_edit_button"),
                        ) {
                            Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Edit Details")
                        }
                        OutlinedButton(
                            onClick = { showDeleteConfirm = true },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error,
                            ),
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("detail_delete_button"),
                        ) {
                            Icon(Icons.Outlined.DeleteOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(stringResource(R.string.delete))
                        }
                    }
                }
            }
        }

        // Per-remedy Movement History
        item(key = "detail_history_hdr") {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 4.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Tune,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = "Movement History (${movements.size})",
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }

        if (movements.isEmpty()) {
            item(key = "detail_history_empty") {
                Text(
                    text = "No stock changes logged for this remedy yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            items(movements, key = { it.id }) { movement ->
                MovementCard(movement = movement)
            }
        }
    }

    if (showAdjustDialog) {
        AdjustStockDialog(
            remedy = remedy,
            initialIsRestock = adjustInitialRestock,
            onDismiss = { showAdjustDialog = false },
            onConfirm = { delta, reason, note, batch, exp ->
                onAdjustStock(delta, reason, note, batch, exp)
                showAdjustDialog = false
            },
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.confirm_delete_title)) },
            text = { Text(stringResource(R.string.confirm_delete_message)) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                    ),
                    modifier = Modifier.testTag("confirm_delete_button"),
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }
}

@Composable
private fun SpecRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
