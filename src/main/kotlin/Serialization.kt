package com.carkzis

import com.carkzis.model.User
import com.carkzis.model.UserRepository
import com.carkzis.model.UserWithDate
import io.ktor.http.*
import io.ktor.serialization.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.di.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

suspend fun Application.configureSerialization() {
    val repository = dependencies.resolve<UserRepository>()

    install(ContentNegotiation) {
        json()
    }

    routing {
        staticResources("/", "static")

        route("/users") {
            get {
                val users = repository.allUsers()

                call.respond(users)
            }

            get("/byName/{userName}") {
                val name = call.parameters["userName"] ?: return@get call.respond(HttpStatusCode.BadRequest)

                val user = repository.userByName(name) ?: return@get call.respond(HttpStatusCode.NotFound)

                call.respond(user)
            }

            post {
                try {
                    val user = call.receive<User>()

                    val currentInstant = Clock.System.now()
                    val currentDate = currentInstant
                        .toLocalDateTime(TimeZone.currentSystemDefault())
                        .format(LocalDateTime.Formats.ISO)

                    val userWithDate = UserWithDate(
                        name = user.name,
                        dateLastPicked = currentDate,
                    )

                    repository.addUser(userWithDate)
                    call.respond(userWithDate)
                } catch (_: IllegalStateException) {
                    call.respond(HttpStatusCode.BadRequest)
                } catch (_: JsonConvertException) {
                    call.respond(HttpStatusCode.BadRequest)
                }
            }

            get("/pick") {
                val user = repository.pickUser() ?: return@get call.respond(HttpStatusCode.NotFound)
                call.respond(user)
            }

            delete("/{userName}") {
                val user = call.parameters["userName"] ?: return@delete call.respond(HttpStatusCode.BadRequest)

                if (repository.removeUser(user)) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }
        }
    }
}