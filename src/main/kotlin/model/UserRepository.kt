package com.carkzis.model

interface UserRepository {
    fun allUsers(): List<UserWithPickPercentage>
    fun userByName(name: String): UserWithDate?
    fun pickUser(): UserWithPickPercentage?
    fun addUser(userWithDate: UserWithDate)
    fun removeUser(name: String): Boolean
}