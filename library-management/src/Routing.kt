package com.example

import io.ktor.server.application.*
import io.ktor.server.pebble.respondTemplate
import io.ktor.server.routing.*

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
