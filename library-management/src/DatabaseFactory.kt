package com.example
import com.example.database.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
        const val URL = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;"
        const val DRIVER = "org.h2.Driver"

        val db by lazy {
            Database.connect(URL, DRIVER)
        }


        fun createSchema() {
            transaction(db) {
                SchemaUtils.create(
                    UsersTable, BookTable, CopyTable, BorrowingTable,
                    ReservationTable, RoomTable, RoomReservationTable, VolunteerTable
                )
            }
        }
    
}
