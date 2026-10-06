package com.example.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "measurement_logs")
data class MeasurementLogEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val title: String,
  val mechanismType: String,
  val mechanismName: String,
  val degrees: Double,
  val revolutions: Double,
  val distanceCm: Double,
  val distanceMm: Double,
  val distanceInches: Double,
  val fractionalInches: String,
  val notes: String = "",
  val timestamp: Long = System.currentTimeMillis()
)
