package com.example

import io.ktor.server.application.*
import com.example.DatabaseFactory


fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureTemplates()
    configureRouting()
    DatabaseFactory.createSchema()
}
