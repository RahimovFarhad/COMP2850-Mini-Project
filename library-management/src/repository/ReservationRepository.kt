package com.example.repository

import com.example.database.Copy
import com.example.database.CopyTable
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.transactions.transaction

import com.example.database.CopyAvailabilityStatus
import com.example.database.Users

import com.example.database.Reservation

import com.example.database.ReservationStatus

class ReservationRepository {
    fun reserveCopy(copyId: Int, userId: Int): Boolean = transaction {
        val user = Users.findById(userId) ?: return@transaction false

        val updatedRows = CopyTable.update({
            (CopyTable.id eq copyId) and (CopyTable.availabilityStatus eq CopyAvailabilityStatus.available)
        }) {
            it[availabilityStatus] = CopyAvailabilityStatus.reserved
        }
        if (updatedRows != 1) return@transaction false

        val copy = Copy.findById(copyId) ?: return@transaction false
        Reservation.new {
            this.copy = copy
            this.user = user
            this.dateReserved = System.currentTimeMillis()
            this.timeLimit = System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000 // 7 days
            this.status = ReservationStatus.active
        }

        return@transaction true
    }

    fun cancelReservation(reservationId: Int, userId: Int): Boolean = transaction {
        val reservation = Reservation.findById(reservationId) ?: return@transaction false
        if (reservation.user.id.value != userId) return@transaction false
        if (reservation.status != ReservationStatus.active) return@transaction false

        val copyId = reservation.copy.id.value
        val releasedRows = CopyTable.update({
            (CopyTable.id eq copyId) and (CopyTable.availabilityStatus eq CopyAvailabilityStatus.reserved)
        }) {
            it[availabilityStatus] = CopyAvailabilityStatus.available
        }
        if (releasedRows != 1) return@transaction false

        reservation.status = ReservationStatus.cancelled
        return@transaction true
    }
}
