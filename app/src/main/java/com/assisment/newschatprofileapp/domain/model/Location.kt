package com.assisment.newschatprofileapp.domain.model


data class Location(
    val latitude: Double,
    val longitude: Double,
    val address: String? = null
) {
    fun getDisplayLocation(): String {
        return address ?: "${String.format("%.6f", latitude)}, ${String.format("%.6f", longitude)}"
    }

    fun hasAddress(): Boolean {
        return !address.isNullOrBlank() && address != getDefaultCoordinatesString()
    }

    private fun getDefaultCoordinatesString(): String {
        return "${String.format("%.6f", latitude)}, ${String.format("%.6f", longitude)}"
    }
}