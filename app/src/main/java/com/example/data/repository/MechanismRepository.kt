package com.example.data.repository

import com.example.data.db.dao.CustomMechanismDao
import com.example.data.db.entity.CustomMechanismEntity
import com.example.domain.MechanismConfig
import com.example.domain.MechanismType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MechanismRepository(private val dao: CustomMechanismDao) {

  val customMechanisms: Flow<List<MechanismConfig>> = dao.getAllCustomMechanisms().map { entities ->
    entities.map { it.toDomain() }
  }

  suspend fun insertCustomMechanism(config: MechanismConfig): Long {
    val entity = config.toEntity()
    return dao.insertMechanism(entity)
  }

  suspend fun deleteCustomMechanism(id: Long) {
    dao.deleteMechanismById(id)
  }

  private fun CustomMechanismEntity.toDomain(): MechanismConfig {
    val mType = try {
      MechanismType.valueOf(this.mechanismType)
    } catch (_: Exception) {
      MechanismType.WHEEL
    }
    return MechanismConfig(
      id = this.id,
      name = this.name,
      type = mType,
      diameterMm = this.diameterMm,
      leadMm = this.leadMm,
      teethCount = this.teethCount,
      toothPitchMm = this.toothPitchMm,
      arcRadiusMm = this.arcRadiusMm,
      gearRatio = if (this.gearRatio != 0.0) this.gearRatio else 1.0,
      isCustom = true,
      notes = this.notes
    )
  }

  private fun MechanismConfig.toEntity(): CustomMechanismEntity {
    return CustomMechanismEntity(
      id = if (this.isCustom) this.id else 0,
      name = this.name,
      mechanismType = this.type.name,
      diameterMm = this.diameterMm,
      leadMm = this.leadMm,
      teethCount = this.teethCount,
      toothPitchMm = this.toothPitchMm,
      arcRadiusMm = this.arcRadiusMm,
      gearRatio = this.gearRatio,
      notes = this.notes
    )
  }
}
