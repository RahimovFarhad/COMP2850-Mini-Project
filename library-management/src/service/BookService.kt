package com.example.service

import com.example.database.Book
import com.example.repository.BookRepository

class BookService(private val bookRepo: BookRepository) {
    fun getBookById(id: Int): Book? {
        require(id >= 0) { "Id must be a non-negative integer" }
        return bookRepo.findById(id)
    }
    fun getBookByISBN(isbn: String): Book? {
        require(isbn.isNotBlank()) { "ISBN cannot be blank" }
        return bookRepo.findByISBN(isbn)
    }
    fun searchBooksByTitle(title: String): List<Book> {
        require(title.isNotBlank()) { "Title cannot be blank" }
        return bookRepo.findByTitle(title)
    }
    fun searchBooksByAuthor(author: String): List<Book> {
        require(author.isNotBlank()) { "Author cannot be blank" }
        return bookRepo.findByAuthor(author)
    }
    fun searchByAny(term: String): List<Book> {
        require(term.isNotBlank()) {"Search term cannot be blank"}
        return bookRepo.findByAny(term)

    }
    fun getAllBooks(): List<Book> = bookRepo.findAll()
    
}