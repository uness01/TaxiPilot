package com.example.taxipilot.core.data.database

// Base de données Room locale (SQLite) de l'application TaxiPilot.
// Elle regroupe TOUTES les tables (entités) et donne accès à leurs DAO.
// Le fichier physique s'appelle "taxipilot.db" et est stocké sur l'appareil.
// Version 4 — fallbackToDestructiveMigration() efface et recrée la DB si le schéma change.

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.taxipilot.core.data.database.dao.*
import com.example.taxipilot.core.data.database.entity.*

@Database(
    entities = [
        // Entités métier (flotte du propriétaire)
        TaxiEntity::class,
        ChauffeurEntity::class,
        TrajetEntity::class,
        ChargeEntity::class,
        ReservationEntity::class,
        // Entités utilisateur de l'app (auth locale)
        UserEntity::class,
        VehicleEntity::class,
        TripEntity::class,
    ],
    version = 4,
    exportSchema = false
)
abstract class TaxiPilotDatabase : RoomDatabase() {

    // ── DAO métier — accès aux tables de la flotte ────────────────────────────
    abstract fun taxiDao(): TaxiDao           // table "taxis"
    abstract fun chauffeurDao(): ChauffeurDao // table "chauffeurs"
    abstract fun trajetDao(): TrajetDao       // table "trajets"
    abstract fun chargeDao(): ChargeDao       // table "charges"
    abstract fun reservationDao(): ReservationDao // table "reservations"

    // ── DAO utilisateur — accès aux tables de l'auth locale ──────────────────
    abstract fun userDao(): UserDao       // table "users"
    abstract fun vehicleDao(): VehicleDao // table "vehicles"
    abstract fun tripDao(): TripDao       // table "trips"

    companion object {
        @Volatile
        private var INSTANCE: TaxiPilotDatabase? = null

        // Singleton : crée la DB une seule fois et la réutilise dans toute l'app.
        // @Volatile garantit que INSTANCE est visible depuis tous les threads.
        fun getInstance(context: Context): TaxiPilotDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    TaxiPilotDatabase::class.java,
                    "taxipilot.db" // nom du fichier SQLite sur l'appareil
                )
                    .fallbackToDestructiveMigration() // en dev : efface la DB si version change
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
