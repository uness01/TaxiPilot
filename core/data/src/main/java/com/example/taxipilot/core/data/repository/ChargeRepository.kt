package com.example.taxipilot.core.data.repository

import com.example.taxipilot.core.data.database.dao.ChargeDao
import com.example.taxipilot.core.data.database.entity.ChargeEntity
import com.example.taxipilot.core.data.database.entity.TypeCharge
import kotlinx.coroutines.flow.Flow

class ChargeRepository(private val dao: ChargeDao) {

    fun getAll(): Flow<List<ChargeEntity>> = dao.getAll()

    fun getByTaxi(taxiId: Long): Flow<List<ChargeEntity>> = dao.getByTaxi(taxiId)

    fun getByType(type: TypeCharge): Flow<List<ChargeEntity>> = dao.getByType(type)

    fun getByTaxiAndType(taxiId: Long, type: TypeCharge): Flow<List<ChargeEntity>> =
        dao.getByTaxiAndType(taxiId, type)

    fun getByPeriode(debut: Long, fin: Long): Flow<List<ChargeEntity>> =
        dao.getByPeriode(debut, fin)

    suspend fun getTotalChargesTaxi(taxiId: Long, debut: Long, fin: Long): Double =
        dao.getTotalChargesTaxi(taxiId, debut, fin)

    suspend fun getTotalChargesPeriode(debut: Long, fin: Long): Double =
        dao.getTotalChargesPeriode(debut, fin)

    suspend fun insert(charge: ChargeEntity): Long = dao.insert(charge)

    suspend fun update(charge: ChargeEntity) = dao.update(charge)

    suspend fun delete(charge: ChargeEntity) = dao.delete(charge)
}
