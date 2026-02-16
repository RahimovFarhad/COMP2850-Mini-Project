package com.example.database

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.dao.id.IntIdTable

object RoomReservationTable : IntIdTable("room_reservation", "room_reservation_id") {
    val room = reference("room_id", RoomTable, ReferenceOption.CASCADE)
    val user = reference("user_id", UsersTable, ReferenceOption.CASCADE)
    val startTime = long("start_time")
    val endTime = long("end_time")
    val status = enumerationByName("status", MAX_ENUM_LENGTH, RoomReservationStatus::class)
    val createdAt = long("created_at")

    init {
        check("room_reservation_end_after_start") { endTime greater startTime }
    }
}
