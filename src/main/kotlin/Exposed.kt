package com.carkzis

import io.ktor.server.application.*
import org.jetbrains.exposed.v1.jdbc.Database

suspend fun Application.configureExposed() {
    Database.connect(
        "jdbc:postgresql://localhost:5432/user_picker_db",
        user = "postgres",
        password = "password"
    )
}
