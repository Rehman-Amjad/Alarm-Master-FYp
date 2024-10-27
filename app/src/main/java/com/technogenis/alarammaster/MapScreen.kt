package com.technogenis.alarammaster

import android.content.Intent
import android.os.Bundle
import android.Manifest
import retrofit2.Call
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.CameraPosition

// Place the below imports with the rest of your imports
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.PlacesStatusCodes
import com.technogenis.alarammaster.retro.PlacesResponse
import com.technogenis.alarammaster.retro.PlacesService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MapScreen : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
    private lateinit var placesClient: PlacesClient
    private lateinit var service: PlacesService


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_map_screen)
        // Initialize fused location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        Places.initialize(applicationContext, R.string.api_key.toString())
        placesClient = Places.createClient(this)

        val retrofit = Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
         service = retrofit.create(PlacesService::class.java)


        if(ContextCompat.checkSelfPermission(
            this,Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED){
            initializeMap()
        }else{
            requestLocationPermission()
        }



    }
    // Register for permission result
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                initializeMap()
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    private fun requestLocationPermission() {
        // Request the ACCESS_FINE_LOCATION permission
        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private fun initializeMap() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Enable the location layer on the map
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        mMap.isMyLocationEnabled = true
        // Set marker click listener
        mMap.setOnMarkerClickListener(this)

        // Get the current location and add a marker
        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location: Location? ->
            if (location != null) {
                lastLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)
                placeMarkerOnMap(currentLatLng)
                moveCameraToLocation(currentLatLng)
                showNearbyMosques(currentLatLng)
            } else {
                Toast.makeText(this, "Location not found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showNearbyMosques(currentLatLng: LatLng) {
        val apiKey = R.string.api_key.toString()
        val location = "${currentLatLng.latitude},${currentLatLng.longitude}"
        val radius = 5000 // 5km radius
        val call = service.getNearbyMosques(location, radius, "mosque", apiKey)

        call.enqueue(object : retrofit2.Callback<PlacesResponse> {
            override fun onResponse(call: Call<PlacesResponse>, response: retrofit2.Response<PlacesResponse>) {
                if (response.isSuccessful) {
                    val placesResponse = response.body()
                    placesResponse?.results?.forEach { place ->
                        val placeLatLng = LatLng(place.geometry.location.lat, place.geometry.location.lng)
                        placeMosqueMarkerOnMap(placeLatLng, place.name)
                    }
                } else {
                    Toast.makeText(this@MapScreen, "Failed to fetch mosques", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<PlacesResponse>, t: Throwable) {
                Toast.makeText(this@MapScreen, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun placeMosqueMarkerOnMap(location: LatLng, name: String) {
        val markerOptions = MarkerOptions()
            .position(location)
            .title(name)
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))

        mMap.addMarker(markerOptions)
    }

    private fun placeMarkerOnMap(location: LatLng) {
        val markerOptions = MarkerOptions().position(location).title("You are here!")
        mMap.addMarker(markerOptions)
    }

    private fun moveCameraToLocation(location: LatLng) {
        val cameraPosition = CameraPosition.Builder()
            .target(location)
            .zoom(15f)
            .build()
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }

    // When a marker is clicked, open the Google Maps app with the marker's location
//    override fun onMarkerClick(marker: Marker): Boolean {
//        val gmmIntentUri = Uri.parse("geo:${marker.position.latitude},${marker.position.longitude}")
//        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
//        mapIntent.setPackage("com.google.android.apps.maps")
//        if (mapIntent.resolveActivity(packageManager) != null) {
//            startActivity(mapIntent)
//        } else {
//            Toast.makeText(this, "Google Maps not installed", Toast.LENGTH_SHORT).show()
//        }
//        return true
//    }
    override fun onMarkerClick(marker: Marker): Boolean {
        // Get the destination location from the marker
        val destinationLatLng = marker.position

        // Create an Intent to start the RouteScreen Activity
        val routeIntent = Intent(this, RoutesScreen::class.java)

        // Pass the current location and destination location
        routeIntent.putExtra("currentLocation", LatLng(lastLocation.latitude, lastLocation.longitude))
        routeIntent.putExtra("destinationLocation", destinationLatLng)

        // Start the RouteScreen Activity
        startActivity(routeIntent)
        return true
    }
//    override fun onMarkerClick(marker: Marker): Boolean {
//        // Use the "daddr" parameter to specify the destination (marker location)
//        // and "saddr" parameter to specify the origin (current location).
//        val currentLat = lastLocation.latitude
//        val currentLng = lastLocation.longitude
//        val destinationLat = marker.position.latitude
//        val destinationLng = marker.position.longitude
//
//        // Create a URI to open Google Maps with directions
//        val gmmIntentUri = Uri.parse("https://www.google.com/maps/dir/?api=1&origin=$currentLat,$currentLng&destination=$destinationLat,$destinationLng&travelmode=driving")
//
//        // Create an intent to open Google Maps
//        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
//        mapIntent.setPackage("com.google.android.apps.maps")
//
//        // Verify that there is a Google Maps app available to handle the intent
//        if (mapIntent.resolveActivity(packageManager) != null) {
//            startActivity(mapIntent)
//        } else {
//            Toast.makeText(this, "Google Maps not installed", Toast.LENGTH_SHORT).show()
//        }
//        return true
//    }

}