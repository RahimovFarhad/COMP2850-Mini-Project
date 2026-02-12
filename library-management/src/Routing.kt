package com.example

import io.ktor.server.application.*
import io.ktor.server.pebble.respondTemplate
import io.ktor.server.routing.*
import io.ktor.server.response.*

import org.jetbrains.exposed.sql.transactions.transaction
import com.example.database.Users
import com.example.database.UserRole


fun Application.configureRouting() {
    routing {
        get("/") {
            call.displayForm()
        }
    }
}

private suspend fun ApplicationCall.displayForm() {
    respondTemplate("form.peb", model = emptyMap<String, Any>())
}
