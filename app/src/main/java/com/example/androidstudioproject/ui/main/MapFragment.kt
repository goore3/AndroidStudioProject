package com.example.androidstudioproject.ui.main

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.AsyncTask
import android.os.Bundle
import android.provider.SettingsSlicesContract.KEY_LOCATION
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.androidstudioproject.BuildConfig
import com.example.androidstudioproject.R
import com.example.androidstudioproject.databinding.FragmentMapBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlin.random.Random

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MapFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MapFragment : Fragment(R.layout.fragment_map) {
    private var map: GoogleMap? = null
    private var cameraPosition: CameraPosition? = null

    private var originLatitude: Double = 28.5021359
    private var originLongitude: Double = 77.4054901
    private var destinationLatitude: Double = 28.5151087
    private var destinationLongitude: Double = 77.3932163

    private val PATTERN_GAP_LENGTH_PX = 20
    private val DOT: PatternItem = Dot()
    private val GAP: PatternItem = Gap(PATTERN_GAP_LENGTH_PX.toFloat())
    private val PATTERN_POLYLINE_DOTTED = listOf(GAP, DOT)

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private val defaultLocation = LatLng(-33.8523341, 151.2106085)
    private var locationPermissionGranted = false

    private var lastKnownLocation: Location? = null

    private lateinit var binding: FragmentMapBinding

    private val callback = OnMapReadyCallback { googleMap ->
        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
//        val sydney = LatLng(-34.0, 151.0)
//        googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
        map = googleMap
        // Prompt the user for permission.
        getLocationPermission()
        // Turn on the My Location layer and the related control on the map.
        updateLocationUI()
        // Get the current location of the device and set the position of the map.
        getDeviceLocation()

        val originLocation = LatLng(originLatitude, originLongitude)
        map?.addMarker(MarkerOptions().position(originLocation))
        map?.animateCamera(CameraUpdateFactory.newLatLngZoom(originLocation, 18F))

        val polyline1 = googleMap.addPolyline(PolylineOptions()
            .clickable(true)
            .add(
                LatLng(-35.016, 143.321),
                LatLng(-34.747, 145.592),
                LatLng(-34.364, 147.891),
                LatLng(-33.501, 150.217),
                LatLng(-32.306,149.248),
                LatLng(-32.491, 147.309)
            ))
        polyline1.tag = "First"

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(-23.684, 133.903), 4f))

        googleMap.setOnPolylineClickListener{
            var polyline = it
            if (polyline.pattern == null || !polyline.pattern!!.contains(DOT)) {
                polyline.pattern = PATTERN_POLYLINE_DOTTED
            } else {
                // The default pattern is a solid stroke.
                polyline.pattern = null
            }
            Toast.makeText(requireContext(), "Route type " + polyline.tag.toString(),
                Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), BuildConfig.MAPS_API_KEY)
        }
        binding = FragmentMapBinding.inflate(inflater, container, false)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())



        return binding.root
    }

    fun doubleToLatLngArray(array: ArrayList<ArrayList<Double>>?): ArrayList<LatLng> {
        var newArray = ArrayList<LatLng>()
        if (array != null) {
            for(element in array) {
                newArray.add(LatLng(element[0], element[1]))
            }
        }
        return newArray
    }

    private fun getEventData(mapFragment: SupportMapFragment?) {
        val dbRef = FirebaseDatabase.getInstance().getReference("routes")

        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var routeList = ArrayList<RouteModel>()
                for (childSnapshot in snapshot.children) {
                    val route = childSnapshot.getValue(RouteModel::class.java)
                    routeList.add(route!!)
                }
                mapFragment?.getMapAsync {
                    map = it
                    map?.clear()
                    val rnd = Random
                    for(route in routeList) {
                        var lineOption = PolylineOptions()
                        val color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256))
                        var location = doubleToLatLngArray(route.indices)
                        lineOption.addAll(location)
                        lineOption.width(4f)
                        lineOption.color(color)
                        lineOption.geodesic(true)
                        lineOption.clickable(true)
                        val polyline = map?.addPolyline(lineOption)
                        polyline?.tag = route.name
                        map?.setOnPolylineClickListener {
                            var polyline = it
                            if (polyline.pattern == null || !polyline.pattern!!.contains(DOT)) {
                                polyline.pattern = PATTERN_POLYLINE_DOTTED
                            } else {
                                // The default pattern is a solid stroke.
                                polyline.pattern = null
                            }
                            Toast.makeText(requireContext(), "Route name " + polyline.tag.toString(),
                                Toast.LENGTH_SHORT).show()
                        }
                    }
//                    val originLocation = LatLng(originLatitude, originLongitude)
//                    map?.addMarker(MarkerOptions().position(originLocation))
//                    val destinationLocation = LatLng(destinationLatitude, destinationLongitude)
//                    map?.addMarker(MarkerOptions().position(destinationLocation))
//                    val urll = getDirectionURL(originLocation, destinationLocation, BuildConfig.DIRECTIONS_API_KEY)
//                    GetDirection(urll).execute()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
        getEventData(mapFragment)
        binding.directions.setOnClickListener {
            mapFragment?.getMapAsync {
                map = it
                val originLocation = LatLng(originLatitude, originLongitude)
                map?.addMarker(MarkerOptions().position(originLocation))
                val destinationLocation = LatLng(destinationLatitude, destinationLongitude)
                map?.addMarker(MarkerOptions().position(destinationLocation))
                val urll = getDirectionURL(originLocation, destinationLocation, BuildConfig.DIRECTIONS_API_KEY)
                GetDirection(urll).execute()
                map?.animateCamera(CameraUpdateFactory.newLatLngZoom(originLocation,  14F))
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        map?.let { map ->
            outState.putParcelable(KEY_CAMERA_POSITION, map.cameraPosition)
            outState.putParcelable(KEY_LOCATION, lastKnownLocation)
        }
        super.onSaveInstanceState(outState)
    }


    private fun getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true
        } else {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        locationPermissionGranted = false
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
        updateLocationUI()
    }

    @SuppressLint("MissingPermission")
    private fun updateLocationUI() {
        if (map == null) {
            return
        }
        try {
            if (locationPermissionGranted) {
                map?.isMyLocationEnabled = true
                map?.uiSettings?.isMyLocationButtonEnabled = true
            } else {
                map?.isMyLocationEnabled = false
                map?.uiSettings?.isMyLocationButtonEnabled = false
                lastKnownLocation = null
                getLocationPermission()
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    @SuppressLint("MissingPermission")
    private fun getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (locationPermissionGranted) {
                val locationResult = fusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        // Set the map's camera position to the current location of the device.
                        lastKnownLocation = task.result
                        if (lastKnownLocation != null) {
                            map?.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                LatLng(lastKnownLocation!!.latitude,
                                    lastKnownLocation!!.longitude), DEFAULT_ZOOM.toFloat()))
                        }
                    } else {
                        Log.d(TAG, "Current location is null. Using defaults.")
                        Log.e(TAG, "Exception: %s", task.exception)
                        map?.moveCamera(CameraUpdateFactory
                            .newLatLngZoom(defaultLocation, DEFAULT_ZOOM.toFloat()))
                        map?.uiSettings?.isMyLocationButtonEnabled = false
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    private fun getDirectionURL(origin:LatLng, dest:LatLng, secret: String) : String{
        return "https://maps.googleapis.com/maps/api/directions/json?origin=${origin.latitude},${origin.longitude}" + "&destination=${dest.latitude},${dest.longitude}" + "&sensor=false" + "&mode=driving" + "&key=$secret"
    }


    companion object {
        private const val TAG = "ELO"
        private const val DEFAULT_ZOOM = 15
        private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1

        // Keys for storing activity state.
        private const val KEY_CAMERA_POSITION = "camera_position"
        private const val KEY_LOCATION = "location"

        // Used for selecting the current place.
        private const val M_MAX_ENTRIES = 5

    }

    fun decodePolyline(encoded: String): List<LatLng> {
        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0
        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat
            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng
            val latLng = LatLng((lat.toDouble() / 1E5),(lng.toDouble() / 1E5))
            poly.add(latLng)
        }
        return poly
    }

    @SuppressLint("StaticFieldLeak")
    private inner class GetDirection(val url : String) : AsyncTask<Void, Void, List<List<LatLng>>>(){
        override fun doInBackground(vararg params: Void?): List<List<LatLng>> {
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val data = response.body!!.string()
            val result =  ArrayList<List<LatLng>>()
            try {
                val respObj = Gson().fromJson(data,MapData::class.java)
                val path =  ArrayList<LatLng>()
                for (i in 0 until respObj.routes[0].legs[0].steps.size) {
                    path.addAll(decodePolyline(respObj.routes[0].legs[0].steps[i].polyline.points))
                }
                result.add(path)
            } catch (e:Exception) {
                e.printStackTrace()
            }
            return result
        }

        override fun onPostExecute(result: List<List<LatLng>>) {
            val lineoption = PolylineOptions()
            for (i in result.indices){
                lineoption.addAll(result[i])
                lineoption.width(10f)
                lineoption.color(Color.GREEN)
                lineoption.geodesic(true)
                }
            map?.addPolyline(lineoption.clickable(true))
        }
    }
}