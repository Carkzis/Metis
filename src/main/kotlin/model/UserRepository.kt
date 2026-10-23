package com.carkzis.model

interface UserRepository {
    suspend fun allUsers(): List<UserWithPickPercentage>
    suspend fun userByName(name: String): UserWithDate?
    suspend fun pickUser(): UserWithDate?
    suspend fun addUser(userWithDate: UserWithDate)
    suspend fun removeUser(name: String): Boolean
}