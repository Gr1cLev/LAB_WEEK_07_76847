package com.example.lab_week_07_76847

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AlertDialog

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.lab_week_07_76847.databinding.ActivityMapsBinding
import com.google.android.gms.location.LocationServices

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    // Variable untuk permission request
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    //A google play location service which helps us interact with Google's Fused Location Provider API
    //The API intelligently provides us with the device location information
    private val fusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Register for activity result (permission request)
        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    // Permission diberikan
                    getLastLocation()
                } else {
                    // Permission ditolak, tampilkan rationale
                    showPermissionRationale {
                        requestPermissionLauncher.launch(ACCESS_FINE_LOCATION)
                    }
                }
            }
    }

    /**
     * Manipulates the map once available.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Cek permission sebelum gunakan lokasi
        when {
            hasLocationPermission() -> getLastLocation()
            shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION) -> {
                showPermissionRationale {
                    requestPermissionLauncher.launch(ACCESS_FINE_LOCATION)
                }
            }
            else -> requestPermissionLauncher.launch(ACCESS_FINE_LOCATION)
        }

        // Tambah marker default (Sydney)
        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }

    //Executed when the location permission has been granted by the user
    private fun getLastLocation() {
        if (hasLocationPermission()) {
            try {
                fusedLocationProviderClient.lastLocation
                    .addOnSuccessListener { location: Location? ->
                        location?.let {
                            val userLocation = LatLng(location.latitude,
                                location.longitude)
                            updateMapLocation(userLocation)
                            addMarkerAtLocation(userLocation, "You")
                        }
                    }
            } catch (e: SecurityException) {
                Log.e("MapsActivity", "SecurityException: ${e.message}")
            }
        } else {
            // If permission was rejected
            requestPermissionLauncher.launch(ACCESS_FINE_LOCATION)
        }
    }


    private fun updateMapLocation(location: LatLng) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
            location, 7f))
    }
    private fun addMarkerAtLocation(location: LatLng, title: String) {
        mMap.addMarker(MarkerOptions().title(title)
            .position(location))
    }

    // Check permission
    private fun hasLocationPermission() =
        ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED

    // Rationale dialog
    private fun showPermissionRationale(positiveAction: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle("Location permission")
            .setMessage("This app will not work without knowing your current location")
            .setPositiveButton(android.R.string.ok) { _, _ -> positiveAction() }
            .setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }
}
