package com.example.taxipilot.core.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.taxipilot.core.data.database.dao.*
import com.example.taxipilot.core.data.database.entity.*

@Database(
    entities = [
        // Domain entities
        TaxiEntity::class,
        ChauffeurEntity::class,
        TrajetEntity::class,
        ChargeEntity::class,
        ReservationEntity::class,
        // App user entities
        UserEntity::class,
        VehicleEntity::class,
        TripEntity::class,
    ],
    version = 4,
    exportSchema = false
)
abstract class TaxiPilotDatabase : RoomDatabase() {

    // Domain DAOs
    abstract fun taxiDao(): TaxiDao
    abstract fun chauffeurDao(): ChauffeurDao
    abstract fun trajetDao(): TrajetDao
    abstract fun chargeDao(): ChargeDao
    abstract fun reservationDao(): ReservationDao

    // App user DAOs
    abstract fun userDao(): UserDao
    abstract fun vehicleDao(): VehicleDao
    abstract fun tripDao(): TripDao

    companion object {
        @Volatile
        private var INSTANCE: TaxiPilotDatabase? = null

        fun getInstance(context: Context): TaxiPilotDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    TaxiPilotDatabase::class.java,
                    "taxipilot.db"
                )
                    .fallbackToDestructiveMigration() // safe for development
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
