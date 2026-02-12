package com.example.database

import org.jetbrains.exposed.dao.id.IntIdTable

object UsersTable : IntIdTable("user", "user_id") {
    val name = varchar("name", MAX_VARCHAR_LENGTH)
    val email = varchar("email", MAX_VARCHAR_LENGTH).uniqueIndex()
    val role = enumerationByName("role", MAX_ENUM_LENGTH, UserRole::class)
    val homeAddress = varchar("home_address", MAX_VARCHAR_LENGTH).nullable()
    val phoneNumber = varchar("phone_number", MAX_VARCHAR_LENGTH).nullable()
    val age = integer("age").nullable()
}
