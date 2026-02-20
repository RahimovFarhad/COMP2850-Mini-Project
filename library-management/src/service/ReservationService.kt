package com.example.service

import com.example.repository.ReservationRepository

class ReservationService(private val reservationRepo: ReservationRepository) {
    fun reserveCopy(copyId: Int, userId: Int): Boolean {
        require(copyId > 0) { "copyId must be positive" }
        require(userId > 0) { "userId must be positive" }
        return reservationRepo.reserveCopy(copyId, userId)
    }

    fun cancelReservation(reservationId: Int, userId: Int): Boolean {
        require(reservationId > 0) { "reservationId must be positive" }
        require(userId > 0) { "userId must be positive" }
        return reservationRepo.cancelReservation(reservationId, userId)
    }
}
