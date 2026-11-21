package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.MovementReason

@Entity(tableName = "stock_movements")
data class StockMovementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val remedyId: Long? = null,
    val remedyName: String,
    val potency: String,
    val delta: Int,
    val resultingQuantity: Int,
    val reason: String = MovementReason.AUDIT_ADJUSTMENT.name,
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis(),
) {
    val movementReason: MovementReason
        get() = MovementReason.fromRaw(reason)
}
