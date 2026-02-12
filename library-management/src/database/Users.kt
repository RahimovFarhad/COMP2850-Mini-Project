package com.example.database

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class Users(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Users>(UsersTable)

    var name by UsersTable.name
    var email by UsersTable.email
    var role by UsersTable.role
    var homeAddress by UsersTable.homeAddress
    var phoneNumber by UsersTable.phoneNumber
    var age by UsersTable.age

    val reservations by Reservation referrersOn ReservationTable.user
    val borrowings by Borrowing referrersOn BorrowingTable.user
    val roomReservations by RoomReservation referrersOn RoomReservationTable.user
    val volunteer by Volunteer optionalBackReferencedOn VolunteerTable.user

    override fun toString(): String = "User(name=$name, email=$email, role=$role)"
}
