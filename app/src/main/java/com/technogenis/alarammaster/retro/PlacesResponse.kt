package com.technogenis.alarammaster.retro

import com.google.android.gms.nearby.messages.Distance

data class PlacesResponse(
    val results: List<PlaceResult>
//    val routes: List<Route>?
)

data class PlaceResult(
    val name: String,
    val geometry: Geometry
)

data class Leg(
    val distance: Distance,
    val duration: Duration
)
data class Route(
    val legs: List<Leg>
)

data class Step(
    val start_location: Location,
    val end_location: Location,
    val polyline: Polyline
)

data class Polyline(
    val points: String
)

data class Location(
    val lat: Double,
    val lng: Double
)

data class Geometry(
    val location: LocationData
)

data class LocationData(
    val lat: Double,
    val lng: Double
)

data class Duration(
    val text: String,
    val value: Int
)

