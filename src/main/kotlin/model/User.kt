package com.carkzis.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val name: String,
)

@Serializable
data class UserWithDate(
    val name: String,
    val dateLastPicked: String
)

@Serializable
data class UserWithPickPercentage(
    val name: String,
    val dateLastPicked: String,
    val pickPercentage: Double
)