package com.example.data.repository

import com.example.data.db.dao.MeasurementLogDao
import com.example.data.db.entity.MeasurementLogEntity
import kotlinx.coroutines.flow.Flow

class MeasurementRepository(private val dao: MeasurementLogDao) {

  val allLogs: Flow<List<MeasurementLogEntity>> = dao.getAllLogs()

  val totalDistanceCm: Flow<Double?> = dao.getTotalDistanceCm()

  suspend fun insertLog(log: MeasurementLogEntity): Long {
    return dao.insertLog(log)
  }

  suspend fun deleteLogById(id: Long) {
    dao.deleteLogById(id)
  }

  suspend fun clearAll() {
    dao.clearAllLogs()
  }
}
