package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.db.dao.CustomMechanismDao
import com.example.data.db.dao.MeasurementLogDao
import com.example.data.db.entity.CustomMechanismEntity
import com.example.data.db.entity.MeasurementLogEntity

@Database(
  entities = [MeasurementLogEntity::class, CustomMechanismEntity::class],
  version = 1,
  exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
  abstract fun measurementLogDao(): MeasurementLogDao
  abstract fun customMechanismDao(): CustomMechanismDao

  companion object {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          AppDatabase::class.java,
          "rotary_measure_db"
        ).fallbackToDestructiveMigration().build()
        INSTANCE = instance
        instance
      }
    }
  }
}
