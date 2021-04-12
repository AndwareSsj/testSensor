package com.skipper.testsensor.thread

import android.content.Context
import android.os.SystemClock
import com.skipper.testsensor.SensorHook
import de.robv.android.xposed.XposedBridge

class SensorThread(private val appId: String) : Thread() {
    private val obj = Object()
    private var suspend = false
    private val stepD = 2


    private val stepCount = object : ThreadLocal<Float>() {
        override fun initialValue(): Float {
            return 1f
        }
    }

    override fun run() {
        super.run()
        while (!isInterrupted) {
            synchronized(obj) {
                while (suspend) {
                    try {
                        obj.wait()
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                }
                val mEvent = SensorHook.sensorEventCache[appId]
                mEvent?.apply {
                    val sEvent = this.sensorEvent
                    val count = stepCount.get()?:1f
                    // Copy from the values array.
                    val timestamp = System.nanoTime()
                    sEvent.accuracy = 3
                    sEvent.timestamp = timestamp

                    val step = floatArrayOf(sEvent.values[0]+10)
                    System.arraycopy(step, 0, sEvent.values, 0, 1)
                    XposedBridge.log("step count: $count, step: ${step[0]}")
                    this.listener.onSensorChanged(sEvent)
                    stepCount.set(count+1)
                }
                sleep(6000)
            }
        }
    }

    @Synchronized
    fun toPause() {
        this.suspend = true
    }

    @Synchronized
    fun toResume() {
        this.suspend = false
        obj.notifyAll()
    }

    fun destory () {
        stepCount.remove()
    }
}