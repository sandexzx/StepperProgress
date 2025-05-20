package com.example.stepperprogress.service

import android.app.Service
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Binder
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

class StepCounterService : Service(), SensorEventListener {
    private val TAG = "StepCounterService"
    private val binder = LocalBinder()
    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null
    private var initialSteps = -1
    private var currentSteps = 0

    private val _stepFlow = MutableSharedFlow<Int>(
        replay = 1,
        extraBufferCapacity = 1
    )
    val stepFlow: SharedFlow<Int> = _stepFlow

    inner class LocalBinder : Binder() {
        fun getService(): StepCounterService = this@StepCounterService
    }

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        
        if (stepSensor == null) {
            Log.e(TAG, "No step counter sensor found on this device")
        } else {
            Log.d(TAG, "Step counter sensor initialized")
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        stepSensor?.let { sensor ->
            val registered = sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
            if (registered) {
                Log.d(TAG, "Step counter sensor listener registered")
            } else {
                Log.e(TAG, "Failed to register step counter sensor listener")
            }
        } ?: run {
            Log.e(TAG, "Cannot register listener: step sensor is null")
        }
        return START_STICKY
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_STEP_COUNTER) {
            val totalSteps = event.values[0].toInt()
            Log.d(TAG, "Raw step count: $totalSteps")
            
            if (initialSteps == -1) {
                initialSteps = totalSteps
                Log.d(TAG, "Initial steps set to: $initialSteps")
            }
            
            currentSteps = totalSteps - initialSteps
            Log.d(TAG, "Current steps: $currentSteps")
            
            _stepFlow.tryEmit(currentSteps)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.d(TAG, "Sensor accuracy changed: $accuracy")
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        Log.d(TAG, "Service destroyed, listener unregistered")
    }
} 