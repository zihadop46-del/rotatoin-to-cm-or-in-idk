package com.example.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.db.entity.MeasurementLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MeasurementLogDao {
  @Query("SELECT * FROM measurement_logs ORDER BY timestamp DESC")
  fun getAllLogs(): Flow<List<MeasurementLogEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertLog(log: MeasurementLogEntity): Long

  @Query("DELETE FROM measurement_logs WHERE id = :id")
  suspend fun deleteLogById(id: Long)

  @Query("DELETE FROM measurement_logs")
  suspend fun clearAllLogs()

  @Query("SELECT SUM(distanceCm) FROM measurement_logs")
  fun getTotalDistanceCm(): Flow<Double?>
}
