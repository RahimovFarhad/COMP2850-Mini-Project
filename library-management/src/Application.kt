package com.example

import io.ktor.server.application.*
import com.example.DatabaseFactory

import com.example.database.TestDatabase
import org.jetbrains.exposed.sql.transactions.TransactionManager


fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureTemplates()
    configureRouting()
    DatabaseFactory.initFromEnvironment()
}

fun Application.testModule() {
    TestDatabase.create()
    TransactionManager.defaultDatabase = TestDatabase.db
    configureTemplates()
    configureRouting()
}
