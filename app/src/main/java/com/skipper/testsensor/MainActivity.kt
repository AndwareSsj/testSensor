package com.skipper.testsensor

import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.lang.Exception

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startBt.setOnClickListener {
            GlobalScope.launch(Dispatchers.Main) {
                withContext(Dispatchers.IO) {
                    val client = OkHttpClient.Builder()
                        .build()
                    val body = FormBody.Builder()
                        .add("appId","com.tencent.mm")
                        .build()
                    val request = Request.Builder()
                        .url("http://127.0.0.1:5000/start")
                        .post(body)
                        .build()
                    try {
                        client.newCall(request).execute()
                    }catch (e:Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        pauseBt.setOnClickListener {
            GlobalScope.launch(Dispatchers.Main) {
                withContext(Dispatchers.IO) {
                    val client = OkHttpClient.Builder()
                        .build()
                    val body = FormBody.Builder()
                        .add("appId","com.tencent.mm")
                        .build()
                    val request = Request.Builder()
                        .url("http://127.0.0.1:5000/pause")
                        .method("POST", body)
                        .build()
                    client.newCall(request).execute()
                }
            }
        }
    }
}