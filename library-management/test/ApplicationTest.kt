package com.example.test

import com.example.testModule
import com.example.database.Book
import com.example.database.Copy
import com.example.database.CopyAvailabilityStatus
import com.example.database.Reservation
import com.example.database.ReservationStatus
import com.example.database.UserRole
import com.example.database.Users
import com.example.repository.BookRepository
import com.example.repository.ReservationRepository
import com.example.service.BookService
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import org.jetbrains.exposed.sql.transactions.transaction

@Suppress("unused")
class ApplicationTest : StringSpec({
    "Home page loads correctly" {
        testApplication {
            application { testModule() }
            val response = client.get("/").also { checkForHtml(it) }
            response.bodyAsText().let {
                it shouldContain "<h1>Library Management</h1>"
            }
        }
    }

    "Books page loads and lists books correctly" {
        testApplication {
            application { testModule() }
            val response = client.get("/books").also { checkForHtml(it) }
            response.bodyAsText().let {
                it shouldContain "<h2>Books</h2>"
                it shouldContain "Test Book"
                it shouldContain "Test Author"
            }
        }
    }

    "Book finding by ISBN works" {
        testApplication {
            application { testModule() }

            val bookService = BookService(BookRepository())
            val foundBook = bookService.getBookByISBN("1234567890")

            foundBook shouldNotBe null
            foundBook!!.title shouldBe "Test Book"
            foundBook.author shouldBe "Test Author"
            foundBook.isbn shouldBe "1234567890"
        }
    }

    "Book reserving marks copy as reserved and creates active reservation" {
        testApplication {
            application { testModule() }

            val reservationRepository = ReservationRepository()
            val (copyId, userId) = transaction {
                val bookService = BookService(BookRepository())
                val book = bookService.getBookByISBN("1234567890")

                book shouldNotBe null

                val copy = Copy.new {
                    this.book = book!!
                    this.availabilityStatus = CopyAvailabilityStatus.available
                    this.location = "Shelf-A1"
                }
                val user = Users.new {
                    name = "Test User"
                    email = "test.user@example.com"
                    role = UserRole.member
                    homeAddress = null
                    phoneNumber = null
                    age = null
                }
                copy.id.value to user.id.value
            }

            val reserved = reservationRepository.reserveCopy(copyId, userId)
            reserved shouldBe true

            transaction {
                val copy = Copy.findById(copyId)
                copy shouldNotBe null
                copy!!.availabilityStatus shouldBe CopyAvailabilityStatus.reserved

                val reservation = Reservation.find { com.example.database.ReservationTable.copy eq copy.id }.firstOrNull()
                reservation shouldNotBe null
                reservation!!.user.id.value shouldBe userId
                reservation.status shouldBe ReservationStatus.active
            }
        }
    }

    "Book reserving marks copy as reserved and creates active reservation" {
        testApplication {
            application { testModule() }

            val reservationRepository = ReservationRepository()
            val (copyId, userId) = transaction {
                val book = Book.find { com.example.database.BookTable.isbn eq "1234567890" }.first()
                val copy = Copy.new {
                    this.book = book
                    this.availabilityStatus = CopyAvailabilityStatus.available
                    this.location = "Shelf-A1"
                }
                val user = Users.new {
                    name = "Test User"
                    email = "test.user@example.com"
                    role = UserRole.member
                    homeAddress = null
                    phoneNumber = null
                    age = null
                }
                copy.id.value to user.id.value
            }

            val reserved = reservationRepository.reserveCopy(copyId, userId)
            reserved shouldBe true

            transaction {
                val copy = Copy.findById(copyId)
                copy shouldNotBe null
                copy!!.availabilityStatus shouldBe CopyAvailabilityStatus.reserved

                val reservation = Reservation.find { com.example.database.ReservationTable.copy eq copy.id }.firstOrNull()
                reservation shouldNotBe null
                reservation!!.user.id.value shouldBe userId
                reservation.status shouldBe ReservationStatus.active
            }
        }
    }
})

fun checkForHtml(response: HttpResponse) {
    response.status shouldBe HttpStatusCode.OK
    response.headers["Content-Type"]?.shouldContain("text/html")
}
