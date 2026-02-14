// Set up application routing & request handling
package com.example
<<<<<<< Updated upstream

import com.example.repository.BookRepository
import com.example.repository.ReservationRepository
import com.example.service.BookService
import com.example.service.ReservationService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.pebble.respondTemplate
import io.ktor.server.request.receiveParameters
import io.ktor.server.response.respondText
import io.ktor.server.routing.*
=======
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.log
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.UserPasswordCredential
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.pebble.respondTemplate
import io.ktor.server.request.receiveParameters
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import io.ktor.server.sessions.clear
import io.ktor.server.sessions.sessions
import io.ktor.server.sessions.set
import io.ktor.server.util.getOrFail
>>>>>>> Stashed changes

import org.jetbrains.exposed.sql.transactions.transaction
import com.example.database.Book
import com.example.database.Copy


fun Application.configureRouting() {
    val bookRepository = BookRepository()
    val bookService = BookService(bookRepository)
    val reservationService = ReservationService(ReservationRepository())

    routing {
        get("/") { call.homePage() }
        get("/register") { call.registrationPage() }
        post("/register") { call.registerUser() }
        get("/login") { call.loginPage() }
        authenticate("auth-form") {
            post("/login") { call.login() }
        }
        authenticate("auth-session") {
            get("/private") { call.privatePage() }
            get("/logout") { call.logout() }
        }
        get("/books") {
            call.displayBooks()
        }
        get("/books/search") {
            val isbn = call.request.queryParameters["isbn"]?.trim().orEmpty()
            val title = call.request.queryParameters["title"]?.trim().orEmpty()
            val author = call.request.queryParameters["author"]?.trim().orEmpty()
            val query = call.request.queryParameters["query"]?.trim().orEmpty()

            val books = when {
                isbn.isNotEmpty() -> listOfNotNull(bookService.getBookByISBN(isbn))
                title.isNotEmpty() -> bookService.searchBooksByTitle(title)
                author.isNotEmpty() -> bookService.searchBooksByAuthor(author)
                query.isNotEmpty() -> bookService.searchByAny(query)
                else -> emptyList()
            }

            if (books.isEmpty()) {
                call.respondText("No books found.")
            } else {
                val body = books.joinToString("\n") {
                    "id=${it.id.value}, title=${it.title}, author=${it.author}, isbn=${it.isbn}"
                }
                call.respondText(body)
            }
        }
        get("/books/{id}") {
            val id = call.parameters["id"]?.trim().orEmpty()
            if (id.isEmpty()) {
                call.respondText("id is required", status = HttpStatusCode.BadRequest)
                return@get
            }

            val book = bookService.getBookById(id.toInt())
            if (book == null) {
                call.respondText("Book not found", status = HttpStatusCode.NotFound)
            } else {
                call.respondText("id=${id}, title=${book.title}, author=${book.author}, isbn=${book.isbn}")
            }
        }
        get("/copies") {
            call.displayCopies()
        }
        post("/reservations/reserve") {
            val params = call.receiveParameters()
            val copyId = (call.request.queryParameters["copyId"] ?: params["copyId"])?.toIntOrNull()
            val userId = (call.request.queryParameters["userId"] ?: params["userId"])?.toIntOrNull()
            if (copyId == null || userId == null) {
                call.respondText("copyId and userId are required integers", status = HttpStatusCode.BadRequest)
                return@post
            }

            val ok = try {
                reservationService.reserveCopy(copyId, userId)
            } catch (err: IllegalArgumentException) {
                false
            }

            if (ok) {
                call.respondText("Reservation created.")
            } else {
                call.respondText("Reservation failed.", status = HttpStatusCode.Conflict)
            }
        }
        post("/reservations/{reservationId}/cancel") {
            val id = call.parameters["reservationId"]?.toIntOrNull()
            if (id == null) {
                call.respondText("reservationId must be an integer", status = HttpStatusCode.BadRequest)
                return@post
            }

            val ok = try {
                reservationService.cancelReservation(id)
            } catch (_: IllegalArgumentException) {
                false
            }
            if (ok) {
                call.respondText("Reservation cancelled.")
            } else {
                call.respondText("Cancel failed.", status = HttpStatusCode.Conflict)
            }
        }
    }
}

private suspend fun ApplicationCall.homePage() {
    respondTemplate("index.peb", model = mapOf("users" to UserDatabase.size))
}

<<<<<<< Updated upstream
private suspend fun ApplicationCall.displayBooks() {
    val books = transaction {
        Book.all().toList()
    }

    respondTemplate("books.peb", model = mapOf("books" to books))
}

private suspend fun ApplicationCall.displayCopies() {
    val copiesForView = transaction {
        Copy.all().map {
            mapOf(
                "bookTitle" to it.book.title,
                "availabilityStatus" to it.availabilityStatus.name,
                "location" to it.location
            )
        }
    }
    respondTemplate("copies.peb", mapOf("copies" to copiesForView))
}
=======
private suspend fun ApplicationCall.registrationPage() {
    respondTemplate("register.peb", model = emptyMap())
}

private suspend fun ApplicationCall.registerUser() {
    val credentials = getCredentials()
    val result = runCatching {
        UserDatabase.addUser(credentials)
    }
    if (result.isSuccess) {
        application.log.info("User ${credentials.name} registered")
        respondRedirect("/")
    }
    else {
        val error = result.exceptionOrNull()?.message ?: ""
        respondTemplate("register.peb", model = mapOf("error" to error))
    }
}

private suspend fun ApplicationCall.getCredentials(): UserPasswordCredential {
    val formParams = receiveParameters()
    val username = formParams.getOrFail("username")
    val password = formParams.getOrFail("password")
    return UserPasswordCredential(username, password)
}

private suspend fun ApplicationCall.loginPage() {
    respondTemplate("login.peb", model = emptyMap())
}

private suspend fun ApplicationCall.login() {
    val username = principal<UserIdPrincipal>()?.name.toString()
    application.log.info("User $username logged in")
    sessions.set(UserSession(username, 1))
    respondRedirect("/private")
}

private suspend fun ApplicationCall.privatePage() {
    val session = principal<UserSession>()
    // Increment visit count & update session cookie
    sessions.set(session?.copy(count = session.count + 1))

    respondTemplate("private.peb", model = mapOf(
        "username" to session?.username.toString(),
        "visits" to (session?.count ?: 0),
    ))
}

private suspend fun ApplicationCall.logout() {
    val username = principal<UserSession>()?.username.toString()
    application.log.info("User $username logged out")
    sessions.clear<UserSession>()
    respondRedirect("/")
}
>>>>>>> Stashed changes
