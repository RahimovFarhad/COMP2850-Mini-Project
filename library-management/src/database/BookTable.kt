package com.example.database

import org.jetbrains.exposed.dao.id.IntIdTable

object BookTable : IntIdTable("book", "book_id") {
    val title = varchar("title", MAX_VARCHAR_LENGTH)
    val isbn = varchar("ISBN", MAX_VARCHAR_LENGTH).uniqueIndex()
    val author = varchar("author", MAX_VARCHAR_LENGTH)
}
