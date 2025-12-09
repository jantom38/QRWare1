package com.qrware.shared.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class LocationType {
    WAREHOUSE, SHELF, RACK, BIN, ZONE, FLOOR, ROOM, BUILDING
}

@Serializable
enum class ZoneType {
    RECEIVING, STORAGE, PICKING, PACKING, SHIPPING, QUALITY_CONTROL, RETURNS
}

@Serializable
data class Location(
    val id: Long? = null,
    val name: String,
    val description: String? = null,
    val locationType: LocationType,
    val zone: Zone? = null,
    val parentLocation: Location? = null,
    val capacity: Int? = null,
    val currentOccupancy: Int = 0,
    val isActive: Boolean = true,
    val coordinates: String? = null,
    val barcode: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

@Serializable
data class Zone(
    val id: Long? = null,
    val name: String,
    val description: String? = null,
    val zoneType: ZoneType,
    val isActive: Boolean = true,
    val capacity: Int? = null,
    val currentOccupancy: Int = 0,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

@Serializable
data class CreateLocationRequest(
    val name: String,
    val description: String? = null,
    val locationType: LocationType,
    val zoneId: Long? = null,
    val parentLocationId: Long? = null,
    val capacity: Int? = null,
    val coordinates: String? = null,
    val barcode: String? = null
)

@Serializable
data class UpdateLocationRequest(
    val name: String? = null,
    val description: String? = null,
    val locationType: LocationType? = null,
    val zoneId: Long? = null,
    val parentLocationId: Long? = null,
    val capacity: Int? = null,
    val coordinates: String? = null,
    val barcode: String? = null,
    val isActive: Boolean? = null
)

@Serializable
data class CreateZoneRequest(
    val name: String,
    val description: String? = null,
    val zoneType: ZoneType,
    val capacity: Int? = null
)

@Serializable
data class UpdateZoneRequest(
    val name: String? = null,
    val description: String? = null,
    val zoneType: ZoneType? = null,
    val capacity: Int? = null,
    val isActive: Boolean? = null
)