package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.RemedyEntity
import com.example.data.local.StockMovementEntity
import com.example.data.model.MovementReason
import com.example.data.model.RemedyForm
import com.example.data.model.SortOption
import com.example.data.model.StockFilter
import com.example.ui.components.RemedyStockCard
import com.example.ui.state.DispensarySummary
import com.example.ui.state.InventoryUiState
import com.example.ui.theme.LocalStockStatusColors
import com.example.ui.theme.MonoFontFamily

@Composable
fun InventoryScreen(
    state: InventoryUiState,
    allMovements: List<StockMovementEntity>,
    onSearchQueryChange: (String) -> Unit,
    onStockFilterSelected: (StockFilter) -> Unit,
    onFormFilterToggled: (RemedyForm?) -> Unit,
    onPotencyFilterToggled: (String?) -> Unit,
    onSortSelected: (SortOption) -> Unit,
    onClearFilters: () -> Unit,
    onQuickDispense: (RemedyEntity) -> Unit,
    onQuickRestock: (RemedyEntity) -> Unit,
    onRemedyClick: (RemedyEntity) -> Unit,
    onAddRemedyClick: () -> Unit,
    onEditRemedyClick: (RemedyEntity) -> Unit,
    onDeleteRemedy: (RemedyEntity) -> Unit,
    onAdjustStockDetailed: (RemedyEntity, Int, MovementReason, String, String?, Long?) -> Unit,
    onLoadStarterKit: () -> Unit,
    onExportCsv: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedDetailId by remember { mutableStateOf<Long?>(null) }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isExpandedWidth = maxWidth >= 840.dp
        val selectedRemedy = remember(selectedDetailId, state.allRemedies) {
            state.allRemedies.firstOrNull { it.id == selectedDetailId }
                ?: state.filteredRemedies.firstOrNull()
        }

        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = WindowInsets(0),
            floatingActionButton = {
                FloatingActionButton(
                    onClick = onAddRemedyClick,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("add_remedy_fab"),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = stringResource(R.string.add_remedy),
                    )
                }
            },
        ) { innerPadding ->
            if (isExpandedWidth && state.allRemedies.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    InventoryListPane(
                        state = state,
                        selectedRemedyId = selectedRemedy?.id,
                        onSearchQueryChange = onSearchQueryChange,
                        onStockFilterSelected = onStockFilterSelected,
                        onFormFilterToggled = onFormFilterToggled,
                        onPotencyFilterToggled = onPotencyFilterToggled,
                        onSortSelected = onSortSelected,
                        onClearFilters = onClearFilters,
                        onQuickDispense = onQuickDispense,
                        onQuickRestock = onQuickRestock,
                        onRemedyClick = { remedy -> selectedDetailId = remedy.id },
                        onAddRemedyClick = onAddRemedyClick,
                        onLoadStarterKit = onLoadStarterKit,
                        onExportCsv = onExportCsv,
                        modifier = Modifier
                            .weight(1.1f)
                            .fillMaxHeight(),
                    )

                    Surface(
                        modifier = Modifier
                            .weight(0.9f)
                            .fillMaxHeight()
                            .padding(vertical = 12.dp),
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    ) {
                        if (selectedRemedy != null) {
                            val remedyMovements = remember(selectedRemedy.id, allMovements) {
                                allMovements.filter { it.remedyId == selectedRemedy.id }
                            }
                            RemedyDetailContent(
                                remedy = selectedRemedy,
                                movements = remedyMovements,
                                onEdit = { onEditRemedyClick(selectedRemedy) },
                                onDelete = { onDeleteRemedy(selectedRemedy) },
                                onAdjustStock = { delta, reason, note, batch, exp ->
                                    onAdjustStockDetailed(selectedRemedy, delta, reason, note, batch, exp)
                                },
                            )
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "Select a remedy to view details",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    InventoryListPane(
                        state = state,
                        selectedRemedyId = null,
                        onSearchQueryChange = onSearchQueryChange,
                        onStockFilterSelected = onStockFilterSelected,
                        onFormFilterToggled = onFormFilterToggled,
                        onPotencyFilterToggled = onPotencyFilterToggled,
                        onSortSelected = onSortSelected,
                        onClearFilters = onClearFilters,
                        onQuickDispense = onQuickDispense,
                        onQuickRestock = onQuickRestock,
                        onRemedyClick = onRemedyClick,
                        onAddRemedyClick = onAddRemedyClick,
                        onLoadStarterKit = onLoadStarterKit,
                        onExportCsv = onExportCsv,
                        modifier = Modifier
                            .widthIn(max = 680.dp)
                            .fillMaxSize(),
                    )
                }
            }
        }
    }
}

@Composable
private fun InventoryListPane(
    state: InventoryUiState,
    selectedRemedyId: Long?,
    onSearchQueryChange: (String) -> Unit,
    onStockFilterSelected: (StockFilter) -> Unit,
    onFormFilterToggled: (RemedyForm?) -> Unit,
    onPotencyFilterToggled: (String?) -> Unit,
    onSortSelected: (SortOption) -> Unit,
    onClearFilters: () -> Unit,
    onQuickDispense: (RemedyEntity) -> Unit,
    onQuickRestock: (RemedyEntity) -> Unit,
    onRemedyClick: (RemedyEntity) -> Unit,
    onAddRemedyClick: () -> Unit,
    onLoadStarterKit: () -> Unit,
    onExportCsv: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showSortMenu by remember { mutableStateOf(false) }
    var showOverflowMenu by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Botanical Hero Header
        item(key = "hero_header") {
            BotanicalHeroHeader(
                summary = state.summary,
                showOverflowMenu = showOverflowMenu,
                onToggleOverflow = { showOverflowMenu = it },
                onKpiClick = onStockFilterSelected,
                onLoadStarterKit = {
                    showOverflowMenu = false
                    onLoadStarterKit()
                },
                onExportCsv = {
                    showOverflowMenu = false
                    onExportCsv()
                },
            )
        }

        if (state.allRemedies.isNotEmpty()) {
            // Search & Sort row
            item(key = "search_row") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedTextField(
                        value = state.searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = {
                            Text(
                                text = stringResource(R.string.search_remedies_hint),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = null,
                            )
                        },
                        trailingIcon = {
                            if (state.searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { onSearchQueryChange("") },
                                    modifier = Modifier.minimumInteractiveComponentSize(),
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Clear,
                                        contentDescription = stringResource(R.string.cd_clear_search),
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("search_remedies_input"),
                    )

                    Box {
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier
                                .size(54.dp)
                                .clip(MaterialTheme.shapes.medium)
                                .clickable { showSortMenu = true }
                                .testTag("sort_menu_button"),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Sort,
                                    contentDescription = stringResource(R.string.sort_by),
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false },
                        ) {
                            SortOption.entries.forEach { option ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = option.label,
                                            fontWeight = if (state.sortOption == option) {
                                                FontWeight.Bold
                                            } else {
                                                FontWeight.Normal
                                            },
                                        )
                                    },
                                    onClick = {
                                        onSortSelected(option)
                                        showSortMenu = false
                                    },
                                )
                            }
                        }
                    }
                }
            }

            // Status filter chips
            item(key = "filter_chips") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        StockFilter.entries.forEach { filter ->
                            val countLabel = when (filter) {
                                StockFilter.ALL -> "${state.summary.totalRemedies}"
                                StockFilter.LOW_STOCK -> "${state.summary.lowStockCount}"
                                StockFilter.OUT_OF_STOCK -> "${state.summary.outOfStockCount}"
                                StockFilter.EXPIRING_SOON -> "${state.summary.expiringSoonCount}"
                                StockFilter.EXPIRED -> "${state.summary.expiredCount}"
                            }
                            FilterChip(
                                selected = state.stockFilter == filter,
                                onClick = { onStockFilterSelected(filter) },
                                label = { Text("${filter.label} ($countLabel)") },
                                modifier = Modifier.testTag("filter_chip_${filter.name}"),
                            )
                        }
                    }

                    // Potency & Form quick chips
                    AnimatedVisibility(visible = state.availablePotencies.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            state.availablePotencies.forEach { potency ->
                                FilterChip(
                                    selected = state.selectedPotency == potency,
                                    onClick = { onPotencyFilterToggled(potency) },
                                    label = {
                                        Text(
                                            text = potency,
                                            fontFamily = MonoFontFamily,
                                        )
                                    },
                                )
                            }
                            RemedyForm.entries.forEach { form ->
                                FilterChip(
                                    selected = state.selectedForm == form,
                                    onClick = { onFormFilterToggled(form) },
                                    label = { Text(form.label) },
                                )
                            }
                        }
                    }
                }
            }
        }

        // Empty or filtered empty states
        if (!state.isLoading && state.allRemedies.isEmpty()) {
            item(key = "empty_inventory") {
                EmptyInventoryCard(
                    onAddRemedyClick = onAddRemedyClick,
                    onLoadStarterKit = onLoadStarterKit,
                )
            }
        } else if (!state.isLoading && state.filteredRemedies.isEmpty()) {
            item(key = "empty_filtered") {
                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Inventory2,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp),
                        )
                        Text(
                            text = "No matching remedies",
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Text(
                            text = "Try adjusting your search term, potency filter, or stock category.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        TextButton(onClick = onClearFilters) {
                            Text("Reset Filters")
                        }
                    }
                }
            }
        } else {
            items(
                items = state.filteredRemedies,
                key = { it.id },
            ) { remedy ->
                RemedyStockCard(
                    remedy = remedy,
                    isSelected = remedy.id == selectedRemedyId,
                    onClick = { onRemedyClick(remedy) },
                    onQuickDispense = { onQuickDispense(remedy) },
                    onQuickRestock = { onQuickRestock(remedy) },
                )
            }
        }
    }
}

@Composable
private fun BotanicalHeroHeader(
    summary: DispensarySummary,
    showOverflowMenu: Boolean,
    onToggleOverflow: (Boolean) -> Unit,
    onKpiClick: (StockFilter) -> Unit,
    onLoadStarterKit: () -> Unit,
    onExportCsv: () -> Unit,
) {
    val statusColors = LocalStockStatusColors.current
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.primaryContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Image(
                painter = painterResource(id = R.drawable.img_apothecary_shelf),
                contentDescription = stringResource(R.string.cd_apothecary_banner),
                contentScale = ContentScale.Crop,
                alpha = 0.22f,
                modifier = Modifier.matchParentSize(),
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.84f),
                            )
                        )
                    ),
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = stringResource(R.string.inventory_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Box {
                        IconButton(
                            onClick = { onToggleOverflow(true) },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("inventory_overflow_button"),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = stringResource(R.string.cd_more_options),
                            )
                        }
                        DropdownMenu(
                            expanded = showOverflowMenu,
                            onDismissRequest = { onToggleOverflow(false) },
                        ) {
                            DropdownMenuItem(
                                leadingIcon = {
                                    Icon(Icons.Outlined.AutoAwesome, contentDescription = null)
                                },
                                text = { Text(stringResource(R.string.load_starter_kit)) },
                                onClick = onLoadStarterKit,
                            )
                            DropdownMenuItem(
                                leadingIcon = {
                                    Icon(Icons.Outlined.Share, contentDescription = null)
                                },
                                text = { Text(stringResource(R.string.export_inventory)) },
                                onClick = onExportCsv,
                            )
                        }
                    }
                }

                // KPI Pills Strip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    KpiStatTile(
                        label = stringResource(R.string.kpi_remedies),
                        value = summary.totalRemedies.toString(),
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        onClick = { onKpiClick(StockFilter.ALL) },
                        modifier = Modifier.weight(1f),
                    )
                    KpiStatTile(
                        label = stringResource(R.string.kpi_bottles),
                        value = summary.totalUnits.toString(),
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                        contentColor = MaterialTheme.colorScheme.primary,
                        onClick = { onKpiClick(StockFilter.ALL) },
                        modifier = Modifier.weight(1f),
                    )
                    val lowTotal = summary.lowStockCount + summary.outOfStockCount
                    KpiStatTile(
                        label = stringResource(R.string.kpi_low_stock),
                        value = lowTotal.toString(),
                        containerColor = if (lowTotal > 0) statusColors.lowBg else MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                        contentColor = if (lowTotal > 0) statusColors.lowText else MaterialTheme.colorScheme.onSurface,
                        onClick = {
                            onKpiClick(
                                if (summary.outOfStockCount > 0 && summary.lowStockCount == 0) {
                                    StockFilter.OUT_OF_STOCK
                                } else {
                                    StockFilter.LOW_STOCK
                                }
                            )
                        },
                        modifier = Modifier.weight(1f),
                    )
                    val expTotal = summary.expiringSoonCount + summary.expiredCount
                    KpiStatTile(
                        label = stringResource(R.string.kpi_expiring),
                        value = expTotal.toString(),
                        containerColor = if (summary.expiredCount > 0) {
                            statusColors.criticalBg
                        } else if (expTotal > 0) {
                            statusColors.lowBg
                        } else {
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
                        },
                        contentColor = if (summary.expiredCount > 0) {
                            statusColors.criticalText
                        } else if (expTotal > 0) {
                            statusColors.lowText
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        onClick = {
                            onKpiClick(
                                if (summary.expiredCount > 0 && summary.expiringSoonCount == 0) {
                                    StockFilter.EXPIRED
                                } else {
                                    StockFilter.EXPIRING_SOON
                                }
                            )
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun KpiStatTile(
    label: String,
    value: String,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.small,
        color = containerColor,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = MonoFontFamily,
                    fontWeight = FontWeight.Bold,
                ),
                color = contentColor,
                maxLines = 1,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor.copy(alpha = 0.85f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun EmptyInventoryCard(
    onAddRemedyClick: () -> Unit,
    onLoadStarterKit: () -> Unit,
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_apothecary_shelf),
                contentDescription = stringResource(R.string.cd_apothecary_banner),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp),
            )
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = stringResource(R.string.empty_inventory_title),
                    style = MaterialTheme.typography.headlineSmall,
                )
                Text(
                    text = stringResource(R.string.empty_inventory_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(4.dp))
                Button(
                    onClick = onAddRemedyClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("empty_add_remedy_button"),
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.add_remedy))
                }
                OutlinedButton(
                    onClick = onLoadStarterKit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("load_starter_kit_button"),
                ) {
                    Icon(Icons.Outlined.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.load_starter_kit))
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}
