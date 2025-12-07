package com.assisment.newschatprofileapp.utils


import android.location.Geocoder
import java.util.Locale

// Extension to format location coordinates
fun Double.formatCoordinate(decimalPlaces: Int = 6): String {
    return String.format("%.${decimalPlaces}f", this)
}

// Extension to get address from coordinates
fun Pair<Double, Double>.getAddress(context: android.content.Context): String? {
    return try {
        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses = geocoder.getFromLocation(this.first, this.second, 1)

        addresses?.firstOrNull()?.let { address ->
            val addressParts = mutableListOf<String>()

            address.thoroughfare?.let { addressParts.add(it) }
            address.subThoroughfare?.let { addressParts.add(it) }
            address.locality?.let { addressParts.add(it) }
            address.adminArea?.let { addressParts.add(it) }
            address.countryName?.let { addressParts.add(it) }

            if (addressParts.isNotEmpty()) {
                addressParts.joinToString(", ")
            } else {
                null
            }
        }
    } catch (e: Exception) {
        null
    }
}


fun Pair<Double, Double>.distanceTo(other: Pair<Double, Double>): Float {
    val results = FloatArray(1)
    android.location.Location.distanceBetween(
        this.first, this.second,
        other.first, other.second,
        results
    )
    return results[0]
}