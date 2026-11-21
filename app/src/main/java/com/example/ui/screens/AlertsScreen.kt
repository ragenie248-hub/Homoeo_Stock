package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.RemedyEntity
import com.example.data.model.ExpiryStatus
import com.example.data.model.MovementReason
import com.example.data.model.daysUntilExpiry
import com.example.data.model.formatFullDate
import com.example.ui.components.AdjustStockDialog
import com.example.ui.components.PotencyBadge
import com.example.ui.theme.LocalStockStatusColors
import com.example.ui.theme.MonoFontFamily

@Composable
fun AlertsScreen(
    remedies: List<RemedyEntity>,
    onRemedyClick: (RemedyEntity) -> Unit,
    onAdjustStock: (RemedyEntity, Int, MovementReason, String, String?, Long?) -> Unit,
    onShareReorderList: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val now = remember(remedies) { System.currentTimeMillis() }
    val outOfStock = remember(remedies) { remedies.filter { it.isOutOfStock } }
    val lowStock = remember(remedies) { remedies.filter { it.isLowStock } }
    val expired = remember(remedies, now) {
        remedies.filter { it.expiryStatus(now) == ExpiryStatus.EXPIRED }
    }
    val expiringSoon = remember(remedies, now) {
        remedies.filter { it.expiryStatus(now) == ExpiryStatus.EXPIRING_SOON }
    }

    var restockTarget by remember { mutableStateOf<RemedyEntity?>(null) }
    var restockInitialAdd by remember { mutableStateOf(true) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .backgroundColorSafe(),
        contentAlignment = Alignment.TopCenter,
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 680.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(key = "alerts_header") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.alerts_title),
                            style = MaterialTheme.typography.headlineMedium,
                        )
                        Text(
                            text = stringResource(R.string.alerts_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (outOfStock.isNotEmpty() || lowStock.isNotEmpty() || expired.isNotEmpty() || expiringSoon.isNotEmpty()) {
                        OutlinedButton(
                            onClick = onShareReorderList,
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("share_reorder_button"),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Share,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Share List")
                        }
                    }
                }
            }

            if (outOfStock.isEmpty() && lowStock.isEmpty() && expired.isEmpty() && expiringSoon.isEmpty()) {
                item(key = "alerts_clear") {
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
                                imageVector = Icons.Outlined.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(44.dp),
                            )
                            Text(
                                text = stringResource(R.string.alerts_all_clear_title),
                                style = MaterialTheme.typography.titleLarge,
                            )
                            Text(
                                text = stringResource(R.string.alerts_all_clear_desc),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            } else {
                if (outOfStock.isNotEmpty()) {
                    item(key = "hdr_out_of_stock") {
                        AlertSectionHeader(
                            title = "Out of Stock (${outOfStock.size})",
                            icon = Icons.Outlined.ErrorOutline,
                            accent = LocalStockStatusColors.current.criticalText,
                        )
                    }
                    items(outOfStock, key = { "oos_${it.id}" }) { remedy ->
                        AlertRemedyCard(
                            remedy = remedy,
                            statusLabel = "0 left · Reorder threshold ${remedy.minStockLevel}",
                            statusBg = LocalStockStatusColors.current.criticalBg,
                            statusFg = LocalStockStatusColors.current.criticalText,
                            actionLabel = "Restock",
                            actionIcon = Icons.Filled.Add,
                            onCardClick = { onRemedyClick(remedy) },
                            onActionClick = {
                                restockTarget = remedy
                                restockInitialAdd = true
                            },
                        )
                    }
                }

                if (lowStock.isNotEmpty()) {
                    item(key = "hdr_low_stock") {
                        AlertSectionHeader(
                            title = "Low Stock (${lowStock.size})",
                            icon = Icons.Outlined.WarningAmber,
                            accent = LocalStockStatusColors.current.lowText,
                        )
                    }
                    items(lowStock, key = { "low_${it.id}" }) { remedy ->
                        AlertRemedyCard(
                            remedy = remedy,
                            statusLabel = "${remedy.quantity} left (threshold ≤ ${remedy.minStockLevel})",
                            statusBg = LocalStockStatusColors.current.lowBg,
                            statusFg = LocalStockStatusColors.current.lowText,
                            actionLabel = "Restock",
                            actionIcon = Icons.Filled.Add,
                            onCardClick = { onRemedyClick(remedy) },
                            onActionClick = {
                                restockTarget = remedy
                                restockInitialAdd = true
                            },
                        )
                    }
                }

                if (expired.isNotEmpty()) {
                    item(key = "hdr_expired") {
                        AlertSectionHeader(
                            title = "Expired Batches (${expired.size})",
                            icon = Icons.Outlined.DeleteOutline,
                            accent = LocalStockStatusColors.current.criticalText,
                        )
                    }
                    items(expired, key = { "exp_${it.id}" }) { remedy ->
                        val expStr = remedy.expiryDateMillis?.let { formatFullDate(it) } ?: ""
                        AlertRemedyCard(
                            remedy = remedy,
                            statusLabel = "Expired on $expStr · Qty ${remedy.quantity}",
                            statusBg = LocalStockStatusColors.current.criticalBg,
                            statusFg = LocalStockStatusColors.current.criticalText,
                            actionLabel = "Update / Dispose",
                            actionIcon = Icons.Outlined.Schedule,
                            onCardClick = { onRemedyClick(remedy) },
                            onActionClick = {
                                restockTarget = remedy
                                restockInitialAdd = false
                            },
                        )
                    }
                }

                if (expiringSoon.isNotEmpty()) {
                    item(key = "hdr_expiring_soon") {
                        AlertSectionHeader(
                            title = "Expiring Within 90 Days (${expiringSoon.size})",
                            icon = Icons.Outlined.Schedule,
                            accent = LocalStockStatusColors.current.lowText,
                        )
                    }
                    items(expiringSoon, key = { "soon_${it.id}" }) { remedy ->
                        val days = remedy.expiryDateMillis?.let { daysUntilExpiry(it, now) } ?: 0L
                        val expStr = remedy.expiryDateMillis?.let { formatFullDate(it) } ?: ""
                        AlertRemedyCard(
                            remedy = remedy,
                            statusLabel = "Expires in ${days}d ($expStr) · Qty ${remedy.quantity}",
                            statusBg = LocalStockStatusColors.current.lowBg,
                            statusFg = LocalStockStatusColors.current.lowText,
                            actionLabel = "Restock",
                            actionIcon = Icons.Filled.Add,
                            onCardClick = { onRemedyClick(remedy) },
                            onActionClick = {
                                restockTarget = remedy
                                restockInitialAdd = true
                            },
                        )
                    }
                }
            }
        }
    }

    restockTarget?.let { target ->
        AdjustStockDialog(
            remedy = target,
            initialIsRestock = restockInitialAdd,
            onDismiss = { restockTarget = null },
            onConfirm = { delta, reason, note, newBatch, newExp ->
                onAdjustStock(target, delta, reason, note, newBatch, newExp)
                restockTarget = null
            },
        )
    }
}

@Composable
private fun Modifier.backgroundColorSafe(): Modifier = this

@Composable
private fun AlertSectionHeader(
    title: String,
    icon: ImageVector,
    accent: Color,
) {
    Row(
        modifier = Modifier.padding(top = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accent,
            modifier = Modifier.size(20.dp),
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Composable
private fun AlertRemedyCard(
    remedy: RemedyEntity,
    statusLabel: String,
    statusBg: Color,
    statusFg: Color,
    actionLabel: String,
    actionIcon: ImageVector,
    onCardClick: () -> Unit,
    onActionClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onCardClick),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = remedy.name,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = listOfNotNull(
                            remedy.remedyForm.label,
                            remedy.bottleSize.takeIf { it.isNotBlank() },
                            remedy.brand.takeIf { it.isNotBlank() },
                        ).joinToString(" · "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                PotencyBadge(potency = remedy.potency)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = statusBg,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = statusLabel,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontFamily = MonoFontFamily,
                            fontWeight = FontWeight.Medium,
                        ),
                        color = statusFg,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    )
                }
                Spacer(Modifier.width(10.dp))
                Button(
                    onClick = onActionClick,
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                    ),
                    modifier = Modifier.height(48.dp),
                ) {
                    Icon(actionIcon, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(actionLabel)
                }
            }
        }
    }
}
