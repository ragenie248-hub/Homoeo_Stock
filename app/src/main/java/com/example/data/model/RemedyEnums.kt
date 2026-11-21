package com.example.data.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

enum class RemedyForm(val label: String, val defaultSize: String) {
    DILUTION("Dilution", "30 ml"),
    GLOBULES("Globules", "15 g"),
    MOTHER_TINCTURE("Mother Tincture (Q)", "30 ml"),
    BIOCHEMIC("Biochemic Tablets", "25 g"),
    TRITURATION("Trituration", "25 g"),
    DROPS("Oral Drops", "30 ml"),
    OINTMENT("Ointment / Cream", "25 g");

    companion object {
        fun fromRaw(raw: String): RemedyForm =
            entries.firstOrNull { it.name == raw || it.label.equals(raw, ignoreCase = true) } ?: DILUTION
    }
}

enum class MovementReason(val label: String, val isAddition: Boolean?) {
    INITIAL_STOCK("Initial Stock", true),
    RESTOCK("Restock", true),
    DISPENSED("Dispensed", false),
    EXPIRED_DISPOSED("Expired / Disposed", false),
    AUDIT_ADJUSTMENT("Audit Adjustment", null);

    companion object {
        fun fromRaw(raw: String): MovementReason =
            entries.firstOrNull { it.name == raw || it.label.equals(raw, ignoreCase = true) }
                ?: AUDIT_ADJUSTMENT
    }
}

enum class StockFilter(val label: String) {
    ALL("All"),
    LOW_STOCK("Low Stock"),
    OUT_OF_STOCK("Out of Stock"),
    EXPIRING_SOON("Expiring Soon"),
    EXPIRED("Expired"),
}

enum class SortOption(val label: String) {
    NAME_ASC("Name (A–Z)"),
    STOCK_LOW_FIRST("Stock (Low–High)"),
    EXPIRY_SOONEST("Expiry (Soonest)"),
    RECENTLY_UPDATED("Recently Updated"),
}

enum class ExpiryStatus {
    NO_EXPIRY,
    HEALTHY,
    EXPIRING_SOON,
    EXPIRED,
}

object RemedyConstants {
    val commonPotencies = listOf("6C", "12C", "30C", "200C", "1M", "10M", "Q", "3X", "6X", "12X", "LM1")
    val commonBrands = listOf("Dr. Reckeweg", "SBL", "Schwabe", "Boiron", "Bakson's", "Hahnemann Labs")
    val commonSizes = listOf("10 ml", "15 ml", "30 ml", "100 ml", "450 ml", "10 g", "15 g", "25 g", "450 g")
    val classicRemedies = listOf(
        "Arnica Montana" to "Mountain Daisy",
        "Nux Vomica" to "Poison Nut",
        "Belladonna" to "Deadly Nightshade",
        "Pulsatilla Nigricans" to "Wind Flower",
        "Aconitum Napellus" to "Monkshood",
        "Bryonia Alba" to "White Bryony",
        "Rhus Toxicodendron" to "Poison Ivy",
        "Sulphur" to "Brimstone",
        "Apis Mellifica" to "Honey Bee",
        "Arsenicum Album" to "White Arsenic",
        "Chamomilla" to "German Chamomile",
        "Gelsemium Sempervirens" to "Yellow Jasmine",
        "Hepar Sulphuris" to "Calcium Sulphide",
        "Ignatia Amara" to "St. Ignatius Bean",
        "Calcarea Carbonica" to "Oyster Shell",
        "Silicea" to "Flint / Silica",
    )

    const val EXPIRING_SOON_DAYS = 90L
}

fun evaluateExpiryStatus(expiryDateMillis: Long?, nowMillis: Long = System.currentTimeMillis()): ExpiryStatus {
    if (expiryDateMillis == null) return ExpiryStatus.NO_EXPIRY
    val diffDays = TimeUnit.MILLISECONDS.toDays(expiryDateMillis - nowMillis)
    return when {
        expiryDateMillis < nowMillis && diffDays <= 0 -> ExpiryStatus.EXPIRED
        diffDays <= RemedyConstants.EXPIRING_SOON_DAYS -> ExpiryStatus.EXPIRING_SOON
        else -> ExpiryStatus.HEALTHY
    }
}

fun daysUntilExpiry(expiryDateMillis: Long, nowMillis: Long = System.currentTimeMillis()): Long =
    TimeUnit.MILLISECONDS.toDays(expiryDateMillis - nowMillis)

fun formatMonthYear(millis: Long): String =
    SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(Date(millis))

fun formatFullDate(millis: Long): String =
    SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(millis))

fun formatTimestamp(millis: Long): String =
    SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(millis))
