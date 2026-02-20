package com.example

import io.ktor.server.application.*
import com.example.DatabaseFactory

import com.example.database.TestDatabase
import org.jetbrains.exposed.sql.transactions.TransactionManager
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.respondRedirect
import io.ktor.server.sessions.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureTemplates()
    configureAuthentication()
    configureRouting()
    DatabaseFactory.initFromEnvironment()
}


fun Application.testModule() {
    TestDatabase.create()
    TransactionManager.defaultDatabase = TestDatabase.db
    configureTemplates()
    configureAuthentication()
    configureRouting()
}
