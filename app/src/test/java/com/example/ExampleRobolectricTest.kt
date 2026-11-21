package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.HomeoStockDatabase
import com.example.data.local.RemedyEntity
import com.example.data.model.MovementReason
import com.example.data.model.RemedyForm
import com.example.data.repository.StockRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var db: HomeoStockDatabase
    private lateinit var repository: StockRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, HomeoStockDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = StockRepository(db.remedyDao(), db.stockMovementDao())
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("HomeoStock", appName)
    }

    @Test
    fun `add dispense and restock remedy updates stock and logs movements`() = runTest {
        val id = repository.addRemedy(
            RemedyEntity(
                name = "Arnica Montana",
                potency = "30c",
                form = RemedyForm.DILUTION.name,
                bottleSize = "30 ml",
                quantity = 3,
                minStockLevel = 2,
            )
        )
        assertTrue(id > 0)

        repository.adjustStock(
            remedyId = id,
            delta = -1,
            reason = MovementReason.DISPENSED,
            note = "Patient Rx",
        )

        val afterDispense = repository.observeRemedyById(id).first()!!
        assertEquals("30C", afterDispense.potency)
        assertEquals(2, afterDispense.quantity)
        assertTrue(afterDispense.isLowStock)

        repository.adjustStock(
            remedyId = id,
            delta = 4,
            reason = MovementReason.RESTOCK,
            note = "Supplier delivery",
            newBatch = "RW-9901",
        )

        val afterRestock = repository.observeRemedyById(id).first()!!
        assertEquals(6, afterRestock.quantity)
        assertEquals("RW-9901", afterRestock.batchNumber)

        val movements = repository.observeMovementsForRemedy(id).first()
        assertEquals(3, movements.size)
        assertEquals(4, movements.first().delta)
    }
}
