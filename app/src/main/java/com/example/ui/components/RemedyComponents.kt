package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.RemedyEntity
import com.example.data.model.ExpiryStatus
import com.example.data.model.MovementReason
import com.example.data.model.daysUntilExpiry
import com.example.data.model.formatFullDate
import com.example.data.model.formatMonthYear
import com.example.ui.theme.LocalStockStatusColors
import com.example.ui.theme.MonoFontFamily

@Composable
fun PotencyBadge(
    potency: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
    ) {
        Text(
            text = potency.ifBlank { "—" },
            style = MaterialTheme.typography.labelMedium.copy(
                fontFamily = MonoFontFamily,
                fontWeight = FontWeight.Bold,
            ),
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            maxLines = 1,
        )
    }
}

@Composable
fun StockStatusBadge(
    remedy: RemedyEntity,
    modifier: Modifier = Modifier,
) {
    val statusColors = LocalStockStatusColors.current
    val (bg, fg, label) = when {
        remedy.isOutOfStock -> Triple(statusColors.criticalBg, statusColors.criticalText, "Out of Stock")
        remedy.isLowStock -> Triple(statusColors.lowBg, statusColors.lowText, "Low Stock")
        else -> Triple(statusColors.healthyBg, statusColors.healthyText, "In Stock")
    }
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = bg,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = fg,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
            maxLines = 1,
        )
    }
}

@Composable
fun ExpiryBadge(
    expiryDateMillis: Long?,
    modifier: Modifier = Modifier,
) {
    if (expiryDateMillis == null) return
    val statusColors = LocalStockStatusColors.current
    val status = com.example.data.model.evaluateExpiryStatus(expiryDateMillis)
    val days = daysUntilExpiry(expiryDateMillis)
    val (bg, fg, text) = when (status) {
        ExpiryStatus.EXPIRED -> Triple(
            statusColors.criticalBg,
            statusColors.criticalText,
            "Expired ${formatMonthYear(expiryDateMillis)}",
        )
        ExpiryStatus.EXPIRING_SOON -> Triple(
            statusColors.lowBg,
            statusColors.lowText,
            if (days <= 1) "Expires tomorrow" else "Exp in ${days}d",
        )
        ExpiryStatus.HEALTHY -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            "Exp ${formatMonthYear(expiryDateMillis)}",
        )
        ExpiryStatus.NO_EXPIRY -> return
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = bg,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.Schedule,
                contentDescription = null,
                tint = fg,
                modifier = Modifier.size(12.dp),
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                color = fg,
                maxLines = 1,
            )
        }
    }
}

@Composable
fun RemedyStockCard(
    remedy: RemedyEntity,
    onClick: () -> Unit,
    onQuickDispense: () -> Unit,
    onQuickRestock: () -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
) {
    val border = if (isSelected) {
        BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
    } else {
        BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .testTag("remedy_card_${remedy.id}"),
        shape = MaterialTheme.shapes.medium,
        color = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        tonalElevation = 1.dp,
        border = border,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
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
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text(
                        text = remedy.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    val subtitleParts = buildList {
                        if (remedy.commonName.isNotBlank()) add(remedy.commonName)
                        add(remedy.remedyForm.label)
                        if (remedy.bottleSize.isNotBlank()) add(remedy.bottleSize)
                    }
                    Text(
                        text = subtitleParts.joinToString(" · "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                PotencyBadge(potency = remedy.potency)
            }

            // Metadata & status badges row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                StockStatusBadge(remedy = remedy)
                ExpiryBadge(expiryDateMillis = remedy.expiryDateMillis)
                if (remedy.brand.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.surfaceContainer,
                    ) {
                        Text(
                            text = remedy.brand,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            // Footer: Shelf location + Quick Stepper
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    if (remedy.shelfLocation.isNotBlank()) {
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(14.dp),
                        )
                        Text(
                            text = remedy.shelfLocation,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.Inventory2,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp),
                        )
                        Text(
                            text = "Reorder at ≤ ${remedy.minStockLevel}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                // Quick - / qty / + controls with 48dp accessible touch targets
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    IconButton(
                        onClick = onQuickDispense,
                        enabled = remedy.quantity > 0,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .size(48.dp)
                            .testTag("dispense_btn_${remedy.id}"),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(
                                    if (remedy.quantity > 0) {
                                        MaterialTheme.colorScheme.surfaceContainerHigh
                                    } else {
                                        MaterialTheme.colorScheme.surfaceContainer
                                    }
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Remove,
                                contentDescription = stringResource(R.string.cd_decrease_stock),
                                tint = if (remedy.quantity > 0) {
                                    MaterialTheme.colorScheme.onSurface
                                } else {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                                },
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }

                    Text(
                        text = remedy.quantity.toString(),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = MonoFontFamily,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 6.dp),
                    )

                    IconButton(
                        onClick = onQuickRestock,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .size(48.dp)
                            .testTag("restock_btn_${remedy.id}"),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = stringResource(R.string.cd_increase_stock),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdjustStockDialog(
    remedy: RemedyEntity,
    initialIsRestock: Boolean = true,
    onDismiss: () -> Unit,
    onConfirm: (delta: Int, reason: MovementReason, note: String, newBatch: String?, newExpiryMillis: Long?) -> Unit,
) {
    var selectedReason by remember {
        mutableStateOf(
            if (initialIsRestock) MovementReason.RESTOCK else MovementReason.DISPENSED
        )
    }
    var amountText by remember { mutableStateOf("1") }
    var noteText by remember { mutableStateOf("") }
    var batchText by remember { mutableStateOf(remedy.batchNumber) }
    var expiryMillis by remember { mutableStateOf(remedy.expiryDateMillis) }
    var showDatePicker by remember { mutableStateOf(false) }

    val parsedAmount = amountText.toIntOrNull()?.coerceAtLeast(1) ?: 1
    val isAddition = when (selectedReason) {
        MovementReason.RESTOCK, MovementReason.INITIAL_STOCK -> true
        MovementReason.DISPENSED, MovementReason.EXPIRED_DISPOSED -> false
        MovementReason.AUDIT_ADJUSTMENT -> initialIsRestock
    }
    var auditDirectionAdd by remember { mutableStateOf(initialIsRestock) }
    val effectiveIsAddition = if (selectedReason == MovementReason.AUDIT_ADJUSTMENT) {
        auditDirectionAdd
    } else {
        isAddition
    }
    val delta = if (effectiveIsAddition) parsedAmount else -parsedAmount
    val previewQty = (remedy.quantity + delta).coerceAtLeast(0)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "${remedy.name} ${remedy.potency}",
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    text = "Current stock: ${remedy.quantity} → After: $previewQty",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Movement Reason",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    val reasons = listOf(
                        MovementReason.RESTOCK,
                        MovementReason.DISPENSED,
                        MovementReason.EXPIRED_DISPOSED,
                    )
                    reasons.forEach { reason ->
                        FilterChip(
                            selected = selectedReason == reason,
                            onClick = { selectedReason = reason },
                            label = {
                                Text(
                                    text = when (reason) {
                                        MovementReason.RESTOCK -> "Restock"
                                        MovementReason.DISPENSED -> "Dispense"
                                        MovementReason.EXPIRED_DISPOSED -> "Dispose"
                                        else -> reason.label
                                    },
                                    maxLines = 1,
                                )
                            },
                            modifier = Modifier.testTag("reason_chip_${reason.name}"),
                        )
                    }
                }

                if (selectedReason == MovementReason.AUDIT_ADJUSTMENT) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = auditDirectionAdd,
                            onClick = { auditDirectionAdd = true },
                            label = { Text("Add (+)") },
                        )
                        FilterChip(
                            selected = !auditDirectionAdd,
                            onClick = { auditDirectionAdd = false },
                            label = { Text("Subtract (-)") },
                        )
                    }
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() } && input.length <= 4) {
                            amountText = input
                        }
                    },
                    label = { Text("Units / Bottles") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("adjust_quantity_input"),
                )

                if (effectiveIsAddition) {
                    OutlinedTextField(
                        value = batchText,
                        onValueChange = { batchText = it },
                        label = { Text("Batch / Lot (optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CalendarMonth,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = expiryMillis?.let { "Expiry: ${formatFullDate(it)}" }
                                ?: "Set Expiry Date (optional)",
                        )
                    }
                }

                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Note (optional)") },
                    placeholder = { Text("e.g., Clinic prescription or supplier delivery") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val actualDelta = previewQty - remedy.quantity
                    onConfirm(
                        actualDelta,
                        selectedReason,
                        noteText,
                        if (effectiveIsAddition && batchText != remedy.batchNumber) batchText else null,
                        if (effectiveIsAddition && expiryMillis != remedy.expiryDateMillis) expiryMillis else null,
                    )
                },
                modifier = Modifier.testTag("confirm_adjust_button"),
            ) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    )

    if (showDatePicker) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = expiryMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        expiryMillis = pickerState.selectedDateMillis
                        showDatePicker = false
                    },
                ) { Text("Set Date") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        ) {
            DatePicker(state = pickerState)
        }
    }
}
