package com.example.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.atan2

data class SensorRotationState(
  val isAvailable: Boolean = false,
  val isListening: Boolean = false,
  val currentDegrees: Double = 0.0,
  val rawAzimuthDeg: Double = 0.0,
  val accumulatedRevolutions: Double = 0.0,
  val isHeld: Boolean = false
)

class RotationSensorManager(context: Context) : SensorEventListener {

  private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
  private val rotationVectorSensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    ?: sensorManager?.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR)

  private val _state = MutableStateFlow(
    SensorRotationState(isAvailable = rotationVectorSensor != null)
  )
  val state: StateFlow<SensorRotationState> = _state.asStateFlow()

  private var tareAzimuthDeg: Double = 0.0
  private var lastRawAzimuthDeg: Double? = null
  private var totalContinuousDeg: Double = 0.0
  private var isHeld: Boolean = false

  fun start() {
    if (rotationVectorSensor == null || sensorManager == null) return
    lastRawAzimuthDeg = null
    totalContinuousDeg = 0.0
    tareAzimuthDeg = 0.0
    isHeld = false
    sensorManager.registerListener(this, rotationVectorSensor, SensorManager.SENSOR_DELAY_UI)
    _state.value = _state.value.copy(isListening = true, isHeld = false)
  }

  fun stop() {
    sensorManager?.unregisterListener(this)
    _state.value = _state.value.copy(isListening = false)
  }

  fun tare() {
    val currentRaw = lastRawAzimuthDeg ?: 0.0
    tareAzimuthDeg = currentRaw
    totalContinuousDeg = 0.0
    _state.value = _state.value.copy(
      currentDegrees = 0.0,
      accumulatedRevolutions = 0.0
    )
  }

  fun toggleHold() {
    isHeld = !isHeld
    _state.value = _state.value.copy(isHeld = isHeld)
  }

  override fun onSensorChanged(event: SensorEvent?) {
    if (event == null || isHeld) return

    if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR ||
      event.sensor.type == Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR
    ) {
      val rotationMatrix = FloatArray(9)
      SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)

      val orientation = FloatArray(3)
      SensorManager.getOrientation(rotationMatrix, orientation)

      // Azimuth in degrees [0, 360)
      val azimuthRad = orientation[0]
      var azimuthDeg = Math.toDegrees(azimuthRad.toDouble())
      if (azimuthDeg < 0) {
        azimuthDeg += 360.0
      }

      val previousRaw = lastRawAzimuthDeg
      if (previousRaw != null) {
        // Continuous unwrapping: detect jump from ~359 to ~0 or vice versa
        var delta = azimuthDeg - previousRaw
        if (delta > 180.0) {
          delta -= 360.0
        } else if (delta < -180.0) {
          delta += 360.0
        }
        totalContinuousDeg += delta
      } else {
        tareAzimuthDeg = azimuthDeg
      }
      lastRawAzimuthDeg = azimuthDeg

      val currentRelDeg = totalContinuousDeg
      val revs = currentRelDeg / 360.0

      _state.value = _state.value.copy(
        currentDegrees = currentRelDeg,
        rawAzimuthDeg = azimuthDeg,
        accumulatedRevolutions = revs
      )
    }
  }

  override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    // No-op
  }
}
