package com.example.smartswitch

import android.app.AlertDialog
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
import android.Manifest
import android.content.BroadcastReceiver
import android.content.pm.PackageManager
import android.location.LocationManager
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Switch
import androidx.core.app.ActivityCompat
import com.example.smartswitch.Horaires.puissances
import com.example.smartswitch.Horaires.ranges
import com.example.smartswitch.Zones.dictionnaireZones
import com.example.smartswitch.Zones.geometryFactory
import com.google.android.gms.location.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import org.locationtech.jts.geom.Coordinate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    var currentPosition = 0
    var currentService = 0

    private lateinit var currentTimeTextView: TextView
    private val handler = Handler(Looper.getMainLooper())
    private val updateTimeRunnable = object : Runnable {
        override fun run() {
            val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
            currentTimeTextView.text = LocalTime.now().format(formatter)
            handler.postDelayed(this, 1000) // Mettre à jour chaque seconde (1000 ms)
        }
    }
    private lateinit var batteryLevelTextView: TextView
    
    private val updateBatteryRunnable = object : Runnable {
        override fun run() {
            val batteryLevel = getBatteryLevel()
            batteryLevelTextView.text = "$batteryLevel%"
            handler.postDelayed(this, 1000) // Mettre à jour chaque minute
        }
    }

    private lateinit var reseau: TextView
    private val updateReseauRunnable = object : Runnable {
        override fun run() {
            val batteryLevel = getBatteryLevel()
            if (batteryLevel >= 20 || isCharging()){
                System.out.println("Batterie OK")
                if (currentService == 1){
                    System.out.println("Service gourmant")

                    //val currentTime = LocalTime.now()
                    val currentTime = LocalTime.of(20, 30)

                    val matchingRange = ranges.firstOrNull { !currentTime.isBefore(it.first) && !currentTime.isAfter(it.second) }

                    if (matchingRange != null) {
                        val (ratioStart, ratioEnd) = calculateRatios(currentTime, matchingRange!!.first, matchingRange.second)
                        println("L'heure actuelle (${currentTime}) est entre ${matchingRange.first} ($ratioStart) et ${matchingRange.second} ($ratioEnd).")

                        if (currentPosition != -1){
                            val firstValue = puissances[matchingRange.first]?.get(currentPosition)?.times(ratioStart)
                            val secondValue = puissances[matchingRange.second]?.get(currentPosition)?.times(ratioEnd)

                            val result = firstValue?.plus(secondValue!!)

                            System.out.println("Résultat: $firstValue * $secondValue = $result")
                        }

                    } else {
                        println("L'heure actuelle (${currentTime}) ne se trouve dans aucune des plages horaires définies.")
                    }
                }
            }
            else{
                System.out.println("Batterie pas OK")
            }
            handler.postDelayed(this, 5000) // Mettre à jour chaque minute
        }
    }
    private lateinit var chargingImageView: ImageView
    private val chargingReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val status: Int = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            val isCharging: Boolean = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
            chargingImageView.visibility = if (isCharging) View.VISIBLE else View.INVISIBLE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val useWifiButton = findViewById<Button>(R.id.useWifiButton)
        val useCellularButton = findViewById<Button>(R.id.useCellularButton)
        val GPSButton = findViewById<Button>(R.id.GPS_Button)
        val serviceButton = findViewById<Button>(R.id.Service_Button)
        val mySwitch = findViewById<Switch>(R.id.switch1)

        chargingImageView = findViewById(R.id.charging)

        batteryLevelTextView = findViewById(R.id.batterie)
        handler.post(updateBatteryRunnable)

        currentTimeTextView = findViewById(R.id.heure)
        handler.post(updateTimeRunnable)

        reseau = findViewById(R.id.reseau)
        handler.post(updateReseauRunnable)

        useWifiButton.setOnClickListener {
            getSpecificNetwork(this, NetworkCapabilities.TRANSPORT_WIFI) { network ->
                forceUseSpecificNetwork(this, network)
            }
        }

        useCellularButton.setOnClickListener {
            getSpecificNetwork(this, NetworkCapabilities.TRANSPORT_CELLULAR) { network ->
                forceUseSpecificNetwork(this, network)
            }
        }

        //mySwitch.isChecked = true
        //GPSButton.isEnabled = false
        mySwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Le switch est en position "On".
                Log.d("Switch", "Le switch est en position On")
                GPSTest()
                startLocationUpdates()
                GPSButton.isEnabled = false
            } else {
                // Le switch est en position "Off".
                Log.d("Switch", "Le switch est en position Off")
                stopLocationUpdates()
                GPSButton.isEnabled = true
            }
        }

        GPSButton.setOnClickListener {
            val mBuilder = AlertDialog.Builder(this)
            mBuilder.setTitle("Position GPS")
            mBuilder.setSingleChoiceItems(R.array.positionsGPS, currentPosition) { dialog, language ->
                when(language){
                    0 -> setPosition(0)
                    1 -> setPosition(1)
                    2 -> setPosition(2)
                    3 -> setPosition(3)
                    4 -> setPosition(4)
                    5 -> setPosition(5)
                    6 -> setPosition(6)
                    7 -> setPosition(7)
                    8 -> setPosition(8)
                    9 -> setPosition(9)
                }
                dialog.dismiss()
            }
            val mDialog = mBuilder.create()
            mDialog.show()
        }

        serviceButton.setOnClickListener {
            val mBuilder = AlertDialog.Builder(this)
            mBuilder.setTitle("Service")
            mBuilder.setSingleChoiceItems(R.array.services, currentService) { dialog, language ->
                when(language){
                    0 -> setService(0)
                    1 -> setService(1)
                    2 -> setService(2)
                }
                dialog.dismiss()
            }
            val mDialog = mBuilder.create()
            mDialog.show()
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                p0 ?: return
                for (location in p0.locations){
                    // Mise à jour de la position
                    Log.d("position", "Latitude : ${location.latitude}, Longitude : ${location.longitude}")
                    val userLocation = geometryFactory.createPoint(Coordinate(location.longitude, location.latitude))

                    // test de la position
                    val matchingZone = dictionnaireZones.entries.firstOrNull { it.value.contains(userLocation) }

                    if (matchingZone != null) {
                        val zone = resources.getStringArray(R.array.positionsGPS)[matchingZone.key]
                        Log.d("position", "L'utilisateur est dans la zone : $zone")
                        setPosition(matchingZone.key)
                    } else {
                        val position = findViewById<TextView>(R.id.positionTextView)
                        position.text = "Zone inconnue"
                        currentPosition = -1
                    }
                }
            }
        }

        //GPSTest()
        //startLocationUpdates()

        setPosition(0)
        setService(1)





    }

    companion object {
        private const val REQUEST_LOCATION_PERMISSION_CODE = 1
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateTimeRunnable)
    }

    override fun onResume() {
        super.onResume()
        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        registerReceiver(chargingReceiver, intentFilter)
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(chargingReceiver)
    }

    private fun GPSTest(){
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Log.d("GPS Status", "GPS est désactivé")
            val position = findViewById<TextView>(R.id.positionTextView)
            position.text = "GPS désactivé"
            currentPosition = -1
            AlertDialog.Builder(this)
                .setMessage("Le GPS est désactivé. Voulez-vous l'activer?")
                .setPositiveButton("Oui") { _, _ ->
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
                .setNegativeButton("Non", null)
                .show()
        }
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.create().apply {
            interval = 10000 // Interval de mise à jour en millisecondes.
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        // check de la permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION_CODE)
            return
        }
        // Commencez à recevoir des mises à jour de la position
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    fun getBatteryLevel(): Float {
        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { ifilter ->
            this.registerReceiver(null, ifilter)
        }
        val batteryLevel: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val batteryScale: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1

        return if (batteryLevel >= 0 && batteryScale > 0) {
            batteryLevel.toFloat() / batteryScale.toFloat() * 100f
        } else {
            -1f
        }
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
        //connectivityManager.bindProcessToNetwork(network)
    }

    private fun setPosition(pos: Int){
        val position = findViewById<TextView>(R.id.positionTextView)
        currentPosition = pos
        //System.out.println(resources.getStringArray(R.array.positionsGPS)[pos])
        position.text = resources.getStringArray(R.array.positionsGPS)[pos]
    }

    private fun setService(serv: Int){
        val service = findViewById<TextView>(R.id.serviceTextView)
        currentService = serv
        System.out.println(resources.getStringArray(R.array.services)[serv])
        service.text = resources.getStringArray(R.array.services)[serv]
    }

    private fun isCharging(): Boolean {
        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { ifilter ->
            this.registerReceiver(null, ifilter)
        }
        val status: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        return status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
    }

    fun calculateRatios(currentTime: LocalTime, startTime: LocalTime, endTime: LocalTime): Pair<Double, Double> {
        val totalMinutes = ChronoUnit.MINUTES.between(startTime, endTime).toDouble()
        val elapsedMinutes = ChronoUnit.MINUTES.between(startTime, currentTime).toDouble()

        val ratioStart = (totalMinutes - elapsedMinutes) / totalMinutes
        val ratioEnd = elapsedMinutes / totalMinutes

        return Pair(ratioStart, ratioEnd)
    }
}

