package com.example.repository

import com.example.database.Copy
import com.example.database.CopyTable
import com.example.database.Book
import com.example.database.BookTable
import org.jetbrains.exposed.sql.transactions.transaction

class CopyRepository {
    fun findByBookTitle(title: String): List<Copy> = transaction {
        Book.find { BookTable.title like "%$title%" }
            .flatMap { it.copies }
            .toList()
    }
    fun findByBookAuthor(author: String): List<Copy> = transaction {
        Book.find { BookTable.author like "%$author%" }
            .flatMap { it.copies }
            .toList()
    }
    fun findByBookISBN(isbn: String): List<Copy> = transaction {
        Book.find { BookTable.isbn eq isbn }
            .flatMap { it.copies }
            .toList()
    }
    fun findAll(): List<Copy> = transaction {
        Copy.all().toList()
    } 
}