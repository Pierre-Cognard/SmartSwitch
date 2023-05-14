package com.example.smartswitch

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.BatteryManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.*
import android.os.Handler
import android.os.Looper
import android.widget.Button

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

        val useWifiButton = findViewById<Button>(R.id.useWifiButton)
        useWifiButton.setOnClickListener {
            getSpecificNetwork(this, NetworkCapabilities.TRANSPORT_WIFI) { network ->
                forceUseSpecificNetwork(this, network)
            }
        }

        val useCellularButton = findViewById<Button>(R.id.useCellularButton)
        useCellularButton.setOnClickListener {
            getSpecificNetwork(this, NetworkCapabilities.TRANSPORT_CELLULAR) { network ->
                forceUseSpecificNetwork(this, network)
            }
        }

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

    fun getSpecificNetwork(context: Context, transportType: Int, callback: (Network?) -> Unit) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkRequest = NetworkRequest.Builder()
            .addTransportType(transportType)
            .build()

        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                callback(network)
                connectivityManager.unregisterNetworkCallback(this)
            }

            override fun onUnavailable() {
                super.onUnavailable()
                callback(null)
                connectivityManager.unregisterNetworkCallback(this)
            }
        }

        connectivityManager.requestNetwork(networkRequest, networkCallback)
    }

    fun forceUseSpecificNetwork(context: Context, network: Network?) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.bindProcessToNetwork(network)
    }
}

