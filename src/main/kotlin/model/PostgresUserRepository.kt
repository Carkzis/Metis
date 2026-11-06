package com.carkzis.model

import com.carkzis.db.UserDAO
import com.carkzis.db.UserTable
import com.carkzis.db.daoToModel
import com.carkzis.db.withTransaction
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere

class PostgresUserRepository : UserRepository {
    override suspend fun allUsers(): List<UserWithPickPercentage> = withTransaction {
        val usersWithDates = UserDAO.all().map(::daoToModel)
        weightUsers(usersWithDates)
    }

    override suspend fun userByName(name: String): UserWithDate? = withTransaction {
        UserDAO
            .find { (UserTable.name eq name) }
            .limit(1)
            .map(::daoToModel)
            .firstOrNull()
    }

    override suspend fun pickUser(): UserWithDate? = withTransaction {
        pickUserFromWeightedList(UserDAO.all().map(::daoToModel))
    }

    override suspend fun confirmPick(name: String): UserWithDate? = withTransaction {
        UserDAO.find { (UserTable.name eq name) and (UserTable.isAvailable eq true) }
            .limit(1)
            .firstOrNull()
            ?.apply { dateLastPicked = currentDateString() }
            ?.let(::daoToModel)
    }

    override suspend fun addUser(userWithDate: UserWithDate): Unit = withTransaction {
        UserDAO.new {
            name = userWithDate.name
            dateLastPicked = userWithDate.dateLastPicked
            isAvailable = userWithDate.isAvailable
        }
    }

    override suspend fun setAvailability(name: String, isAvailable: Boolean): Boolean = withTransaction {
        val user = UserDAO.find { UserTable.name eq name }.limit(1).firstOrNull()
        user?.isAvailable = isAvailable
        user != null
    }

    override suspend fun removeUser(name: String): Boolean = withTransaction {
        val rowsDeleted = UserTable.deleteWhere {
            UserTable.name eq name
        }
        rowsDeleted == 1
    }
}