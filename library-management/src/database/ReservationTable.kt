package com.example.database

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

object ReservationTable : IntIdTable("reservation", "reservation_id") {
    val user = reference("user_id", UsersTable, ReferenceOption.CASCADE)
    val copy = reference("copy_id", CopyTable, ReferenceOption.CASCADE)
    val dateReserved = long("date_reserved")
    val timeLimit = long("time_limit")
    val status = enumerationByName("status", MAX_ENUM_LENGTH, ReservationStatus::class)

    init {
        index(
            customIndexName = "uq_reservation_active_copy",
            isUnique = true,
            copy,
            filterCondition = { status eq ReservationStatus.active }
        )
    }
}
