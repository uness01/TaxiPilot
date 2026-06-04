package com.example.taxipilot.core.data.repository

// Repository pour les charges locales (Room/SQLite).
// Expose les opérations CRUD et les calculs de dépenses par période ou par taxi.
// Utilisé par DriverViewModel (saisie de dépenses) et OwnerViewModel (tableau de bord financier).

import com.example.taxipilot.core.data.database.dao.ChargeDao
import com.example.taxipilot.core.data.database.entity.ChargeEntity
import com.example.taxipilot.core.data.database.entity.TypeCharge
import kotlinx.coroutines.flow.Flow

class ChargeRepository(private val dao: ChargeDao) {

    // Toutes les charges (flux observable, du plus récent)
    fun getAll(): Flow<List<ChargeEntity>> = dao.getAll()

    // Charges d'un taxi précis (son historique de dépenses)
    fun getByTaxi(taxiId: Long): Flow<List<ChargeEntity>> = dao.getByTaxi(taxiId)

    // Charges filtrées par type (DIESEL, REPARATION, AUTRE)
    fun getByType(type: TypeCharge): Flow<List<ChargeEntity>> = dao.getByType(type)

    // Charges d'un taxi précis filtrées par type (ex : tous les diesels de tel taxi)
    fun getByTaxiAndType(taxiId: Long, type: TypeCharge): Flow<List<ChargeEntity>> =
        dao.getByTaxiAndType(taxiId, type)

    // Charges dans une fenêtre temporelle
    fun getByPeriode(debut: Long, fin: Long): Flow<List<ChargeEntity>> =
        dao.getByPeriode(debut, fin)

    // Total des dépenses d'un taxi sur une période (retourne 0.0 si aucune charge)
    suspend fun getTotalChargesTaxi(taxiId: Long, debut: Long, fin: Long): Double =
        dao.getTotalChargesTaxi(taxiId, debut, fin)

    // Total de toutes les charges sur une période (vue globale du propriétaire)
    suspend fun getTotalChargesPeriode(debut: Long, fin: Long): Double =
        dao.getTotalChargesPeriode(debut, fin)

    // Insère une charge et retourne l'ID généré
    suspend fun insert(charge: ChargeEntity): Long = dao.insert(charge)

    // Met à jour une charge existante
    suspend fun update(charge: ChargeEntity) = dao.update(charge)

    // Supprime une charge
    suspend fun delete(charge: ChargeEntity) = dao.delete(charge)
}
