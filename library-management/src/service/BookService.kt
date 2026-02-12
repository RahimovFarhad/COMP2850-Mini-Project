package com.example.service

import com.example.database.Book
import com.example.repository.BookRepository

class BookService(private val bookRepo: BookRepository) {
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
    fun getAllBooks(): List<Book> = bookRepo.findAll()
    
}