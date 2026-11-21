package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [RemedyEntity::class, StockMovementEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class HomeoStockDatabase : RoomDatabase() {
    abstract fun remedyDao(): RemedyDao
    abstract fun stockMovementDao(): StockMovementDao

    companion object {
        @Volatile
        private var INSTANCE: HomeoStockDatabase? = null

        fun getInstance(context: Context): HomeoStockDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    HomeoStockDatabase::class.java,
                    "homeostock.db",
                ).build().also { INSTANCE = it }
            }
        }
    }
}
