package com.example.database

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.dao.id.IntIdTable

object BorrowingTable : IntIdTable("borrowing", "borrow_id") {
    val user = reference("user_id", UsersTable, ReferenceOption.CASCADE)
    val copy = reference("copy_id", CopyTable, ReferenceOption.CASCADE)
    val dateIn = long("date_in")
    val dateOut = long("date_out")
    val status = enumerationByName("status", MAX_ENUM_LENGTH, BorrowingStatus::class)
}
