package com.technogenis.alarammaster

import DirectionsResponse
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.technogenis.alarammaster.retro.DirectionService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar

class RoutesScreen : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private var currentLocation: LatLng? = null
    private var destinationLocation: LatLng? = null
    private lateinit var directionsService: DirectionService

    private lateinit var distanceTextView: TextView
    private lateinit var durationTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_routes_screen)

        // Initialize TextViews for distance and duration
        distanceTextView = findViewById(R.id.distance_text)
        durationTextView = findViewById(R.id.duration_text)

        setAlarm("SILENT_MODE")

        // Retrieve the current and destination locations from Intent
        currentLocation = intent.getParcelableExtra("currentLocation")
        destinationLocation = intent.getParcelableExtra("destinationLocation")

        // Initialize the map
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


//        fetchRouteDetails()
    }

    private fun setAlarm(mode: String) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1  // Months are zero-based, so add 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        calendar.add(Calendar.MINUTE, 5)
        val newHour = calendar.get(Calendar.HOUR_OF_DAY)
        val newMinute = calendar.get(Calendar.MINUTE)
        calendar.set(year, month, day, newHour, newMinute, 0)

        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            Toast.makeText(this, "Please set a future time.", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(this, AlarmReceiver::class.java).apply {
            action = mode
        }

        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)

        Toast.makeText(this, "Alarm set for $mode", Toast.LENGTH_SHORT).show()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Place markers for current and destination locations
        currentLocation?.let {
            mMap.addMarker(MarkerOptions().position(it).title("Current Location"))
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(it, 12f))
        }

        destinationLocation?.let {
            mMap.addMarker(MarkerOptions().position(it).title("Destination Location"))
            mMap.addMarker(MarkerOptions().position(it).title("Destination Location"))
            drawRoute(currentLocation!!, destinationLocation!!)
        }
    }

    private fun drawRoute(origin: LatLng, destination: LatLng) {
        // Example to add a line between the two points
        val polylineOptions = PolylineOptions()
            .add(origin)
            .add(destination)
            .width(5f)
            .color(android.graphics.Color.BLUE)

        mMap.addPolyline(polylineOptions)
    }

    private fun fetchRouteDetails() {
        // Check if locations are available
        if (currentLocation == null || destinationLocation == null) {
            Log.e("MapScreen", "Current location or destination location is not available")
            Toast.makeText(this, "Current location or destination location is not available", Toast.LENGTH_SHORT).show()
            return
        }

        // Create a location string for the API request
        val origin = "${currentLocation!!.latitude},${currentLocation!!.longitude}"
        val destination = "${destinationLocation!!.latitude},${destinationLocation!!.longitude}"
        val apiKey = R.string.api_key.toString() // Make sure to keep this secure and not hardcoded

        // Log the origin and destination
        Log.d("MapScreen", "Origin: $origin, Destination: $destination")

        // Make a request to the Directions API
        directionsService.getDirections(origin, destination, apiKey).enqueue(object : Callback<DirectionsResponse> {
            override fun onResponse(call: Call<DirectionsResponse>, response: Response<DirectionsResponse>) {
                if (response.isSuccessful) {
                    val directionsResponse = response.body()
                    Log.d("MapScreen", "API Response: $directionsResponse")

                    // Check for null routes and legs
                    val route = directionsResponse?.routes?.firstOrNull()
                    if (route != null) {
                        val leg = route.legs?.first()
                        // Safely set distance and duration to TextViews
//                        distanceTextView.text = "Distance: ${leg?.distance?.text ?: "N/A"}"
//                        durationTextView.text = "Duration: ${leg?.duration?.text ?: "N/A"}"
                    } else {
                        Log.e("MapScreen", "No routes or legs found")
                        Toast.makeText(this@RoutesScreen, "No routes found", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("MapScreen", "Request failed: ${response.code()} ${response.message()}")
                    Toast.makeText(this@RoutesScreen, "Request failed: ${response.code()} ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<DirectionsResponse>, t: Throwable) {
                Log.e("MapScreen", "API call failed: ${t.message}")
                Toast.makeText(this@RoutesScreen, "API call failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }



}