package com.example.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_mechanisms")
data class CustomMechanismEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val name: String,
  val mechanismType: String,
  val diameterMm: Double = 0.0,
  val leadMm: Double = 0.0,
  val teethCount: Int = 0,
  val toothPitchMm: Double = 0.0,
  val arcRadiusMm: Double = 0.0,
  val gearRatio: Double = 1.0,
  val notes: String = "",
  val createdAt: Long = System.currentTimeMillis()
)
