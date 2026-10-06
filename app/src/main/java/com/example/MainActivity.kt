package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.MainViewModel
import com.example.ui.screens.MotionStepperScreen
import com.example.ui.screens.PresetsScreen
import com.example.ui.screens.SamsungCalculatorScreen
import com.example.ui.screens.SamsungHistoryScreen
import com.example.ui.screens.SamsungUnitConverterScreen
import com.example.ui.screens.SensorProtractorScreen
import com.example.ui.theme.RotaryMeasureTheme
import com.example.ui.theme.SamsungDarkBackground

enum class SamsungViewMode {
  CALCULATOR,
  UNIT_CONVERTER,
  HISTORY,
  PROTRACTOR,
  STEPPER,
  PRESETS
}

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      RotaryMeasureTheme {
        SamsungRotaryApp()
      }
    }
  }
}

@Composable
fun SamsungRotaryApp(
  viewModel: MainViewModel = viewModel()
) {
  var currentView by rememberSaveable { mutableStateOf(SamsungViewMode.CALCULATOR) }

  // Handle system back button to return to Samsung Calculator home screen
  BackHandler(enabled = currentView != SamsungViewMode.CALCULATOR) {
    currentView = SamsungViewMode.CALCULATOR
  }

  val currentDegrees by viewModel.degrees.collectAsStateWithLifecycle()
  val activeMechanism by viewModel.selectedMechanism.collectAsStateWithLifecycle()
  val allMechanisms by viewModel.allMechanisms.collectAsStateWithLifecycle()
  val precisionResult by viewModel.precisionResult.collectAsStateWithLifecycle()
  val logs by viewModel.measurementLogs.collectAsStateWithLifecycle()
  val totalOdometerCm by viewModel.totalDistanceCm.collectAsStateWithLifecycle()
  val sensorState by viewModel.sensorState.collectAsStateWithLifecycle()
  val stepperAngle by viewModel.stepperAngle.collectAsStateWithLifecycle()
  val microstepping by viewModel.microstepping.collectAsStateWithLifecycle()
  val stepperResult by viewModel.stepperResult.collectAsStateWithLifecycle()

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(SamsungDarkBackground)
      .statusBarsPadding()
  ) {
    when (currentView) {
      SamsungViewMode.CALCULATOR -> {
        SamsungCalculatorScreen(
          currentDegrees = currentDegrees,
          activeMechanism = activeMechanism,
          allMechanisms = allMechanisms,
          precisionResult = precisionResult,
          onDegreesChanged = { viewModel.setDegrees(it) },
          onMechanismSelected = { viewModel.selectMechanism(it) },
          onOpenHistory = { currentView = SamsungViewMode.HISTORY },
          onOpenUnitConverter = { currentView = SamsungViewMode.UNIT_CONVERTER },
          onOpenProtractor = { currentView = SamsungViewMode.PROTRACTOR },
          onOpenStepper = { currentView = SamsungViewMode.STEPPER },
          onOpenPresets = { currentView = SamsungViewMode.PRESETS },
          onSaveMeasurement = { title, notes ->
            viewModel.saveCurrentMeasurement(title, notes)
          }
        )
      }

      SamsungViewMode.UNIT_CONVERTER -> {
        SamsungUnitConverterScreen(
          currentDegrees = currentDegrees,
          activeMechanism = activeMechanism,
          allMechanisms = allMechanisms,
          precisionResult = precisionResult,
          onDegreesChanged = { viewModel.setDegrees(it) },
          onMechanismSelected = { viewModel.selectMechanism(it) },
          onBackToCalculator = { currentView = SamsungViewMode.CALCULATOR }
        )
      }

      SamsungViewMode.HISTORY -> {
        SamsungHistoryScreen(
          logs = logs,
          totalOdometerCm = totalOdometerCm,
          onSelectLog = { deg ->
            viewModel.setDegrees(deg)
            currentView = SamsungViewMode.CALCULATOR
          },
          onClearAll = { viewModel.clearAllLogs() },
          onBack = { currentView = SamsungViewMode.CALCULATOR }
        )
      }

      SamsungViewMode.PROTRACTOR -> {
        SensorProtractorScreen(
          sensorManager = viewModel.sensorManager,
          sensorState = sensorState,
          activeMechanism = activeMechanism,
          onApplyToMainDial = { deg ->
            viewModel.setDegrees(deg)
            currentView = SamsungViewMode.CALCULATOR
          },
          onQuickSave = { deg, title ->
            viewModel.setDegrees(deg)
            viewModel.saveCurrentMeasurement(title, "Recorded via gyro sensor")
          }
        )
      }

      SamsungViewMode.STEPPER -> {
        MotionStepperScreen(
          activeMechanism = activeMechanism,
          stepperAngle = stepperAngle,
          microstepping = microstepping,
          stepperResult = stepperResult,
          onStepperAngleChanged = { viewModel.setStepperAngle(it) },
          onMicrosteppingChanged = { viewModel.setMicrostepping(it) }
        )
      }

      SamsungViewMode.PRESETS -> {
        PresetsScreen(
          activeMechanism = activeMechanism,
          allMechanisms = allMechanisms,
          onSelectMechanism = { mech ->
            viewModel.selectMechanism(mech)
            currentView = SamsungViewMode.CALCULATOR
          },
          onSaveCustomMechanism = { name, type, dia, lead, teeth, pitch, arcR, gear, notes ->
            viewModel.saveCustomMechanism(name, type, dia, lead, teeth, pitch, arcR, gear, notes)
          },
          onDeleteCustomMechanism = { viewModel.deleteCustomMechanism(it) }
        )
      }
    }
  }
}
