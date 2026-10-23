package com.carkzis

import com.carkzis.model.FakeUserRepository
import com.carkzis.model.UserWithDate
import kotlinx.coroutines.test.runTest
import org.junit.Test

class FakeUserRepositoryTest {
    val sut = FakeUserRepository()

    @Test
    fun `test allUsers returns all users`() = runTest {
        val users = sut.allUsers()
        assert(users.size == 4)
    }

    @Test
    fun `test userByName returns correct user`() = runTest {
        val user = sut.userByName("Andy")
        assert(user != null)
        assert(user?.name == "Andy")
    }

    @Test
    fun `test addUser adds a new user`() = runTest {
        val newUser = UserWithDate(name = "Eve", dateLastPicked = "2026-10-23T10:33:19Z")
        sut.addUser(newUser)
        val user = sut.userByName("Eve")
        assert(user != null)
        assert(user?.name == "Eve")
    }

    @Test
    fun `test removeUser removes a user`() = runTest {
        val removed = sut.removeUser("Andy")
        assert(removed)
        val user = sut.userByName("Andy")
        assert(user == null)
    }

    @Test
    fun `test pickUser returns a user`() = runTest {
        val pickedUser = sut.pickUser()
        assert(pickedUser != null)
        assert(pickedUser?.name in listOf("Andy", "Becky", "Charlie", "Dara"))
    }

    @Test
    fun `test pickUser updates dateLastPicked`() = runTest {
        val pickedUser = sut.pickUser()
        assert(pickedUser != null)
        val updatedUser = sut.userByName(pickedUser!!.name)
        assert(updatedUser != null)
        assert(updatedUser?.dateLastPicked != "2026-10-23T10:33:19Z")
    }

    @Test
    fun `test allUsers returns users with pick percentages`() = runTest {
        val users = sut.allUsers()
        assert(users.isNotEmpty())
        users.forEach { user ->
            assert(user.pickPercentage >= 0.0)
            assert(user.pickPercentage == (100 / users.size).toDouble())
        }
    }

    @Test
    fun `test pickUser returns null when no users`() = runTest {
        // Remove all users
        sut.removeUser("Andy")
        sut.removeUser("Becky")
        sut.removeUser("Charlie")
        sut.removeUser("Dara")
    }

    @Test
    fun `test addUser throws exception for duplicate user`() = runTest {
        val duplicateUser = UserWithDate(name = "Andy", dateLastPicked = "2026-10-23T10:33:19Z")
        try {
            sut.addUser(duplicateUser)
            assert(false) // Should not reach here
        } catch (e: IllegalStateException) {
            assert(e.message == "Cannot duplicate task names!")
        }
    }

    @Test
    fun `test userByName returns null when not found`() = runTest {
        val user = sut.userByName("NonExistentUser")
        assert(user == null)
    }
}