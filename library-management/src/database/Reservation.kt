package com.example.database

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass

class Reservation(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Reservation>(ReservationTable)

    var user by Users referencedOn ReservationTable.user
    var copy by Copy referencedOn ReservationTable.copy
    var dateReserved by ReservationTable.dateReserved
    var timeLimit by ReservationTable.timeLimit
    var status by ReservationTable.status
}
