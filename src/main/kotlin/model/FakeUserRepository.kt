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

class FakeUserRepository : UserRepository {
    private val userWithDates = mutableListOf(
        UserWithDate(name = "Andy", dateLastPicked = "2026-10-23T10:33:19Z"),
        UserWithDate(name = "Becky", dateLastPicked = "2026-10-23T10:33:19Z"),
        UserWithDate(name = "Charlie", dateLastPicked = "2026-10-23T10:33:19Z"),
        UserWithDate(name = "Dara", dateLastPicked = "2026-10-23T10:33:19Z")
    )

    override fun allUsers(): List<UserWithPickPercentage> {
        return weightUsers()
    }

    override fun userByName(name: String): UserWithDate? = userWithDates.find { it.name.equals(name, ignoreCase = true) }

    override fun pickUser(): UserWithPickPercentage? {
        val weightedUsers = weightUsers()
        if (weightedUsers.isEmpty()) return null

        val totalWeight = weightedUsers.sumOf { it.pickPercentage }
        val randomValue = (0..totalWeight.toInt()).random().toDouble()

        weightedUsers.fold(0.0) { cumulativeWeight, user ->
            val newCumulativeWeight = cumulativeWeight + user.pickPercentage
            if (randomValue <= newCumulativeWeight) {
                val currentInstant = Clock.System.now()
                val currentDate = currentInstant
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                    .format(kotlinx.datetime.LocalDateTime.Formats.ISO)

                val updatedUser = UserWithDate(
                    name = user.name,
                    dateLastPicked = currentDate
                )

                userWithDates.removeIf { it.name == user.name }
                userWithDates.add(UserWithDate(name = updatedUser.name, dateLastPicked = updatedUser.dateLastPicked))

                return user
            }
            newCumulativeWeight
        }

        return null
    }

    override fun addUser(userWithDate: UserWithDate) {
        if (userByName(userWithDate.name) != null) {
            throw IllegalStateException("Cannot duplicate task names!")
        }
        userWithDates.add(userWithDate)
    }

    override fun removeUser(name: String) = userWithDates.removeIf { it.name == name }

    private fun weightUsers(): List<UserWithPickPercentage> {
        val now = Instant.now()
        val weightedUsers = userWithDates.map { userWithDate ->
            val weight = userWithDate.dateLastPicked.toInstantOrNull()
                ?.let { Duration.between(it, now).toMillis().coerceAtLeast(0).toDouble() }
                ?: 0.0

            userWithDate to weight
        }

        val totalWeight = weightedUsers.sumOf { it.second }

        if (totalWeight <= 0.0) {
            val equalShare = 100.0 / weightedUsers.size
            return weightedUsers
                .sortedByDescending { it.second }
                .map { (userWithDate, _) ->
                    UserWithPickPercentage(
                        name = userWithDate.name,
                        dateLastPicked = userWithDate.dateLastPicked,
                        pickPercentage = equalShare
                    )
                }
        }

        return weightedUsers
            .sortedByDescending { it.second }
            .map { (userWithDate, weight) ->
                UserWithPickPercentage(
                    name = userWithDate.name,
                    dateLastPicked = userWithDate.dateLastPicked,
                    pickPercentage = (weight / totalWeight) * 100.0
                )
            }
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
}