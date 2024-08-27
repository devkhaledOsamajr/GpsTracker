package com.example.gpstracker

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.gpstracker.databinding.ActivityMainBinding
import com.google.android.gms.location.*

class MainActivity : AppCompatActivity() {

    private val requestGpsPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        Log.d("GPS_TRACKER", "Permissions result: $permissions")
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            getUserLocation()
        } else {
            Log.e("GPS_TRACKER", "Location permissions denied.")
        }
    }

    private lateinit var fusedLocationProvider: FusedLocationProviderClient
    private lateinit var binding: ActivityMainBinding

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.locations.forEach { location ->
                Log.e("GPS_TRACKER", "longitude: ${location.longitude}")
                Log.e("GPS_TRACKER", "latitude: ${location.latitude}")
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getUserLocation() {
        Log.d("GPS_TRACKER", "Requesting user location...")
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 8000).build()
        val currentLocationRequest = CurrentLocationRequest.Builder()
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .build()

        fusedLocationProvider.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )

        fusedLocationProvider.getCurrentLocation(currentLocationRequest, null)
            .addOnSuccessListener { location ->
                if (location != null) {
                    Log.e(
                        "GPS_TRACKER",
                        "Current location obtained: ${location.longitude}, ${location.latitude}"
                    )
                } else {
                    Log.e("GPS_TRACKER", "Current location is null.")
                }
            }
            .addOnFailureListener { e ->
                Log.e("GPS_TRACKER", "Failed to get current location: ${e.message}")
            }
    }

    private fun isGpsPermissionAllowed(requestedPermission: String): Boolean {
        val isGranted = ContextCompat.checkSelfPermission(
            this,
            requestedPermission
        ) == PackageManager.PERMISSION_GRANTED
        Log.d("GPS_TRACKER", "$requestedPermission is granted: $isGranted")
        return isGranted
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fusedLocationProvider = LocationServices.getFusedLocationProviderClient(this)

        if (isGpsPermissionAllowed(Manifest.permission.ACCESS_COARSE_LOCATION) ||
            isGpsPermissionAllowed(Manifest.permission.ACCESS_FINE_LOCATION)
        ) {
            getUserLocation()
        } else if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) ||
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)
        ) {
            showDialog(
                title = "We need to access Location",
                postiveText = "OK",
                negativeText = "No",
                onPostiveClickListner = { dialog, _ ->
                    dialog?.dismiss()
                    requestGpsPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                },
                onNegativeClickListner = { dialog, _ ->
                    dialog?.dismiss()
                    Log.e("GPS_TRACKER", "Location permissions denied by user.")
                }
            )
        } else {
            requestGpsPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationProvider.removeLocationUpdates(locationCallback)
    }
}
