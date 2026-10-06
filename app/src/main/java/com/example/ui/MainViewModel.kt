package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.db.entity.MeasurementLogEntity
import com.example.data.repository.MeasurementRepository
import com.example.data.repository.MechanismRepository
import com.example.domain.MechanismConfig
import com.example.domain.MechanismPresets
import com.example.domain.MechanismType
import com.example.domain.PrecisionCalculator
import com.example.domain.PrecisionResult
import com.example.domain.StepperMotionResult
import com.example.sensor.RotationSensorManager
import com.example.sensor.SensorRotationState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

  private val database = AppDatabase.getDatabase(application)
  private val measurementRepo = MeasurementRepository(database.measurementLogDao())
  private val mechanismRepo = MechanismRepository(database.customMechanismDao())
  val sensorManager = RotationSensorManager(application)

  val sensorState: StateFlow<SensorRotationState> = sensorManager.state

  // Current Rotational Degrees
  private val _degrees = MutableStateFlow(90.0)
  val degrees: StateFlow<Double> = _degrees.asStateFlow()

  // Selected Mechanism Configuration
  private val _selectedMechanism = MutableStateFlow(MechanismPresets.defaultPresets[1]) // 100mm roller default
  val selectedMechanism: StateFlow<MechanismConfig> = _selectedMechanism.asStateFlow()

  // Custom and Built-in Mechanisms
  val customMechanisms: StateFlow<List<MechanismConfig>> = mechanismRepo.customMechanisms
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyList()
    )

  val allMechanisms: StateFlow<List<MechanismConfig>> = customMechanisms.map { customs ->
    MechanismPresets.defaultPresets + customs
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = MechanismPresets.defaultPresets
  )

  // Real-time Calculation Result
  val precisionResult: StateFlow<PrecisionResult> = combine(
    _degrees,
    _selectedMechanism
  ) { deg, mech ->
    PrecisionCalculator.calculateFromDegrees(deg, mech)
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = PrecisionCalculator.calculateFromDegrees(90.0, MechanismPresets.defaultPresets[1])
  )

  // Stepper & CNC calculation state
  private val _stepperAngle = MutableStateFlow(1.8) // Standard NEMA 17 (200 steps/rev)
  val stepperAngle: StateFlow<Double> = _stepperAngle.asStateFlow()

  private val _microstepping = MutableStateFlow(16) // 1/16 microstepping
  val microstepping: StateFlow<Int> = _microstepping.asStateFlow()

  val stepperResult: StateFlow<StepperMotionResult> = combine(
    _stepperAngle,
    _microstepping,
    _selectedMechanism
  ) { angle, micro, mech ->
    PrecisionCalculator.calculateStepperMotion(angle, micro, mech)
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = PrecisionCalculator.calculateStepperMotion(1.8, 16, MechanismPresets.defaultPresets[1])
  )

  // Saved measurement logs
  val measurementLogs: StateFlow<List<MeasurementLogEntity>> = measurementRepo.allLogs
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyList()
    )

  val totalDistanceCm: StateFlow<Double?> = measurementRepo.totalDistanceCm
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = 0.0
    )

  fun setDegrees(value: Double) {
    _degrees.value = value
  }

  fun addDegrees(delta: Double) {
    _degrees.value += delta
  }

  fun selectMechanism(config: MechanismConfig) {
    _selectedMechanism.value = config
  }

  fun setStepperAngle(angle: Double) {
    _stepperAngle.value = angle
  }

  fun setMicrostepping(micro: Int) {
    _microstepping.value = micro
  }

  /**
   * Bidirectional Conversion: target distance (cm or inches) to degrees.
   */
  fun setDegreesFromTargetDistance(distance: Double, isCentimeters: Boolean) {
    val mech = _selectedMechanism.value
    val deg = if (isCentimeters) {
      PrecisionCalculator.calculateDegreesFromCm(distance, mech)
    } else {
      PrecisionCalculator.calculateDegreesFromInches(distance, mech)
    }
    _degrees.value = deg
  }

  fun saveCurrentMeasurement(title: String, notes: String = "") {
    val currentRes = precisionResult.value
    val mech = _selectedMechanism.value
    viewModelScope.launch {
      measurementRepo.insertLog(
        MeasurementLogEntity(
          title = if (title.isNotBlank()) title else "${mech.name} @ ${PrecisionCalculator.formatNum(currentRes.degrees, 1)}°",
          mechanismType = mech.type.name,
          mechanismName = mech.name,
          degrees = currentRes.degrees,
          revolutions = currentRes.revolutions,
          distanceCm = currentRes.distanceCm,
          distanceMm = currentRes.distanceMm,
          distanceInches = currentRes.distanceInches,
          fractionalInches = currentRes.fractionalInch,
          notes = notes
        )
      )
    }
  }

  fun deleteLog(id: Long) {
    viewModelScope.launch {
      measurementRepo.deleteLogById(id)
    }
  }

  fun clearAllLogs() {
    viewModelScope.launch {
      measurementRepo.clearAll()
    }
  }

  fun saveCustomMechanism(
    name: String,
    type: MechanismType,
    diameterMm: Double,
    leadMm: Double,
    teethCount: Int,
    toothPitchMm: Double,
    arcRadiusMm: Double,
    gearRatio: Double,
    notes: String
  ) {
    viewModelScope.launch {
      val config = MechanismConfig(
        name = name,
        type = type,
        diameterMm = diameterMm,
        leadMm = leadMm,
        teethCount = teethCount,
        toothPitchMm = toothPitchMm,
        arcRadiusMm = arcRadiusMm,
        gearRatio = if (gearRatio > 0.0) gearRatio else 1.0,
        isCustom = true,
        notes = notes
      )
      val newId = mechanismRepo.insertCustomMechanism(config)
      _selectedMechanism.value = config.copy(id = newId)
    }
  }

  fun deleteCustomMechanism(id: Long) {
    viewModelScope.launch {
      mechanismRepo.deleteCustomMechanism(id)
      if (_selectedMechanism.value.id == id && _selectedMechanism.value.isCustom) {
        _selectedMechanism.value = MechanismPresets.defaultPresets[1]
      }
    }
  }

  fun applySensorDegrees() {
    val sensorDeg = sensorState.value.currentDegrees
    _degrees.value = sensorDeg
  }

  override fun onCleared() {
    super.onCleared()
    sensorManager.stop()
  }
}
