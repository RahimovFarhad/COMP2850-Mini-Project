package com.example.database

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class Copy(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Copy>(CopyTable)

    var book by Book referencedOn CopyTable.book
    var availabilityStatus by CopyTable.availabilityStatus
    var location by CopyTable.location

    val reservations by Reservation referrersOn ReservationTable.copy
    val borrowings by Borrowing referrersOn BorrowingTable.copy

    override fun toString(): String = "Copy of ${book.title} at $location"
}
