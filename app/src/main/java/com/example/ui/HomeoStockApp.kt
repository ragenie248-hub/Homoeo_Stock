package com.example.ui

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.R
import com.example.data.local.RemedyEntity
import com.example.ui.components.RemedyFormSheet
import com.example.ui.screens.AlertsScreen
import com.example.ui.screens.InventoryScreen
import com.example.ui.screens.LedgerScreen
import com.example.ui.screens.RemedyDetailScreen
import com.example.ui.state.StockViewModel

object HomeoStockRoutes {
    const val INVENTORY = "inventory"
    const val ALERTS = "alerts"
    const val LEDGER = "ledger"
    const val DETAIL = "remedy/{remedyId}"

    fun detail(remedyId: Long): String = "remedy/$remedyId"
}

private data class TopLevelDestination(
    val route: String,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String,
)

private val topLevelDestinations = listOf(
    TopLevelDestination(
        route = HomeoStockRoutes.INVENTORY,
        labelRes = R.string.nav_inventory,
        selectedIcon = Icons.Filled.Inventory2,
        unselectedIcon = Icons.Outlined.Inventory2,
        testTag = "nav_inventory",
    ),
    TopLevelDestination(
        route = HomeoStockRoutes.ALERTS,
        labelRes = R.string.nav_alerts,
        selectedIcon = Icons.Filled.NotificationsActive,
        unselectedIcon = Icons.Outlined.NotificationsActive,
        testTag = "nav_alerts",
    ),
    TopLevelDestination(
        route = HomeoStockRoutes.LEDGER,
        labelRes = R.string.nav_ledger,
        selectedIcon = Icons.Filled.History,
        unselectedIcon = Icons.Outlined.History,
        testTag = "nav_ledger",
    ),
)

@Composable
fun HomeoStockApp(
    viewModel: StockViewModel = viewModel(factory = StockViewModel.Factory),
) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }

    val inventoryState by viewModel.inventoryState.collectAsStateWithLifecycle()
    val allMovements by viewModel.allMovements.collectAsStateWithLifecycle()

    var showFormSheet by remember { mutableStateOf(false) }
    var editingRemedy by remember { mutableStateOf<RemedyEntity?>(null) }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val isDetailRoute = currentDestination?.route?.startsWith("remedy/") == true
    val alertBadgeCount = inventoryState.summary.totalAlerts

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavRail = maxWidth >= 600.dp

        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                if (!useNavRail && !isDetailRoute) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ) {
                        topLevelDestinations.forEach { dest ->
                            val selected = currentDestination?.hierarchy?.any {
                                it.route == dest.route
                            } == true
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    navController.navigate(dest.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = {
                                    NavTabIcon(
                                        dest = dest,
                                        selected = selected,
                                        alertCount = if (dest.route == HomeoStockRoutes.ALERTS) alertBadgeCount else 0,
                                    )
                                },
                                label = { Text(stringResource(dest.labelRes)) },
                                modifier = Modifier.testTag(dest.testTag),
                            )
                        }
                    }
                }
            },
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                if (useNavRail && !isDetailRoute) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxHeight(),
                    ) {
                        topLevelDestinations.forEach { dest ->
                            val selected = currentDestination?.hierarchy?.any {
                                it.route == dest.route
                            } == true
                            NavigationRailItem(
                                selected = selected,
                                onClick = {
                                    navController.navigate(dest.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = {
                                    NavTabIcon(
                                        dest = dest,
                                        selected = selected,
                                        alertCount = if (dest.route == HomeoStockRoutes.ALERTS) alertBadgeCount else 0,
                                    )
                                },
                                label = { Text(stringResource(dest.labelRes)) },
                                modifier = Modifier.testTag(dest.testTag),
                            )
                        }
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = HomeoStockRoutes.INVENTORY,
                    modifier = Modifier.weight(1f).fillMaxSize(),
                ) {
                    composable(HomeoStockRoutes.INVENTORY) {
                        InventoryScreen(
                            state = inventoryState,
                            allMovements = allMovements,
                            onSearchQueryChange = viewModel::onSearchQueryChanged,
                            onStockFilterSelected = viewModel::onStockFilterSelected,
                            onFormFilterToggled = viewModel::onFormFilterToggled,
                            onPotencyFilterToggled = viewModel::onPotencyFilterToggled,
                            onSortSelected = viewModel::onSortSelected,
                            onClearFilters = viewModel::clearAllFilters,
                            onQuickDispense = { remedy -> viewModel.quickAdjustStock(remedy, -1) },
                            onQuickRestock = { remedy -> viewModel.quickAdjustStock(remedy, +1) },
                            onRemedyClick = { remedy ->
                                navController.navigate(HomeoStockRoutes.detail(remedy.id))
                            },
                            onAddRemedyClick = {
                                editingRemedy = null
                                showFormSheet = true
                            },
                            onEditRemedyClick = { remedy ->
                                editingRemedy = remedy
                                showFormSheet = true
                            },
                            onDeleteRemedy = viewModel::deleteRemedy,
                            onAdjustStockDetailed = viewModel::adjustStockDetailed,
                            onLoadStarterKit = viewModel::loadStarterHomeKit,
                            onExportCsv = {
                                shareText(
                                    context = context,
                                    subject = "HomeoStock Inventory CSV",
                                    body = viewModel.buildInventoryCsvText(),
                                )
                            },
                        )
                    }

                    composable(HomeoStockRoutes.ALERTS) {
                        AlertsScreen(
                            remedies = inventoryState.allRemedies,
                            onRemedyClick = { remedy ->
                                navController.navigate(HomeoStockRoutes.detail(remedy.id))
                            },
                            onAdjustStock = viewModel::adjustStockDetailed,
                            onShareReorderList = {
                                shareText(
                                    context = context,
                                    subject = "HomeoStock Reorder Checklist",
                                    body = viewModel.buildReorderSummaryText(),
                                )
                            },
                        )
                    }

                    composable(HomeoStockRoutes.LEDGER) {
                        LedgerScreen(
                            movements = allMovements,
                            onExportCsv = {
                                shareText(
                                    context = context,
                                    subject = "HomeoStock Inventory CSV",
                                    body = viewModel.buildInventoryCsvText(),
                                )
                            },
                        )
                    }

                    composable(
                        route = HomeoStockRoutes.DETAIL,
                        arguments = listOf(navArgument("remedyId") { type = NavType.LongType }),
                    ) { backStackEntry ->
                        val remedyId = backStackEntry.arguments?.getLong("remedyId") ?: 0L
                        val remedy by viewModel.observeRemedy(remedyId)
                            .collectAsStateWithLifecycle(initialValue = null)
                        val movements by viewModel.observeMovementsForRemedy(remedyId)
                            .collectAsStateWithLifecycle(initialValue = emptyList())

                        RemedyDetailScreen(
                            remedy = remedy,
                            movements = movements,
                            onBack = { navController.popBackStack() },
                            onEdit = { target ->
                                editingRemedy = target
                                showFormSheet = true
                            },
                            onDelete = viewModel::deleteRemedy,
                            onAdjustStock = viewModel::adjustStockDetailed,
                        )
                    }
                }
            }
        }
    }

    if (showFormSheet) {
        RemedyFormSheet(
            existingRemedy = editingRemedy,
            onDismiss = { showFormSheet = false },
            onSave = { remedy ->
                viewModel.saveRemedy(remedy)
                showFormSheet = false
            },
        )
    }
}

@Composable
private fun NavTabIcon(
    dest: TopLevelDestination,
    selected: Boolean,
    alertCount: Int,
) {
    val icon = if (selected) dest.selectedIcon else dest.unselectedIcon
    if (alertCount > 0) {
        BadgedBox(
            badge = {
                Badge {
                    Text(if (alertCount > 99) "99+" else alertCount.toString())
                }
            },
        ) {
            Icon(imageVector = icon, contentDescription = stringResource(dest.labelRes))
        }
    } else {
        Icon(imageVector = icon, contentDescription = stringResource(dest.labelRes))
    }
}

private fun shareText(context: Context, subject: String, body: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, body)
    }
    context.startActivity(Intent.createChooser(intent, subject))
}
