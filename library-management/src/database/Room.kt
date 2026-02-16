package com.example.database

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass

class Room(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Room>(RoomTable)

    var roomName by RoomTable.roomName
    var capacity by RoomTable.capacity
    var availabilityStatus by RoomTable.availabilityStatus

    val reservations by RoomReservation referrersOn RoomReservationTable.room
}
