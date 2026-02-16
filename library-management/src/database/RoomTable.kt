package com.example.database

import org.jetbrains.exposed.dao.id.IntIdTable

object RoomTable : IntIdTable("room", "room_id") {
    val roomName = varchar("room_name", MAX_VARCHAR_LENGTH)
    val capacity = integer("capacity")
    val availabilityStatus = enumerationByName("availability_status", MAX_ENUM_LENGTH, RoomAvailabilityStatus::class)
}
