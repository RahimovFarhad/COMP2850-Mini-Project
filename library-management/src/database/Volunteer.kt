package com.example.database

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass

class Volunteer(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Volunteer>(VolunteerTable)

    var user by Users referencedOn VolunteerTable.user
    var shiftStart by VolunteerTable.shiftStart
    var shiftEnd by VolunteerTable.shiftEnd
    var taskDescription by VolunteerTable.taskDescription
}
