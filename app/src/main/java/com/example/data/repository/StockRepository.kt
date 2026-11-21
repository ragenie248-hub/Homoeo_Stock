package com.example.data.repository

import com.example.data.local.RemedyDao
import com.example.data.local.RemedyEntity
import com.example.data.local.StockMovementDao
import com.example.data.local.StockMovementEntity
import com.example.data.model.MovementReason
import com.example.data.model.RemedyForm
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.TimeUnit

class StockRepository(
    private val remedyDao: RemedyDao,
    private val movementDao: StockMovementDao,
) {
    fun observeAllRemedies(): Flow<List<RemedyEntity>> = remedyDao.observeAllRemedies()

    fun observeRemedyById(id: Long): Flow<RemedyEntity?> = remedyDao.observeRemedyById(id)

    fun observeAllMovements(): Flow<List<StockMovementEntity>> = movementDao.observeAllMovements()

    fun observeMovementsForRemedy(remedyId: Long): Flow<List<StockMovementEntity>> =
        movementDao.observeMovementsForRemedy(remedyId)

    suspend fun addRemedy(remedy: RemedyEntity, note: String = "Added to inventory"): Long {
        val normalized = remedy.copy(
            name = remedy.name.trim(),
            commonName = remedy.commonName.trim(),
            potency = remedy.potency.trim().uppercase(),
            brand = remedy.brand.trim(),
            bottleSize = remedy.bottleSize.trim().ifBlank { remedy.remedyForm.defaultSize },
            batchNumber = remedy.batchNumber.trim(),
            shelfLocation = remedy.shelfLocation.trim(),
            notes = remedy.notes.trim(),
            quantity = remedy.quantity.coerceAtLeast(0),
            minStockLevel = remedy.minStockLevel.coerceAtLeast(0),
            updatedAt = System.currentTimeMillis(),
        )
        return remedyDao.insertRemedyWithMovement(
            remedy = normalized,
            movementDao = movementDao,
            reason = MovementReason.INITIAL_STOCK.name,
            note = note,
        )
    }

    suspend fun updateRemedy(updated: RemedyEntity) {
        val existing = remedyDao.getRemedyById(updated.id)
        val now = System.currentTimeMillis()
        val normalized = updated.copy(
            name = updated.name.trim(),
            commonName = updated.commonName.trim(),
            potency = updated.potency.trim().uppercase(),
            brand = updated.brand.trim(),
            bottleSize = updated.bottleSize.trim().ifBlank { updated.remedyForm.defaultSize },
            batchNumber = updated.batchNumber.trim(),
            shelfLocation = updated.shelfLocation.trim(),
            notes = updated.notes.trim(),
            quantity = updated.quantity.coerceAtLeast(0),
            minStockLevel = updated.minStockLevel.coerceAtLeast(0),
            updatedAt = now,
        )
        remedyDao.updateRemedy(normalized)
        if (existing != null && existing.quantity != normalized.quantity) {
            val delta = normalized.quantity - existing.quantity
            movementDao.insertMovement(
                StockMovementEntity(
                    remedyId = normalized.id,
                    remedyName = normalized.name,
                    potency = normalized.potency,
                    delta = delta,
                    resultingQuantity = normalized.quantity,
                    reason = MovementReason.AUDIT_ADJUSTMENT.name,
                    note = "Updated via remedy editor",
                    timestamp = now,
                )
            )
        }
    }

    suspend fun adjustStock(
        remedyId: Long,
        delta: Int,
        reason: MovementReason,
        note: String = "",
        newBatch: String? = null,
        newExpiryMillis: Long? = null,
    ): Boolean {
        return remedyDao.adjustStockTransaction(
            remedyId = remedyId,
            delta = delta,
            reason = reason.name,
            note = note.trim(),
            newBatch = newBatch,
            newExpiryMillis = newExpiryMillis,
            movementDao = movementDao,
        )
    }

    suspend fun deleteRemedy(remedyId: Long) {
        remedyDao.deleteRemedyById(remedyId)
    }

    /**
     * Explicit user-triggered starter kit import (never auto-seeded silently).
     */
    suspend fun loadStarterHomeKit(): Int {
        val now = System.currentTimeMillis()
        val days = { d: Long -> now + TimeUnit.DAYS.toMillis(d) }
        val starter = listOf(
            RemedyEntity(
                name = "Arnica Montana",
                commonName = "Mountain Daisy",
                potency = "30C",
                form = RemedyForm.DILUTION.name,
                brand = "Dr. Reckeweg",
                bottleSize = "30 ml",
                quantity = 5,
                minStockLevel = 2,
                batchNumber = "RW-2409A",
                expiryDateMillis = days(620),
                shelfLocation = "Rack A · Shelf 1",
                notes = "Trauma, bruising & muscle soreness",
                updatedAt = now,
            ),
            RemedyEntity(
                name = "Nux Vomica",
                commonName = "Poison Nut",
                potency = "200C",
                form = RemedyForm.DILUTION.name,
                brand = "SBL",
                bottleSize = "30 ml",
                quantity = 2,
                minStockLevel = 2,
                batchNumber = "SBL-8812",
                expiryDateMillis = days(540),
                shelfLocation = "Rack B · Shelf 3",
                notes = "Digestive strain & irritability; evening dose",
                updatedAt = now,
            ),
            RemedyEntity(
                name = "Belladonna",
                commonName = "Deadly Nightshade",
                potency = "30C",
                form = RemedyForm.GLOBULES.name,
                brand = "Boiron",
                bottleSize = "15 g",
                quantity = 1,
                minStockLevel = 2,
                batchNumber = "BO-1140",
                expiryDateMillis = days(65),
                shelfLocation = "Kit Box · Tray 1",
                notes = "Sudden high fever & throbbing congestion",
                updatedAt = now,
            ),
            RemedyEntity(
                name = "Calendula Officinalis",
                commonName = "Marigold",
                potency = "Q",
                form = RemedyForm.MOTHER_TINCTURE.name,
                brand = "Schwabe",
                bottleSize = "100 ml",
                quantity = 4,
                minStockLevel = 1,
                batchNumber = "SW-5032",
                expiryDateMillis = days(410),
                shelfLocation = "Tincture Cabinet",
                notes = "Antiseptic wash for cuts; dilute before topical use",
                updatedAt = now,
            ),
            RemedyEntity(
                name = "Aconitum Napellus",
                commonName = "Monkshood",
                potency = "200C",
                form = RemedyForm.DILUTION.name,
                brand = "Dr. Reckeweg",
                bottleSize = "30 ml",
                quantity = 0,
                minStockLevel = 2,
                batchNumber = "RW-2204K",
                expiryDateMillis = days(290),
                shelfLocation = "Rack A · Shelf 1",
                notes = "Acute sudden chill & restlessness",
                updatedAt = now,
            ),
            RemedyEntity(
                name = "Rhus Toxicodendron",
                commonName = "Poison Ivy",
                potency = "1M",
                form = RemedyForm.DILUTION.name,
                brand = "SBL",
                bottleSize = "30 ml",
                quantity = 3,
                minStockLevel = 1,
                batchNumber = "SBL-7709",
                expiryDateMillis = days(820),
                shelfLocation = "Rack C · High Potency",
                notes = "Joint stiffness improved by warm motion",
                updatedAt = now,
            ),
            RemedyEntity(
                name = "Calcarea Phosphorica",
                commonName = "Calcium Phosphate",
                potency = "6X",
                form = RemedyForm.BIOCHEMIC.name,
                brand = "Schwabe",
                bottleSize = "25 g",
                quantity = 6,
                minStockLevel = 2,
                batchNumber = "SW-3318",
                expiryDateMillis = days(45),
                shelfLocation = "Biochemic Drawer 1",
                notes = "Tissue salt for bone recovery & growth",
                updatedAt = now,
            ),
            RemedyEntity(
                name = "Pulsatilla Nigricans",
                commonName = "Wind Flower",
                potency = "30C",
                form = RemedyForm.DILUTION.name,
                brand = "Bakson's",
                bottleSize = "30 ml",
                quantity = 4,
                minStockLevel = 2,
                batchNumber = "BK-9021",
                expiryDateMillis = days(730),
                shelfLocation = "Rack B · Shelf 2",
                notes = "Keep tightly capped away from camphor",
                updatedAt = now,
            ),
        )
        val ids = remedyDao.insertRemedies(starter)
        val movements = starter.zip(ids).map { (remedy, id) ->
            StockMovementEntity(
                remedyId = id,
                remedyName = remedy.name,
                potency = remedy.potency,
                delta = remedy.quantity,
                resultingQuantity = remedy.quantity,
                reason = MovementReason.INITIAL_STOCK.name,
                note = "Imported from Starter Home Kit",
                timestamp = now,
            )
        }
        movementDao.insertMovements(movements)
        return starter.size
    }
}
