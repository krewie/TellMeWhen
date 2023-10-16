package com.krewie.tellmewhen

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Rect
import android.location.GnssStatus
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.krewie.tellmewhen.databinding.ActivityMainBinding
import com.krewie.tellmewhen.ui.theme.TellMeWhenTheme
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay


interface PermissionResultListener {
    fun onPermissionResult(
        requestCode: Int,
        permissions: Array<String?>?, grantResults: IntArray?
    )
}

class MainActivity : ComponentActivity(), PermissionResultListener {

    private val PERMISSION_REQUEST_CODE = 1;
    lateinit var mMap: MapView
    lateinit var controller: IMapController
    lateinit var mMyLocationOverlay: MyLocationNewOverlay
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    lateinit var mReceiver: MapEventsReceiver

    val mapEventsReceiver = MapsEventReceiverImpl()
    val mapEventsOverlay = MapEventsOverlay(mapEventsReceiver)

    override fun onCreate(savedInstanceState: Bundle?) {

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return requestPermissions(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ), PERMISSION_REQUEST_CODE)
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)

        setContent {
            TellMeWhenTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    binding.root
                }
            }
        }

        setContentView(binding.root)

        Configuration.getInstance().load(
            applicationContext,
            getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE)
        )
        mMap = binding.osmmap
        mMap.setTileSource(TileSourceFactory.MAPNIK)
        mMap.mapCenter
        mMap.setMultiTouchControls(true)
        mMap.getLocalVisibleRect(Rect())


        mMyLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(this), mMap)
        controller = mMap.controller

        mMyLocationOverlay.enableMyLocation()
        mMyLocationOverlay.enableFollowLocation()
        mMyLocationOverlay.isDrawAccuracyEnabled = true
        mMyLocationOverlay.runOnFirstFix {
            runOnUiThread {
                controller.setCenter(mMyLocationOverlay.myLocation)
                controller.animateTo(mMyLocationOverlay.myLocation)
            }
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location : Location? ->
                // Got last known location. In some rare situations this can be null.
                //KISTA
                val defaultCoord = GeoPoint(59.40584150174974, 17.942154618822755)
                location?.let {
                    controller.animateTo(
                        GeoPoint(location.latitude, location.longitude)
                    )
                } ?: run {
                    controller.animateTo(defaultCoord)
                }
            }

        controller.setZoom(18.0)

        Log.e("TAG", "onCreate:in ${controller.zoomIn()}")
        Log.e("TAG", "onCreate: out  ${controller.zoomOut()}")

        mMap.overlays.add(mMyLocationOverlay)
        mMap.addMapListener(mapEventsReceiver)
        mMap.overlays.add(mapEventsOverlay)
    }


    val mStatusCallback: GnssStatus.Callback = object : GnssStatus.Callback() {
        override fun onStarted() {}
        override fun onStopped() {}
        override fun onFirstFix(ttffMillis: Int) {}
        override fun onSatelliteStatusChanged(status: GnssStatus) {
            Log.e("onGpsStatusChanged", "Status Change!");
            Log.v("TAG", "GNSS Status: " + status.satelliteCount + " satellites.")
        }
    }

    override fun onPermissionResult(
        requestCode: Int,
        permissions: Array<String?>?,
        grantResults: IntArray?
    ) {
        Log.e("permissionResult", "what happend");
    }
}