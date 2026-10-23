package com.carkzis.model

import com.carkzis.db.UserDAO
import com.carkzis.db.UserTable
import com.carkzis.db.daoToModel
import com.carkzis.db.withTransaction
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
        val usersWithDates = UserDAO.all().map(::daoToModel).toMutableList()
        val pickedUser = pickUserFromWeightedList(usersWithDates)

        if (pickedUser != null) {
            UserDAO.find { UserTable.name eq pickedUser.name }
                .limit(1)
                .firstOrNull()
                ?.apply {
                    dateLastPicked = pickedUser.dateLastPicked
                }
        }

        pickedUser
    }

    override suspend fun addUser(userWithDate: UserWithDate): Unit = withTransaction {
        UserDAO.new {
            name = userWithDate.name
            dateLastPicked = userWithDate.dateLastPicked
        }
    }

    override suspend fun removeUser(name: String): Boolean = withTransaction {
        val rowsDeleted = UserTable.deleteWhere {
            UserTable.name eq name
        }
        rowsDeleted == 1
    }
}