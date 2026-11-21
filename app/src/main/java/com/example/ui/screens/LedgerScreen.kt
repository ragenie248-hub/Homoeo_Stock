package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.example.data.local.StockMovementEntity
import com.example.data.model.MovementReason
import com.example.data.model.formatTimestamp
import com.example.ui.components.PotencyBadge
import com.example.ui.theme.LocalStockStatusColors
import com.example.ui.theme.MonoFontFamily

private enum class LedgerFilter(val label: String) {
    ALL("All"),
    RESTOCKS("Restocked"),
    DISPENSED("Dispensed"),
    OTHER("Disposed / Audit"),
}

@Composable
fun LedgerScreen(
    movements: List<StockMovementEntity>,
    onExportCsv: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedFilter by remember { mutableStateOf(LedgerFilter.ALL) }

    val filtered = remember(movements, selectedFilter) {
        when (selectedFilter) {
            LedgerFilter.ALL -> movements
            LedgerFilter.RESTOCKS -> movements.filter {
                it.movementReason == MovementReason.RESTOCK ||
                    it.movementReason == MovementReason.INITIAL_STOCK
            }
            LedgerFilter.DISPENSED -> movements.filter {
                it.movementReason == MovementReason.DISPENSED
            }
            LedgerFilter.OTHER -> movements.filter {
                it.movementReason == MovementReason.EXPIRED_DISPOSED ||
                    it.movementReason == MovementReason.AUDIT_ADJUSTMENT
            }
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 680.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(key = "ledger_header") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.ledger_title),
                            style = MaterialTheme.typography.headlineMedium,
                        )
                        Text(
                            text = stringResource(R.string.ledger_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (movements.isNotEmpty()) {
                        OutlinedButton(
                            onClick = onExportCsv,
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("export_csv_button"),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Share,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Export CSV")
                        }
                    }
                }
            }

            if (movements.isNotEmpty()) {
                item(key = "ledger_filters") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        LedgerFilter.entries.forEach { filter ->
                            FilterChip(
                                selected = selectedFilter == filter,
                                onClick = { selectedFilter = filter },
                                label = { Text(filter.label) },
                            )
                        }
                    }
                }
            }

            if (filtered.isEmpty()) {
                item(key = "ledger_empty") {
                    Surface(
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.History,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(40.dp),
                            )
                            Text(
                                text = stringResource(R.string.ledger_empty_title),
                                style = MaterialTheme.typography.titleLarge,
                            )
                            Text(
                                text = stringResource(R.string.ledger_empty_desc),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            } else {
                items(filtered, key = { it.id }) { movement ->
                    MovementCard(movement = movement)
                }
            }
        }
    }
}

@Composable
fun MovementCard(
    movement: StockMovementEntity,
    modifier: Modifier = Modifier,
) {
    val statusColors = LocalStockStatusColors.current
    val isPositive = movement.delta >= 0
    val deltaColor = if (isPositive) statusColors.healthyText else statusColors.criticalText
    val deltaBg = if (isPositive) statusColors.healthyBg else statusColors.criticalBg
    val deltaText = if (movement.delta > 0) "+${movement.delta}" else movement.delta.toString()

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = deltaBg,
            ) {
                Text(
                    text = deltaText,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = MonoFontFamily,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = deltaColor,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = movement.remedyName,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    PotencyBadge(potency = movement.potency)
                }

                Text(
                    text = buildString {
                        append(movement.movementReason.label)
                        append(" · New qty: ${movement.resultingQuantity}")
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                if (movement.note.isNotBlank()) {
                    Text(
                        text = movement.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                Text(
                    text = formatTimestamp(movement.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                )
            }
        }
    }
}
