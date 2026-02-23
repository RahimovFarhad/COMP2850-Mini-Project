package com.example
import com.example.database.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

import org.apache.commons.csv.CSVFormat
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.insertAndGetId

import java.io.InputStreamReader
import java.io.BufferedReader
import com.example.database.CopyAvailabilityStatus


object DatabaseFactory {
    const val BOOKS_DATA = "csv/library_booklist.csv"
    private const val DEFAULT_H2_URL = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE"

    private val dbUrl: String = normalizeDatabaseUrl(
        System.getenv("DATABASE_URL") ?: DEFAULT_H2_URL
    )
    private val dbUser: String = System.getenv("DATABASE_USER")
        ?: "sa"
    private val dbPassword: String = System.getenv("DATABASE_PASSWORD")
        ?: ""
    private val dbDriver: String = when ((System.getenv("DATABASE_DRIVER") ?: "org.h2.Driver").trim().lowercase()) {
        "postgresql", "postgres", "org.postgresql", "org.postgresql.driver" -> "org.postgresql.Driver"
        else -> System.getenv("DATABASE_DRIVER") ?: "org.h2.Driver"
    }

    private fun normalizeDatabaseUrl(rawUrl: String): String {
        val url = rawUrl.trim()
        if (!url.startsWith("jdbc:h2:mem:", ignoreCase = true)) {
            return url
        }

        val lower = url.lowercase()
        var normalized = url

        if ("db_close_delay=" !in lower) {
            normalized += if (normalized.endsWith(";")) "DB_CLOSE_DELAY=-1" else ";DB_CLOSE_DELAY=-1"
        }
        if ("db_close_on_exit=" !in lower) {
            normalized += if (normalized.endsWith(";")) "DB_CLOSE_ON_EXIT=FALSE" else ";DB_CLOSE_ON_EXIT=FALSE"
        }

        return normalized
    }

    val db by lazy {
        Database.connect(dbUrl, dbDriver, dbUser, dbPassword)
    }

    fun main(args: Array<String>) {
        val logging = args.isNotEmpty() && args[0].lowercase() == "--sql"
        val resetSchema = args.any { it == "--reset-schema" }
        val seedBooks = args.any { it == "--seed-books" }

        initDatabase(resetSchema = resetSchema, seedBooks = seedBooks, logging = logging)
    }

    fun initFromEnvironment() {
        val logging = (System.getenv("DB_SQL_LOG") ?: "false").toBoolean()
        val resetSchema = (System.getenv("DB_RESET_SCHEMA") ?: "false").toBoolean()
        val seedBooks = (System.getenv("DB_SEED_BOOKS") ?: "false").toBoolean()
        initDatabase(resetSchema = resetSchema, seedBooks = seedBooks, logging = logging)
    }

    fun initDatabase(resetSchema: Boolean, seedBooks: Boolean, logging: Boolean) {
        transaction(db) {
            if (logging) {
                addLogger(StdOutSqlLogger)
            }

            if (resetSchema) {
                SchemaUtils.drop(
                    VolunteerTable, RoomReservationTable, ReservationTable, BorrowingTable,
                    CopyTable, RoomTable, BookTable, UsersTable
                )
            }
            SchemaUtils.create(
                UsersTable, BookTable, CopyTable, BorrowingTable,
                ReservationTable, RoomTable, RoomReservationTable, VolunteerTable
            )

            if (seedBooks) {
                addBooks(BOOKS_DATA)
            }
        }
    }

    fun addBooks(filename: String): LinkedHashMap<String, EntityID<Int>> {
        val inputStream = this::class.java.classLoader.getResourceAsStream(filename)
            ?: throw IllegalArgumentException("File not found: $filename")

        InputStreamReader(inputStream).use { reader ->
            val records = CSVFormat.DEFAULT.parse(BufferedReader(reader)).drop(1)
            val books = LinkedHashMap<String, EntityID<Int>>()
            val copies = LinkedHashMap<String, EntityID<Int>>()
            for (record in records) {
                if (books.containsKey(record[0])) {
                    val bookId = books[record[0]] ?: error("Book id missing for title ${record[0]}")
                    copies[record[0]] = CopyTable.insertAndGetId {
                        it[book] = bookId
                        it[availabilityStatus] = CopyAvailabilityStatus.available
                        it[location] = "Default Location"
                    }
                    continue
                }
                // if (Book.find {BookTable.isbn eq record[2]}.count() == 0L){
                    books[record[0]] = BookTable.insertAndGetId {
                        it[title] = record[0]
                        it[author] = record[1]
                        it[isbn] = record[2]
                    }
                // }
                val bookId = books[record[0]] ?: error("Book id missing for title ${record[0]}")
                copies[record[0]] = CopyTable.insertAndGetId {
                    it[book] = bookId
                    it[availabilityStatus] = CopyAvailabilityStatus.available
                    it[location] = "Default Location"
                }

            }
            return books
        }
    }
}
