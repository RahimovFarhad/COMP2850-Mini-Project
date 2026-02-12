package com.example.database

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.dao.id.IntIdTable

object CopyTable : IntIdTable("copy", "copy_id") {
    val book = reference("book_id", BookTable, ReferenceOption.CASCADE)
    val availabilityStatus = enumerationByName("availability_status", MAX_ENUM_LENGTH, CopyAvailabilityStatus::class)
    val location = varchar("location", MAX_VARCHAR_LENGTH)
}
