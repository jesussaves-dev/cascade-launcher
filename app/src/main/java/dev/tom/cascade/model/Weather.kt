package dev.tom.cascade.model

data class Place(
    val name: String,
    val latitude: Double,
    val longitude: Double,
)

data class Weather(
    val temperatureC: Int,
    val code: Int,
    val updatedAtMillis: Long,
)
