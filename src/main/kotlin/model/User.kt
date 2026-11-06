package com.carkzis.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val name: String,
)

@Serializable
data class UserWithDate(
    val name: String,
    val dateLastPicked: String,
    val isAvailable: Boolean = true
)

@Serializable
data class AvailabilityUpdate(
    val isAvailable: Boolean
)

@Serializable
data class UserWithPickPercentage(
    val name: String,
    val dateLastPicked: String,
    val pickPercentage: Double,
    val isAvailable: Boolean = true
)