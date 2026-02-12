package com.example.database

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.timestamp
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

object ReservationTable : IntIdTable("reservation", "reservation_id") {
    val user = reference("user_id", UsersTable, ReferenceOption.CASCADE)
    val copy = reference("copy_id", CopyTable, ReferenceOption.CASCADE)
    val dateReserved = timestamp("date_reserved")
    val timeLimit = timestamp("time_limit")
    val status = enumerationByName("status", MAX_ENUM_LENGTH, ReservationStatus::class)
}
