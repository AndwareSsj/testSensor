package com.skipper.testsensor

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.util.SparseArray
import com.skipper.testsensor.data.HookSensorEvent
import com.skipper.testsensor.thread.SensorThread
import de.robv.android.xposed.*
import de.robv.android.xposed.callbacks.XC_LoadPackage
import fi.iki.elonen.NanoHTTPD.newFixedLengthResponse

class SensorHook : IXposedHookLoadPackage, IXposedHookZygoteInit {
    companion object {
        val sensorEventCache: HashMap<String, HookSensorEvent> = HashMap()
        val contextCache:HashMap<String, Context> = HashMap()
        private val threadCache = mutableMapOf<String, SensorThread?>()
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam?) {
        lpparam?.apply {
            XposedHelpers.findAndHookMethod(Application::class.java, "attach", Context::class.java, object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam?) {
                    super.afterHookedMethod(param)
                    val context = param!!.args[0] as Context
                    contextCache[lpparam.packageName] = context
                }
            })
            val sensorEL = XposedHelpers.findClass(
                    "android.hardware.SystemSensorManager\$SensorEventQueue",
                    this.classLoader
            )
            XposedBridge.hookAllMethods(sensorEL, "addSensorEvent", object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam?) {
                    super.afterHookedMethod(param)
                    val sensorEvent = param?.let {
                        val sensor = it.args[0] as Sensor

                        val field = it.thisObject.javaClass.getDeclaredField("mSensorsEvents")
                        field.isAccessible = true
                        val handleField = sensor.javaClass.getDeclaredField("mHandle")
                        handleField.isAccessible = true
                        val handle = handleField[sensor] as Int
                        val sEvent = (field[it.thisObject] as SparseArray<SensorEvent>)[handle]
                        sEvent.sensor = sensor
                        val mListenerField = it.thisObject.javaClass.getDeclaredField("mListener")
                        mListenerField.isAccessible = true
                        val mListener = mListenerField[it.thisObject] as SensorEventListener
                        HookSensorEvent(mListener, sEvent)
                    }
                    sensorEvent?.apply {
                        if (this.sensorEvent.sensor.type == Sensor.TYPE_STEP_COUNTER) {
                            sensorEventCache[lpparam.packageName] = this
                            XposedBridge.log("传感器初始化成功!")
                        }
                    }
                }
            })
        }
    }

    fun startThread(appId: String) {
        val sensorThread = SensorThread(appId)
        sensorThread.start()
        threadCache[appId] = sensorThread
        XposedBridge.log("线程启动成功!")
    }

    fun pauseThread(appId: String) {
        threadCache[appId]?.toPause()
    }

    override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam?) {
//        val server = AsyncHttpServer()
//        server.post("/start") { request, response ->
//            if (request.body is UrlEncodedFormBody) {
//                val formBody = request.body as UrlEncodedFormBody
//                val map = formBody.get()
//                val appId = map["appId"] as String
//                startThread(appId)
//                response.send("ok:$appId\n")
//            }
//        }
//        server.post("/pause") { request, response ->
//            if (request.body is UrlEncodedFormBody) {
//                val formBody = request.body as UrlEncodedFormBody
//                val map = formBody.get()
//                val appId = map["appId"] as String
//                pauseThread(appId)
//                response.send("ok")
//            }
//        }
//        server.listen(5000)

        val server = HookHttpServer(5000) { path, session ->

            when(path) {
                "/start" -> {
                    val params = session.parms
                    val appId = params["appId"]
                    startThread(appId ?: "com.tencent.mm")
                }
                "/pause" -> {

                }
            }
            newFixedLengthResponse("ok")
        }
        server.start()
    }
}