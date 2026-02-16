package com.example.database

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass

class Borrowing(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Borrowing>(BorrowingTable)

    var user by Users referencedOn BorrowingTable.user
    var copy by Copy referencedOn BorrowingTable.copy
    var dateIn by BorrowingTable.dateIn
    var dateOut by BorrowingTable.dateOut
    var status by BorrowingTable.status
}
