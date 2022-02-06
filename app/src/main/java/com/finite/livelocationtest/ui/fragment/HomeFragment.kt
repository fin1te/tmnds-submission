package com.finite.livelocationtest.ui.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.content.IntentSender
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import com.finite.livelocationtest.R
import com.finite.livelocationtest.databinding.FragmentHomeBinding
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

class HomeFragment : Fragment(), EasyPermissions.PermissionCallbacks, OnMapReadyCallback {

    companion object {
        const val PERMISSION_LOCATION_REQUEST_CODE = 1
    }

    private var binding : FragmentHomeBinding? = null
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var myMap : GoogleMap
    private val location = LatLng(18.989401, 73.117516)

    @SuppressLint("MissingPermission")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val fragmentBinding = FragmentHomeBinding.inflate(inflater, container, false)
        binding = fragmentBinding

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())

        if(!hasLocationPermission()) {
            requestLocationPermission()
        }

        val fragmentManager : FragmentManager = childFragmentManager

        val mapFragment = fragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


        binding!!.fab.setOnClickListener {

                fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
                    Log.d("FirstFragment", "OnSuccessListener")

                    if(location!=null) {
                        updateMap(location.latitude,location.longitude)
                        Log.d("FirstFragment", "Latitude" + location.latitude.toString() + " : Longitude " + location.longitude.toString())
                        Toast.makeText(context, "${location.latitude} : ${location.longitude}", Toast.LENGTH_LONG).show()
                    } else {
                        Log.d("FirstFragment", "Reached Else")
                        createLocationRequest()
                    }

                }.addOnFailureListener {
                    Log.d("FirstFragment", "OnFailureListener")
                }

        }

        return fragmentBinding.root
    }


    @SuppressLint("MissingPermission")
    private fun createLocationRequest() {

        val locationRequest : LocationRequest = LocationRequest.create().apply {
            interval = 100
            fastestInterval = 50
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            maxWaitTime= 100
        }

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        val client: SettingsClient = LocationServices.getSettingsClient(this.requireContext())
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener { locationSettingsResponse ->
            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
                if(location!=null) {
                    updateMap(location.latitude,location.longitude)
                    Log.d("FirstFragment", "Latitude" + location.latitude.toString() + " : Longitude " + location.longitude.toString())
                    Toast.makeText(context, "${location.latitude} : ${location.longitude}", Toast.LENGTH_LONG).show()
                }
            }
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException){
                try {
                    exception.startResolutionForResult(this.requireActivity(),
                        1)
                } catch (sendEx: IntentSender.SendIntentException) {

                }
            }
        }
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

    override fun onMapReady(p0: GoogleMap) {
        myMap = p0

        myMap.addMarker(
            MarkerOptions()
            .position(location))

        myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location,15f))
    }

    private fun updateMap(lat: Double, lon: Double) {
        myMap.clear()
        val latLong = LatLng(lat,lon)
        myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLong,15f))
        myMap.addMarker(
            MarkerOptions()
                .position(latLong))
    }
}