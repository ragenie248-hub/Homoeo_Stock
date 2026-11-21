package com.example.ui.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.local.HomeoStockDatabase
import com.example.data.local.RemedyEntity
import com.example.data.local.StockMovementEntity
import com.example.data.model.ExpiryStatus
import com.example.data.model.MovementReason
import com.example.data.model.RemedyForm
import com.example.data.model.SortOption
import com.example.data.model.StockFilter
import com.example.data.model.formatFullDate
import com.example.data.repository.StockRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DispensarySummary(
    val totalRemedies: Int = 0,
    val totalUnits: Int = 0,
    val lowStockCount: Int = 0,
    val outOfStockCount: Int = 0,
    val expiringSoonCount: Int = 0,
    val expiredCount: Int = 0,
) {
    val totalAlerts: Int
        get() = lowStockCount + outOfStockCount + expiringSoonCount + expiredCount
}

data class InventoryUiState(
    val isLoading: Boolean = true,
    val allRemedies: List<RemedyEntity> = emptyList(),
    val filteredRemedies: List<RemedyEntity> = emptyList(),
    val summary: DispensarySummary = DispensarySummary(),
    val searchQuery: String = "",
    val stockFilter: StockFilter = StockFilter.ALL,
    val selectedForm: RemedyForm? = null,
    val selectedPotency: String? = null,
    val sortOption: SortOption = SortOption.NAME_ASC,
    val availablePotencies: List<String> = emptyList(),
)

private data class FilterCriteria(
    val query: String = "",
    val stockFilter: StockFilter = StockFilter.ALL,
    val selectedForm: RemedyForm? = null,
    val selectedPotency: String? = null,
    val sortOption: SortOption = SortOption.NAME_ASC,
)

class StockViewModel(
    private val repository: StockRepository,
) : ViewModel() {

    private val filterCriteria = MutableStateFlow(FilterCriteria())
    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val events: SharedFlow<String> = _events.asSharedFlow()

    val inventoryState: StateFlow<InventoryUiState> = combine(
        repository.observeAllRemedies(),
        filterCriteria,
    ) { remedies, criteria ->
        val now = System.currentTimeMillis()
        val query = criteria.query.trim().lowercase()

        val filtered = remedies
            .filter { remedy ->
                val matchesQuery = query.isEmpty() ||
                    remedy.name.lowercase().contains(query) ||
                    remedy.commonName.lowercase().contains(query) ||
                    remedy.potency.lowercase().contains(query) ||
                    remedy.brand.lowercase().contains(query) ||
                    remedy.shelfLocation.lowercase().contains(query) ||
                    remedy.batchNumber.lowercase().contains(query)

                val expStatus = remedy.expiryStatus(now)
                val matchesStockFilter = when (criteria.stockFilter) {
                    StockFilter.ALL -> true
                    StockFilter.LOW_STOCK -> remedy.isLowStock
                    StockFilter.OUT_OF_STOCK -> remedy.isOutOfStock
                    StockFilter.EXPIRING_SOON -> expStatus == ExpiryStatus.EXPIRING_SOON
                    StockFilter.EXPIRED -> expStatus == ExpiryStatus.EXPIRED
                }

                val matchesForm = criteria.selectedForm == null ||
                    remedy.remedyForm == criteria.selectedForm

                val matchesPotency = criteria.selectedPotency == null ||
                    remedy.potency.equals(criteria.selectedPotency, ignoreCase = true)

                matchesQuery && matchesStockFilter && matchesForm && matchesPotency
            }
            .let { list ->
                when (criteria.sortOption) {
                    SortOption.NAME_ASC -> list.sortedWith(
                        compareBy<RemedyEntity> { it.name.lowercase() }.thenBy { it.potency }
                    )
                    SortOption.STOCK_LOW_FIRST -> list.sortedWith(
                        compareBy<RemedyEntity> { it.quantity }.thenBy { it.name.lowercase() }
                    )
                    SortOption.EXPIRY_SOONEST -> list.sortedWith(
                        compareBy<RemedyEntity> { it.expiryDateMillis ?: Long.MAX_VALUE }
                            .thenBy { it.name.lowercase() }
                    )
                    SortOption.RECENTLY_UPDATED -> list.sortedByDescending { it.updatedAt }
                }
            }

        val summary = DispensarySummary(
            totalRemedies = remedies.size,
            totalUnits = remedies.sumOf { it.quantity },
            lowStockCount = remedies.count { it.isLowStock },
            outOfStockCount = remedies.count { it.isOutOfStock },
            expiringSoonCount = remedies.count { it.expiryStatus(now) == ExpiryStatus.EXPIRING_SOON },
            expiredCount = remedies.count { it.expiryStatus(now) == ExpiryStatus.EXPIRED },
        )

        val potencies = remedies.map { it.potency }.filter { it.isNotBlank() }.distinct().sorted()

        InventoryUiState(
            isLoading = false,
            allRemedies = remedies,
            filteredRemedies = filtered,
            summary = summary,
            searchQuery = criteria.query,
            stockFilter = criteria.stockFilter,
            selectedForm = criteria.selectedForm,
            selectedPotency = criteria.selectedPotency,
            sortOption = criteria.sortOption,
            availablePotencies = potencies,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = InventoryUiState(isLoading = true),
    )

    val allMovements: StateFlow<List<StockMovementEntity>> = repository.observeAllMovements()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList(),
        )

    fun observeRemedy(remedyId: Long): Flow<RemedyEntity?> = repository.observeRemedyById(remedyId)

    fun observeMovementsForRemedy(remedyId: Long): Flow<List<StockMovementEntity>> =
        repository.observeMovementsForRemedy(remedyId)

    fun onSearchQueryChanged(query: String) {
        filterCriteria.update { it.copy(query = query) }
    }

    fun onStockFilterSelected(filter: StockFilter) {
        filterCriteria.update { it.copy(stockFilter = filter) }
    }

    fun onFormFilterToggled(form: RemedyForm?) {
        filterCriteria.update { current ->
            current.copy(selectedForm = if (current.selectedForm == form) null else form)
        }
    }

    fun onPotencyFilterToggled(potency: String?) {
        filterCriteria.update { current ->
            current.copy(selectedPotency = if (current.selectedPotency == potency) null else potency)
        }
    }

    fun onSortSelected(sortOption: SortOption) {
        filterCriteria.update { it.copy(sortOption = sortOption) }
    }

    fun clearAllFilters() {
        filterCriteria.value = FilterCriteria()
    }

    fun saveRemedy(remedy: RemedyEntity) {
        viewModelScope.launch {
            if (remedy.id == 0L) {
                repository.addRemedy(remedy)
                _events.tryEmit("Added ${remedy.name} ${remedy.potency}")
            } else {
                repository.updateRemedy(remedy)
                _events.tryEmit("Updated ${remedy.name} ${remedy.potency}")
            }
        }
    }

    fun quickAdjustStock(remedy: RemedyEntity, delta: Int) {
        if (delta < 0 && remedy.quantity <= 0) {
            _events.tryEmit("${remedy.name} ${remedy.potency} is already out of stock")
            return
        }
        viewModelScope.launch {
            val reason = if (delta >= 0) MovementReason.RESTOCK else MovementReason.DISPENSED
            val note = if (delta >= 0) "Quick restock (+$delta)" else "Quick dispense ($delta)"
            repository.adjustStock(
                remedyId = remedy.id,
                delta = delta,
                reason = reason,
                note = note,
            )
        }
    }

    fun adjustStockDetailed(
        remedy: RemedyEntity,
        delta: Int,
        reason: MovementReason,
        note: String,
        newBatch: String? = null,
        newExpiryMillis: Long? = null,
    ) {
        viewModelScope.launch {
            val ok = repository.adjustStock(
                remedyId = remedy.id,
                delta = delta,
                reason = reason,
                note = note,
                newBatch = newBatch,
                newExpiryMillis = newExpiryMillis,
            )
            if (ok) {
                val sign = if (delta > 0) "+$delta" else "$delta"
                _events.tryEmit("${remedy.name} ${remedy.potency}: $sign (${reason.label})")
            }
        }
    }

    fun deleteRemedy(remedy: RemedyEntity) {
        viewModelScope.launch {
            repository.deleteRemedy(remedy.id)
            _events.tryEmit("Removed ${remedy.name} ${remedy.potency}")
        }
    }

    fun loadStarterHomeKit() {
        viewModelScope.launch {
            val count = repository.loadStarterHomeKit()
            _events.tryEmit("Loaded $count starter remedies into inventory")
        }
    }

    fun buildReorderSummaryText(): String {
        val remedies = inventoryState.value.allRemedies
        val now = System.currentTimeMillis()
        val reorder = remedies.filter { it.needsReorder }
        val expiring = remedies.filter {
            val status = it.expiryStatus(now)
            status == ExpiryStatus.EXPIRED || status == ExpiryStatus.EXPIRING_SOON
        }
        return buildString {
            appendLine("HomeoStock — Reorder & Expiry Checklist")
            appendLine("Generated: ${formatFullDate(now)}")
            appendLine()
            if (reorder.isNotEmpty()) {
                appendLine("RESTOCK NEEDED (${reorder.size}):")
                reorder.forEach { r ->
                    val status = if (r.isOutOfStock) "OUT OF STOCK" else "Low (${r.quantity}/${r.minStockLevel})"
                    val brand = if (r.brand.isNotBlank()) " · ${r.brand}" else ""
                    appendLine("- ${r.name} ${r.potency} (${r.remedyForm.label}, ${r.bottleSize}$brand) — $status")
                }
                appendLine()
            }
            if (expiring.isNotEmpty()) {
                appendLine("EXPIRY ATTENTION (${expiring.size}):")
                expiring.forEach { r ->
                    val expDate = r.expiryDateMillis?.let { formatFullDate(it) } ?: "N/A"
                    val label = if (r.expiryStatus(now) == ExpiryStatus.EXPIRED) "EXPIRED" else "Expiring"
                    appendLine("- ${r.name} ${r.potency} (${r.bottleSize}, Batch: ${r.batchNumber.ifBlank { "—" }}) — $label ($expDate)")
                }
            }
            if (reorder.isEmpty() && expiring.isEmpty()) {
                appendLine("All remedies are well-stocked and within expiry dates.")
            }
        }
    }

    fun buildInventoryCsvText(): String {
        val remedies = inventoryState.value.allRemedies
        return buildString {
            appendLine("Name,CommonName,Potency,Form,Brand,BottleSize,Quantity,MinStock,Batch,ExpiryDate,ShelfLocation,Notes")
            remedies.forEach { r ->
                val exp = r.expiryDateMillis?.let { formatFullDate(it) } ?: ""
                val cells = listOf(
                    r.name,
                    r.commonName,
                    r.potency,
                    r.remedyForm.label,
                    r.brand,
                    r.bottleSize,
                    r.quantity.toString(),
                    r.minStockLevel.toString(),
                    r.batchNumber,
                    exp,
                    r.shelfLocation,
                    r.notes,
                ).map { cell -> "\"${cell.replace("\"", "\"\"")}\"" }
                appendLine(cells.joinToString(","))
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                val db = HomeoStockDatabase.getInstance(app)
                StockViewModel(StockRepository(db.remedyDao(), db.stockMovementDao()))
            }
        }
    }
}
