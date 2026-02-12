package com.example.database

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass

class Book(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Book>(BookTable)

    var title by BookTable.title
    var isbn by BookTable.isbn
    var author by BookTable.author

    val copies by Copy referrersOn CopyTable.book

    override fun toString(): String = "$title by $author"
}
