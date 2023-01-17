package com.example.androidstudioproject.ui.main

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.example.androidstudioproject.BuildConfig
import com.example.androidstudioproject.R
import com.example.androidstudioproject.databinding.FragmentCreateRouteBinding
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.IgnoreExtraProperties
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlin.random.Random

class CreateRouteFragment : Fragment() {
    private lateinit var database: DatabaseReference
    private lateinit var binding: FragmentCreateRouteBinding

    private val viewModel: MainViewModel by viewModels(
        ownerProducer = { requireActivity() }
    )

    @IgnoreExtraProperties
    data class Route(val userId: String? = null, val name: String? = null, val indices: List<List<Double>>? = null) {
    }

    fun createRoute(userId: String?, name: String, indices: List<List<Double>>) {
        val route = Route(userId, name, indices)

        val charset = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        val routeId = (1..10).map { charset.random() }.joinToString("")

        database.child("routes").child(routeId).setValue(route).addOnSuccessListener {
            Snackbar.make(requireContext(), requireView(), "Success", Snackbar.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Snackbar.make(requireContext(), requireView(), "Something went wrong!", Snackbar.LENGTH_SHORT).show()
        }
    }

    fun LatLngToDoubleArray(array: List<LatLng>): List<List<Double>> {
        var newArray = ArrayList<List<Double>>()
        for (element in array) {
            var newElement = ArrayList<Double>()
            newElement.add(element.latitude)
            newElement.add(element.longitude)
            newArray.add(newElement)
        }
        return newArray
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCreateRouteBinding.inflate(inflater,container, false)
        database = Firebase.database.reference

        binding.AddButton.setOnClickListener {
            val name = binding.NameValue.text
            val ogLatVal = binding.OriginLatitudeValue.text
            val ogLonVal = binding.OriginLongitudeValue.text
            val dstLatVal = binding.DestinationLatidudeValue.text
            val dstLonVal = binding.DestinationLongitudeValue.text
            if (name == null || ogLatVal == null || ogLonVal == null || dstLatVal == null || dstLonVal == null) {
                Snackbar.make(requireContext(),requireView(),"Some of the fields are empty", Snackbar.LENGTH_SHORT).show()
            } else {
                if(viewModel.checkUser()) {
                    val ogLatLon: LatLng = LatLng(ogLatVal.toString().toDouble(),ogLonVal.toString().toDouble())
                    val dstLatLon: LatLng = LatLng(dstLatVal.toString().toDouble(),dstLonVal.toString().toDouble())
                    val url = getDirectionURL(ogLatLon, dstLatLon, BuildConfig.DIRECTIONS_API_KEY)
                    val route = GetDirection(url).execute().get()
                    val doubleArr = LatLngToDoubleArray(route[0])
                    createRoute(viewModel.user.value!!.uid, name.toString(), doubleArr)
                } else {
                    Snackbar.make(requireContext(), requireView(), "You are not logged in to create the route", Snackbar.LENGTH_SHORT).show()
                }
            }
        }
        return binding.root
    }

    private fun getDirectionURL(origin:LatLng, dest:LatLng, secret: String) : String{
        return "https://maps.googleapis.com/maps/api/directions/json?origin=${origin.latitude},${origin.longitude}" + "&destination=${dest.latitude},${dest.longitude}" + "&sensor=false" + "&mode=driving" + "&key=$secret"
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
        }
    }
}