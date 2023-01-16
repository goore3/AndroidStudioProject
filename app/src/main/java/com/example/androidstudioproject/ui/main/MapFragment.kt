package com.example.androidstudioproject.ui.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.widget.Toast.makeText
import androidx.appcompat.content.res.AppCompatResources
import androidx.databinding.DataBindingUtil.setContentView
import com.example.androidstudioproject.R
import com.example.androidstudioproject.databinding.FragmentMapBinding
import com.google.android.material.snackbar.Snackbar
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.maps.*
import com.mapbox.maps.extension.style.expressions.dsl.generated.interpolate
import com.mapbox.maps.extension.style.expressions.generated.Expression.Companion.linear
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.Plugin
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorBearingChangedListener
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.viewport.data.FollowPuckViewportStateBearing
import com.mapbox.maps.plugin.viewport.data.FollowPuckViewportStateOptions
import com.mapbox.maps.plugin.viewport.state.FollowPuckViewportState
import com.mapbox.maps.plugin.viewport.viewport
import com.mapbox.maps.testapp.utils.LocationPermissionHelper
import java.lang.ref.WeakReference
import com.mapbox.maps.extension.style.layers.generated.lineLayer
import com.mapbox.maps.extension.style.layers.properties.generated.LineCap
import com.mapbox.maps.extension.style.layers.properties.generated.LineJoin
import com.mapbox.maps.extension.style.style

/**
 * A simple [Fragment] subclass.
 * Use the [MapFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MapFragment : Fragment(R.layout.fragment_map) {
    private lateinit var mapView: MapView
    private lateinit var map: MapboxMap
    private lateinit var binding: FragmentMapBinding

    private lateinit var locationPermissionHelper: LocationPermissionHelper

    private val onIndicatorBearingChangedListener = OnIndicatorBearingChangedListener {
        mapView.getMapboxMap().setCamera(CameraOptions.Builder().bearing(it).build())
    }

    private val onIndicatorPositionChangedListener = OnIndicatorPositionChangedListener {
        mapView.getMapboxMap().setCamera(CameraOptions.Builder().center(it).build())
        mapView.gestures.focalPoint = mapView.getMapboxMap().pixelForCoordinate(it)
    }


    private val onMoveListener = object : OnMoveListener {
        override fun onMoveBegin(detector: MoveGestureDetector) {
           // onCameraTrackingDismissed()
        }

        override fun onMove(detector: MoveGestureDetector): Boolean {
            return false
        }

        override fun onMoveEnd(detector: MoveGestureDetector) {}
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentMapBinding.inflate(inflater, container, false)
        mapView = binding.mapView
        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS)

        mapView = MapView(requireContext())
        locationPermissionHelper = LocationPermissionHelper(WeakReference(requireActivity()))
        locationPermissionHelper.checkPermissions {
            onMapReady()
        }

        return binding.root
    }

    private fun onMapReady() {
        mapView.getMapboxMap().setCamera(
            CameraOptions.Builder()
                .zoom(14.0)
                .build()
        )
//        mapView.getMapboxMap().loadStyleUri(
//            Style.MAPBOX_STREETS
//        ) {
//            geoJsonSource("line") {
//                url("asset://map.geojson")
//            }
//            +linelayer("linelayer", "line") {
//
//            }
//            initLocationComponent()
//            setupGesturesListener()
//        }
        mapView.getMapboxMap().loadStyle((
                    style(styleUri = Style.MAPBOX_STREETS) {
                        +geoJsonSource(GEOJSON_SOURCE_ID) {
                            url("assets://map.geojson")
                        }
                        +lineLayer("linelayer", GEOJSON_SOURCE_ID) {
                            lineCap(LineCap.ROUND)
                            lineJoin(LineJoin.ROUND)
                            lineOpacity(0.7)
                            lineWidth(8.0)
                            lineColor("#800")
                        }
                    }))
//        mapView.viewport.apply {
//            transitionTo(
//                makeFollowPuckViewportState(
//                    FollowPuckViewportStateOptions
//                        .Builder()
//                        // in case if you want to keep the north up
//                        .bearing(FollowPuckViewportStateBearing.Constant(0.0))
//                        .pitch(0.0)
//                        .zoom(10.0)
//                        .build()
//                )
//            ) {
//                idle()
//            }
//        }
    }

    private fun setupGesturesListener() {
        mapView.gestures.addOnMoveListener(onMoveListener)
    }

    private fun initLocationComponent() {
        val locationComponentPlugin = mapView.location
        locationComponentPlugin.updateSettings {
            this.enabled = true
            this.locationPuck = LocationPuck2D(
                bearingImage = AppCompatResources.getDrawable(
//                    this@LocationTrackingActivity,
                    requireContext(),
                    R.drawable.mapbox_user_puck_icon,
                ),
                shadowImage = AppCompatResources.getDrawable(
//                    this@LocationTrackingActivity,
                    requireContext(),
                    R.drawable.mapbox_user_icon_shadow,
                ),
                scaleExpression = interpolate {
                    linear()
                    zoom()
                    stop {
                        literal(0.0)
                        literal(0.6)
                    }
                    stop {
                        literal(20.0)
                        literal(1.0)
                    }
                }.toJson()
            )
        }
        locationComponentPlugin.addOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
        locationComponentPlugin.addOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
    }
//
//    private fun onCameraTrackingDismissed() {
//        Snackbar.make(requireView(), "onCameraTrackingDismissed", Snackbar.LENGTH_SHORT).show()
//        mapView.location
//            .removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
//        mapView.location
//            .removeOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
//        mapView.gestures.removeOnMoveListener(onMoveListener)
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        mapView.location
//            .removeOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
//        mapView.location
//            .removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
//        mapView.gestures.removeOnMoveListener(onMoveListener)
//    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        locationPermissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    companion object {
        private const val GEOJSON_SOURCE_ID = "line"
        private const val ZOOM = 14.0
    }
}