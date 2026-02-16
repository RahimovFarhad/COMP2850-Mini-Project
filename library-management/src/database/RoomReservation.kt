package com.example.database

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass

class RoomReservation(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<RoomReservation>(RoomReservationTable)

    var room by Room referencedOn RoomReservationTable.room
    var user by Users referencedOn RoomReservationTable.user
    var startTime by RoomReservationTable.startTime
    var endTime by RoomReservationTable.endTime
    var status by RoomReservationTable.status
    var createdAt by RoomReservationTable.createdAt
}
