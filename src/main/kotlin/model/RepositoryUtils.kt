package com.carkzis.model

import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.toLocalDateTime
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.time.Clock

internal fun weightUsers(users: List<UserWithDate>): List<UserWithPickPercentage> {
    val now = Instant.now()
    val available = users.filter { it.isAvailable }
    val weightedUsers = available.map { userWithDate ->
        val weight = userWithDate.dateLastPicked.toInstantOrNull()
            ?.let { Duration.between(it, now).toMillis().coerceAtLeast(0).toDouble() }
            ?: 0.0

        userWithDate to weight
    }

    val totalWeight = weightedUsers.sumOf { it.second }
    val equalShare = if (weightedUsers.isEmpty()) 0.0 else 100.0 / weightedUsers.size

    val availableResults = weightedUsers
        .sortedByDescending { it.second }
        .map { (userWithDate, weight) ->
            UserWithPickPercentage(
                name = userWithDate.name,
                dateLastPicked = userWithDate.dateLastPicked,
                pickPercentage = if (totalWeight <= 0.0) equalShare else (weight / totalWeight) * 100.0,
                isAvailable = true
            )
        }

    val unavailableResults = users
        .filterNot { it.isAvailable }
        .map {
            UserWithPickPercentage(
                name = it.name,
                dateLastPicked = it.dateLastPicked,
                pickPercentage = 0.0,
                isAvailable = false
            )
        }

    return availableResults + unavailableResults
}

fun currentDateString(): String = Clock.System.now()
    .toLocalDateTime(TimeZone.currentSystemDefault())
    .format(kotlinx.datetime.LocalDateTime.Formats.ISO)

fun pickUserFromWeightedList(users: List<UserWithDate>): UserWithDate? {
    val weightedUsers = weightUsers(users).filter { it.isAvailable }
    if (weightedUsers.isEmpty()) return null

    val totalWeight = weightedUsers.sumOf { it.pickPercentage }
    val randomValue = (0..totalWeight.toInt()).random().toDouble()

    weightedUsers.fold(0.0) { cumulativeWeight, user ->
        val newCumulativeWeight = cumulativeWeight + user.pickPercentage
        if (randomValue <= newCumulativeWeight) {
            return users.first { it.name == user.name }
        }
        newCumulativeWeight
    }

    return null
}

private fun String.toInstantOrNull(): Instant? = try {
    Instant.parse(this)
} catch (_: Exception) {
    try {
        LocalDateTime.parse(this, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            .atZone(ZoneId.systemDefault())
            .toInstant()
    } catch (_: Exception) {
        null
    }
}