package com.example.repository

import com.example.database.Book
import com.example.database.BookTable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.transactions.transaction

class BookRepository {
    fun findById(id: Int): Book? = transaction{
        Book.find { BookTable.id eq id }.firstOrNull()
    }
    fun findByISBN(isbn: String): Book? = transaction{
        val isbn_trim = isbn.trim()
        Book.find { BookTable.isbn eq isbn_trim }.firstOrNull()
    }
    fun findByTitle(title: String): List<Book> = transaction {
        val title_trim = title.trim()
        Book.find { BookTable.title like "%$title_trim%" }.toList()
    }
    fun findByAuthor(author: String): List<Book> = transaction {
        val author_trim = author.trim()
        Book.find { BookTable.author like "%$author_trim%" }.toList()
    }
    fun findAll(): List<Book> = transaction {
        Book.all().toList()
    }

    fun findByAny(term: String): List<Book> = transaction {
        val q = term.trim()
        if (q.isEmpty()) return@transaction emptyList()

        Book.find {
            (BookTable.isbn eq q) or
            (BookTable.title like "%$q%") or
            (BookTable.author like "%$q%")
        }.toList()
    }

}
