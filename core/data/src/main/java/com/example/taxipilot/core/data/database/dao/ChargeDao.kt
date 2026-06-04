package com.example.taxipilot.core.data.database.dao

// DAO pour la table "charges" — dépenses liées aux taxis.
// Deux fonctions d'agrégation importantes pour le tableau de bord financier :
//   - getTotalChargesTaxi   : total des dépenses d'un taxi sur une période
//   - getTotalChargesPeriode: total de TOUTES les dépenses sur une période (vue globale)
// COALESCE(..., 0.0) garantit que la somme retourne 0 et non null si aucun résultat.

import androidx.room.*
import com.example.taxipilot.core.data.database.entity.ChargeEntity
import com.example.taxipilot.core.data.database.entity.TypeCharge
import kotlinx.coroutines.flow.Flow

@Dao
interface ChargeDao {

    // Toutes les charges, du plus récent au plus ancien
    @Query("SELECT * FROM charges ORDER BY date DESC")
    fun getAll(): Flow<List<ChargeEntity>>

    // Charges d'un taxi précis (son historique de dépenses)
    @Query("SELECT * FROM charges WHERE taxiId = :taxiId ORDER BY date DESC")
    fun getByTaxi(taxiId: Long): Flow<List<ChargeEntity>>

    // Charges filtrées par type (DIESEL, REPARATION, AUTRE)
    @Query("SELECT * FROM charges WHERE type = :type ORDER BY date DESC")
    fun getByType(type: TypeCharge): Flow<List<ChargeEntity>>

    // Charges d'un taxi précis filtrées par type (ex : seulement les diesels de tel taxi)
    @Query("SELECT * FROM charges WHERE taxiId = :taxiId AND type = :type ORDER BY date DESC")
    fun getByTaxiAndType(taxiId: Long, type: TypeCharge): Flow<List<ChargeEntity>>

    // Charges dans une fenêtre temporelle [debut, fin]
    @Query("SELECT * FROM charges WHERE date BETWEEN :debut AND :fin ORDER BY date DESC")
    fun getByPeriode(debut: Long, fin: Long): Flow<List<ChargeEntity>>

    // Total des dépenses d'un taxi sur une période (retourne 0.0 si aucune charge)
    @Query("""
        SELECT COALESCE(SUM(montant), 0.0) FROM charges
        WHERE taxiId = :taxiId AND date BETWEEN :debut AND :fin
    """)
    suspend fun getTotalChargesTaxi(taxiId: Long, debut: Long, fin: Long): Double

    // Total de toutes les charges sur une période (vue globale propriétaire)
    @Query("SELECT COALESCE(SUM(montant), 0.0) FROM charges WHERE date BETWEEN :debut AND :fin")
    suspend fun getTotalChargesPeriode(debut: Long, fin: Long): Double

    // Insère une nouvelle charge (ABORT si conflit)
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(charge: ChargeEntity): Long

    // Met à jour une charge existante
    @Update
    suspend fun update(charge: ChargeEntity)

    // Supprime une charge
    @Delete
    suspend fun delete(charge: ChargeEntity)
}
