package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        SimulatorProfileEntity::class,
        MileageTransactionEntity::class,
        AwardBookingEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class KrisFlyerDatabase : RoomDatabase() {
    abstract fun krisFlyerDao(): KrisFlyerDao

    companion object {
        @Volatile
        private var INSTANCE: KrisFlyerDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("UPDATE simulator_profile SET languageCode = 'en'")
            }
        }

        fun getInstance(context: Context): KrisFlyerDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    KrisFlyerDatabase::class.java,
                    "krisflyer_sim.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
