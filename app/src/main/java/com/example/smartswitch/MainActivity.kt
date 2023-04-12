package com.example.smartswitch

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.*
import android.os.Handler
import android.os.Looper

class MainActivity : AppCompatActivity() {

    private lateinit var currentTimeTextView: TextView
    private val handler = Handler(Looper.getMainLooper())
    private val updateTimeRunnable = object : Runnable {
        override fun run() {
            currentTimeTextView.text = getCurrentTime()
            handler.postDelayed(this, 1000) // Mettre à jour chaque seconde (1000 ms)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val batteryLevel = getBatteryLevel(this)
        val batteryLevelTextView = findViewById<TextView>(R.id.batterie)
        batteryLevelTextView.text = "$batteryLevel%"

        currentTimeTextView = findViewById(R.id.heure)
        handler.post(updateTimeRunnable)

    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateTimeRunnable)
    }

    fun getBatteryLevel(context: Context): Float {
        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { ifilter ->
            context.registerReceiver(null, ifilter)
        }
        val batteryLevel: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val batteryScale: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1

        return if (batteryLevel >= 0 && batteryScale > 0) {
            batteryLevel.toFloat() / batteryScale.toFloat() * 100f
        } else {
            -1f
        }
    }

    private fun getCurrentTime(): String {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }
}

