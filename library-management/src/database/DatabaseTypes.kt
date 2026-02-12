package com.example.database

const val MAX_VARCHAR_LENGTH = 255
const val MAX_ENUM_LENGTH = 32

enum class CopyAvailabilityStatus {
    available,
    borrowed,
    reserved,
    not_returned,
    inaccessible
}

enum class UserRole {
    staff,
    volunteer,
    member
}

enum class BorrowingStatus {
    not_returned,
    returned
}

enum class ReservationStatus {
    active,
    fulfilled,
    cancelled,
    expired
}

enum class RoomAvailabilityStatus {
    available,
    booked,
    maintenance
}

enum class RoomReservationStatus {
    booked,
    cancelled,
    completed,
    no_show
}
