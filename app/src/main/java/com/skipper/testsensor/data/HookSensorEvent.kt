package com.skipper.testsensor.data

import android.hardware.SensorEvent
import android.hardware.SensorEventListener

data class HookSensorEvent(val listener: SensorEventListener, val sensorEvent: SensorEvent)