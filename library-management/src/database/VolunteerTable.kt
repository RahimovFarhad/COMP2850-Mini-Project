package com.example.database

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.dao.id.IntIdTable

object VolunteerTable : IntIdTable("volunteer", "volunteer_id") {
    val user = reference("user_id", UsersTable, ReferenceOption.CASCADE).uniqueIndex()
    val shiftStart = long("shift_start")
    val shiftEnd = long("shift_end")
    val taskDescription = varchar("task_description", MAX_VARCHAR_LENGTH)

    init {
        check("volunteer_shift_end_after_start") { shiftEnd greater shiftStart }
    }
}
