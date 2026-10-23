package com.carkzis

import com.carkzis.model.PostgresUserRepository
import com.carkzis.model.UserRepository
import io.ktor.server.application.*
import io.ktor.server.plugins.di.*

// The contents of the `install` function will be used for the project template
fun Application.configureDependencyInjection() {
    dependencies {
        provide<UserRepository> {
            PostgresUserRepository()
        }
    }
}

