package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RemedyDao {
    @Query("SELECT * FROM remedies ORDER BY name COLLATE NOCASE ASC, potency ASC")
    fun observeAllRemedies(): Flow<List<RemedyEntity>>

    @Query("SELECT * FROM remedies WHERE id = :id")
    fun observeRemedyById(id: Long): Flow<RemedyEntity?>

    @Query("SELECT * FROM remedies WHERE id = :id")
    suspend fun getRemedyById(id: Long): RemedyEntity?

    @Query("SELECT COUNT(*) FROM remedies")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRemedy(remedy: RemedyEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRemedies(remedies: List<RemedyEntity>): List<Long>

    @Update
    suspend fun updateRemedy(remedy: RemedyEntity)

    @Query("DELETE FROM remedies WHERE id = :id")
    suspend fun deleteRemedyById(id: Long)

    @Transaction
    suspend fun insertRemedyWithMovement(
        remedy: RemedyEntity,
        movementDao: StockMovementDao,
        reason: String,
        note: String,
    ): Long {
        val insertedId = insertRemedy(remedy)
        if (remedy.quantity > 0) {
            movementDao.insertMovement(
                StockMovementEntity(
                    remedyId = insertedId,
                    remedyName = remedy.name,
                    potency = remedy.potency,
                    delta = remedy.quantity,
                    resultingQuantity = remedy.quantity,
                    reason = reason,
                    note = note,
                    timestamp = remedy.updatedAt,
                )
            )
        }
        return insertedId
    }

    @Transaction
    suspend fun adjustStockTransaction(
        remedyId: Long,
        delta: Int,
        reason: String,
        note: String,
        newBatch: String? = null,
        newExpiryMillis: Long? = null,
        movementDao: StockMovementDao,
    ): Boolean {
        val current = getRemedyById(remedyId) ?: return false
        val nextQuantity = (current.quantity + delta).coerceAtLeast(0)
        val actualDelta = nextQuantity - current.quantity
        val now = System.currentTimeMillis()
        val updated = current.copy(
            quantity = nextQuantity,
            batchNumber = if (!newBatch.isNullOrBlank()) newBatch.trim() else current.batchNumber,
            expiryDateMillis = newExpiryMillis ?: current.expiryDateMillis,
            updatedAt = now,
        )
        updateRemedy(updated)
        if (actualDelta != 0 || !newBatch.isNullOrBlank() || newExpiryMillis != null) {
            movementDao.insertMovement(
                StockMovementEntity(
                    remedyId = current.id,
                    remedyName = current.name,
                    potency = current.potency,
                    delta = actualDelta,
                    resultingQuantity = nextQuantity,
                    reason = reason,
                    note = note,
                    timestamp = now,
                )
            )
        }
        return true
    }
}
