package com.carkzis.model

class FakeUserRepository : UserRepository {
    private val userWithDates = mutableListOf(
        UserWithDate(name = "Andy", dateLastPicked = "2026-10-23T10:33:19Z"),
        UserWithDate(name = "Becky", dateLastPicked = "2026-10-23T10:33:19Z"),
        UserWithDate(name = "Charlie", dateLastPicked = "2026-10-23T10:33:19Z"),
        UserWithDate(name = "Dara", dateLastPicked = "2026-10-23T10:33:19Z")
    )

    override suspend fun allUsers(): List<UserWithPickPercentage> {
        return weightUsers(userWithDates)
    }

    override suspend fun userByName(name: String): UserWithDate? = userWithDates.find { it.name.equals(name, ignoreCase = true) }

    override suspend fun pickUser(): UserWithDate? {
        return pickUserFromWeightedList(userWithDates)
    }

    override suspend fun addUser(userWithDate: UserWithDate) {
        if (userByName(userWithDate.name) != null) {
            throw IllegalStateException("Cannot duplicate task names!")
        }
        userWithDates.add(userWithDate)
    }

    override suspend fun removeUser(name: String) = userWithDates.removeIf { it.name == name }

}