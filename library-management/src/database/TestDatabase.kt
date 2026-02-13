package com.example.database

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object TestDatabase {
    const val URL = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;"
    const val DRIVER = "org.h2.Driver"

    val db by lazy {
        Database.connect(URL, DRIVER)
    }
    
    fun create() {
        transaction(db) {
            // Reset all tables for deterministic tests.
            SchemaUtils.drop(
                VolunteerTable,
                RoomReservationTable,
                ReservationTable,
                BorrowingTable,
                CopyTable,
                RoomTable,
                BookTable,
                UsersTable
            )
            SchemaUtils.create(
                UsersTable,
                BookTable,
                CopyTable,
                BorrowingTable,
                ReservationTable,
                RoomTable,
                RoomReservationTable,
                VolunteerTable
            )

            Book.new {
                title = "Test Book"
                author = "Test Author"
                isbn = "1234567890"
            }
        }
    }
}
