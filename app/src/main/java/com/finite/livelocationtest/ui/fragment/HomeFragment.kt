package com.finite.livelocationtest.ui.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.content.IntentSender
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.finite.livelocationtest.R
import com.finite.livelocationtest.adapter.LocationAdapter
import com.finite.livelocationtest.databinding.FragmentHomeBinding
import com.finite.livelocationtest.model.LocationModel
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Task
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class HomeFragment : Fragment(), EasyPermissions.PermissionCallbacks, OnMapReadyCallback {

    /** Contains the request used code for location permission */
    companion object {
        const val PERMISSION_LOCATION_REQUEST_CODE = 1
    }

    private var binding: FragmentHomeBinding? = null

    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    /** Location & Map Initializations */
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var myMap: GoogleMap
    private val location = LatLng(18.989401, 73.117516)

    /** RecyclerView Initializations */
    private lateinit var recView: RecyclerView
    private lateinit var dataList: ArrayList<LocationModel>
    private lateinit var locationAdapter: LocationAdapter

    @SuppressLint("MissingPermission")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val fragmentBinding = FragmentHomeBinding.inflate(inflater, container, false)
        binding = fragmentBinding

        /** RecyclerView, Adapter and Model connections */
        dataList = ArrayList()
        recView = binding!!.recView
        locationAdapter = LocationAdapter(this.requireContext(), dataList)
        recView.layoutManager = LinearLayoutManager(this.requireContext())
        recView.adapter = locationAdapter

        /** Initialized FLPC */
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext()) // Test111
        locationCallback = object : LocationCallback() {

            override fun onLocationResult(locationResult: LocationResult) {
                locationResult ?: return
                for (location in locationResult.locations){

                    val timestamp: String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val current = LocalDateTime.now()
                        val formatter = DateTimeFormatter.ofPattern("dd/MM/yy  HH:mm:ss.SSS")
                        current.format(formatter).toString()
                    } else {
                        val currentDate: String =
                            SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(Date())
                        val currentTime =
                            SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date())
                        "$currentDate  $currentTime"
                    }

                    updateMap(location.latitude,location.longitude)
                    dataList.add(
                        0,
                        LocationModel(
                            location.latitude.toString(),
                            location.longitude.toString(),
                            timestamp
                        )
                    )
                    locationAdapter.notifyDataSetChanged()
                }
            }

        }

        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireContext())

        /** Requests for Location Perms if not given */
        if (!hasLocationPermission()) {
            requestLocationPermission()
        }



        val fragmentManager: FragmentManager = childFragmentManager

        val mapFragment = fragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding!!.fab.setOnClickListener {
            createLocationRequest()
        }
        return fragmentBinding.root
    }

    /** Creates a Request to Switch Location On and Update the Data */
    @SuppressLint("MissingPermission")
    private fun createLocationRequest() {

        val locationRequest: LocationRequest = LocationRequest.create().apply {
            interval = 100
            fastestInterval = 50
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            maxWaitTime = 100
            smallestDisplacement = 1f
        }

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        val client: SettingsClient = LocationServices.getSettingsClient(this.requireContext())
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener { locationSettingsResponse ->


            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {

                    val timestamp: String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val current = LocalDateTime.now()
                        val formatter = DateTimeFormatter.ofPattern("dd/MM/yy  HH:mm:ss.SSS")
                        current.format(formatter).toString()
                    } else {
                        val currentDate: String =
                            SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(Date())
                        val currentTime =
                            SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date())
                        "$currentDate  $currentTime"
                    }

                    updateMap(location.latitude, location.longitude)
                    dataList.add(
                        0,
                        LocationModel(
                            location.latitude.toString(),
                            location.longitude.toString(),
                            timestamp
                        )
                    )
                    locationAdapter.notifyDataSetChanged()
                    Log.d(
                        "FirstFragment",
                        "Latitude" + location.latitude.toString() + " : Longitude " + location.longitude.toString()
                    )
                    Toast.makeText(
                        context,
                        "${location.accuracy}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    exception.startResolutionForResult(
                        this.requireActivity(),
                        1
                    )
                } catch (sendEx: IntentSender.SendIntentException) {

                }
            }
        }

        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )

    }

    private fun hasLocationPermission() =
        EasyPermissions.hasPermissions(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        )

    private fun requestLocationPermission() {
        EasyPermissions.requestPermissions(
            this,
            "This application cannot work without Location Permission.",
            PERMISSION_LOCATION_REQUEST_CODE,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            SettingsDialog.Builder(requireActivity()).build().show()
        } else {
            requestLocationPermission()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        Toast.makeText(
            requireContext(),
            "Permission Granted!",
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    /** Initialises the Map with Default Data*/
    override fun onMapReady(p0: GoogleMap) {
        myMap = p0

        myMap.addMarker(
            MarkerOptions()
                .position(location)
        )
        myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
    }

    /** Updates the map with current location when this method is called */
    private fun updateMap(lat: Double, lon: Double) {
        myMap.clear()
        val latLong = LatLng(lat, lon)
        myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLong, 17f))
        myMap.addMarker(
            MarkerOptions()
                .position(latLong)
        )
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}