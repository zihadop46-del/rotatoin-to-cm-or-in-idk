package com.example.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.db.entity.CustomMechanismEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomMechanismDao {
  @Query("SELECT * FROM custom_mechanisms ORDER BY id DESC")
  fun getAllCustomMechanisms(): Flow<List<CustomMechanismEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMechanism(mechanism: CustomMechanismEntity): Long

  @Update
  suspend fun updateMechanism(mechanism: CustomMechanismEntity)

  @Query("DELETE FROM custom_mechanisms WHERE id = :id")
  suspend fun deleteMechanismById(id: Long)
}
