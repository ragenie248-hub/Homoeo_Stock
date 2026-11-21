package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.ExpiryStatus
import com.example.data.model.RemedyForm
import com.example.data.model.evaluateExpiryStatus

@Entity(tableName = "remedies")
data class RemedyEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val commonName: String = "",
    val potency: String,
    val form: String = RemedyForm.DILUTION.name,
    val brand: String = "",
    val bottleSize: String = "30 ml",
    val quantity: Int = 0,
    val minStockLevel: Int = 2,
    val batchNumber: String = "",
    val expiryDateMillis: Long? = null,
    val shelfLocation: String = "",
    val notes: String = "",
    val updatedAt: Long = System.currentTimeMillis(),
) {
    val remedyForm: RemedyForm
        get() = RemedyForm.fromRaw(form)

    val isOutOfStock: Boolean
        get() = quantity <= 0

    val isLowStock: Boolean
        get() = quantity in 1..minStockLevel

    val needsReorder: Boolean
        get() = quantity <= minStockLevel

    fun expiryStatus(nowMillis: Long = System.currentTimeMillis()): ExpiryStatus =
        evaluateExpiryStatus(expiryDateMillis, nowMillis)
}
