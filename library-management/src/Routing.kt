// Set up application routing & request handling
package com.example

import io.ktor.server.sessions.clear
import io.ktor.server.sessions.sessions
import io.ktor.server.sessions.set
import io.ktor.server.sessions.get

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

import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.SortOrder
import com.example.database.Book
import com.example.database.Copy
import com.example.database.BookTable
import com.example.database.CopyTable
import com.example.database.Reservation
import com.example.database.ReservationTable
import com.example.database.Users
import com.example.database.UsersTable
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

import com.example.database.UserRole
import com.example.database.CopyAvailabilityStatus

private data class CopyAggregateRow(
    val copyId: Int,
    val bookId: Int,
    val bookTitle: String,
    val author: String,
    val isbn: String,
    val availabilityStatus: String,
    val location: String
)

fun Application.configureRouting() {
    val bookRepository = BookRepository()
    val bookService = BookService(bookRepository)
    val reservationService = ReservationService(ReservationRepository())

    routing {
        get("/") { call.homePage() }
        get("/register") { call.registrationPage() }
        post("/register") { call.registerUser() }
        get("/login") { call.loginPage() }
        post("/login") { call.manualLogin() }
        authenticate("auth-session") {
            get("/logout") { call.logout() }

            post("/reservations/reserve") {
                val username = call.sessions.get<UserSession>()?.username?.trim().orEmpty()
                val userId = findOrCreateMemberUserId(username)
                if (userId == null) {
                    call.respondText("User not found", status = HttpStatusCode.Unauthorized)
                    return@post
                }

                val params = call.receiveParameters()
                val copyId = (call.request.queryParameters["copyId"] ?: params["copyId"])?.toIntOrNull()
                if (copyId == null) {
                    call.respondText("copyId is required integer", status = HttpStatusCode.BadRequest)
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
                val username = call.sessions.get<UserSession>()?.username?.trim().orEmpty()
                val userId = findOrCreateMemberUserId(username)
                if (userId == null) {
                    call.respondText("User not found", status = HttpStatusCode.Unauthorized)
                    return@post
                }

                val id = call.parameters["reservationId"]?.toIntOrNull()
                if (id == null) {
                    call.respondText("reservationId must be an integer", status = HttpStatusCode.BadRequest)
                    return@post
                }

                val ok = try {
                    reservationService.cancelReservation(id, userId)
                } catch (_: IllegalArgumentException) {
                    false
                }
                if (ok) {
                    call.respondText("Reservation cancelled.")
                } else {
                    call.respondText("Cancel failed.", status = HttpStatusCode.Conflict)
                }
            }
            post("/my-reservations/{reservationId}/cancel") {
                val username = call.sessions.get<UserSession>()?.username?.trim().orEmpty()
                val userId = findOrCreateMemberUserId(username)
                if (userId == null) {
                    call.respondRedirect("/login")
                    return@post
                }

                val id = call.parameters["reservationId"]?.toIntOrNull()
                if (id == null) {
                    call.respondRedirect("/my-reservations?message=Invalid%20reservation%20id")
                    return@post
                }

                val ok = try {
                    reservationService.cancelReservation(id, userId)
                } catch (_: IllegalArgumentException) {
                    false
                }

                if (ok) {
                    call.respondRedirect("/my-reservations?message=Reservation%20cancelled")
                } else {
                    call.respondRedirect("/my-reservations?message=Cancel%20failed")
                }
            }
            get("/my-reservations"){
                val username = call.sessions.get<UserSession>()?.username?.trim().orEmpty()
                val userId = findOrCreateMemberUserId(username)
                if (userId == null) {
                    call.respondText("User not found", status = HttpStatusCode.Unauthorized)
                    return@get
                }

                val reservationsForView = transaction {
                    Reservation.find {ReservationTable.user eq userId}
                        .toList()
                        .sortedByDescending { it.dateReserved }
                        .map {
                            mapOf(
                                "reservationId" to it.id.value,
                                "copyId" to it.copy.id.value,
                                "bookTitle" to it.copy.book.title,
                                "status" to it.status.name,
                                "dateReserved" to it.dateReserved
                            )
                        }
                }
                call.respondTemplate(
                    "my-reservations.peb",
                    mapOf(
                        "username" to username,
                        "reservations" to reservationsForView,
                        "message" to (call.request.queryParameters["message"] ?: "")
                    )
                )
            }
            put("/set-availability/{copyId}"){
                val username = call.sessions.get<UserSession>()?.username?.trim().orEmpty()
                val currentUser = transaction {
                    Users.find { UsersTable.name eq username }.firstOrNull()
                }
                if (currentUser == null) {
                    call.respondText("User not found", status = HttpStatusCode.Unauthorized)
                    return@put
                }
                if (currentUser.role == UserRole.member) {
                    call.respondText("Only library workers can change availability", status = HttpStatusCode.Forbidden)
                    return@put
                }

                val copyId = call.parameters["copyId"]?.toIntOrNull()
                if (copyId == null) {
                    call.respondText("copyId must be an integer", status = HttpStatusCode.BadRequest)
                    return@put
                }

                val newAvailabilityRaw = call.request.queryParameters["availability"]?.trim().orEmpty()
                val newAvailability = try {
                    CopyAvailabilityStatus.valueOf(newAvailabilityRaw.lowercase())
                } catch (_: IllegalArgumentException) {
                    call.respondText("Invalid availability value", status = HttpStatusCode.BadRequest)
                    return@put
                }

                val result = transaction {
                    val copy = Copy.findById(copyId) ?: return@transaction "NOT_FOUND"
                    val currentAvailability = copy.availabilityStatus
                    if (!isAvailabilityTransitionAllowed(currentAvailability, newAvailability)) {
                        return@transaction "INVALID_TRANSITION"
                    }
                    copy.availabilityStatus = newAvailability
                    return@transaction "OK"
                }

                when (result) {
                    "NOT_FOUND" -> call.respondText("Copy not found", status = HttpStatusCode.NotFound)
                    "INVALID_TRANSITION" -> call.respondText(
                        "Invalid availability transition from current state",
                        status = HttpStatusCode.Conflict
                    )
                    else -> call.respondText("Availability updated")
                }

            }
        }
        get("/books") {
            val isbn = call.request.queryParameters["isbn"]?.trim().orEmpty()
            val title = call.request.queryParameters["title"]?.trim().orEmpty()
            val author = call.request.queryParameters["author"]?.trim().orEmpty()
            val query = call.request.queryParameters["query"]?.trim().orEmpty()

            val books = when {
                isbn.isNotEmpty() -> listOfNotNull(bookService.getBookByISBN(isbn))
                title.isNotEmpty() -> bookService.searchBooksByTitle(title)
                author.isNotEmpty() -> bookService.searchBooksByAuthor(author)
                query.isNotEmpty() -> bookService.searchByAny(query)
                else -> bookService.getAllBooks()
            }

            call.displayBooks(books, isbn, title, author, query)
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
        
    }
}

private suspend fun ApplicationCall.homePage() {
    val username = sessions.get<UserSession>()?.username ?: ""
    val userRole = if (username.isBlank()) {
        ""
    } else {
        transaction {
            Users.find { UsersTable.name eq username }.firstOrNull()?.role?.name ?: ""
        }.orEmpty()
    }
    respondTemplate(
        "index.peb",
        model = mapOf(
            "users" to UserDatabase.size,
            "username" to username,
            "userRole" to userRole
        )
    )
}

private suspend fun ApplicationCall.displayBooks(
    books: List<Book>,
    isbn: String = "",
    title: String = "",
    author: String = "",
    query: String = ""
) {
    respondTemplate(
        "books.peb",
        model = mapOf(
            "books" to books,
            "isbn" to isbn,
            "titleFilter" to title,
            "authorFilter" to author,
            "queryFilter" to query
        )
    )
}

private suspend fun ApplicationCall.displayCopies() {
    val username = sessions.get<UserSession>()?.username ?: ""
    val userRole = if (username.isBlank()) {
        ""
    } else {
        transaction {
            Users.find { UsersTable.name eq username }.firstOrNull()?.role?.name ?: ""
        }.orEmpty()
    }

    val searchQuery = request.queryParameters["search"]?.trim().orEmpty()
    val searchQueryLower = searchQuery.lowercase()

    val copiesForView = transaction {
        (CopyTable innerJoin BookTable)
            .selectAll()
            .orderBy(BookTable.title to SortOrder.ASC, CopyTable.id to SortOrder.ASC)
            .map {
                CopyAggregateRow(
                    copyId = it[CopyTable.id].value,
                    bookId = it[BookTable.id].value,
                    bookTitle = it[BookTable.title],
                    author = it[BookTable.author],
                    isbn = it[BookTable.isbn],
                    availabilityStatus = it[CopyTable.availabilityStatus].name,
                    location = it[CopyTable.location]
                )
            }
            .groupBy { it.bookId }
            .values
            .map { rowsForBook ->
                val first = rowsForBook.first()
                val availableRows = rowsForBook.filter { it.availabilityStatus == CopyAvailabilityStatus.available.name }
                val firstAvailableCopyId = availableRows.firstOrNull()?.copyId
                val locationSummary = rowsForBook.map { it.location }.distinct().joinToString(", ")

                mapOf(
                    "bookId" to first.bookId,
                    "bookTitle" to first.bookTitle,
                    "author" to first.author,
                    "isbn" to first.isbn,
                    "totalCopies" to rowsForBook.size,
                    "availableCopies" to availableRows.size,
                    "firstAvailableCopyId" to firstAvailableCopyId,
                    "locations" to locationSummary
                )
            }
            .filter { row ->
                if (searchQueryLower.isBlank()) {
                    true
                } else {
                    val title = (row["bookTitle"] as String).lowercase()
                    val author = (row["author"] as String).lowercase()
                    val isbn = (row["isbn"] as String).lowercase()
                    val locations = (row["locations"] as String).lowercase()
                    title.contains(searchQueryLower) ||
                        author.contains(searchQueryLower) ||
                        isbn.contains(searchQueryLower) ||
                        locations.contains(searchQueryLower)
                }
            }
    }
    respondTemplate(
        "copies.peb",
        mapOf(
            "copies" to copiesForView,
            "username" to username,
            "userRole" to userRole,
            "searchQuery" to searchQuery
        )
    )
}
private suspend fun ApplicationCall.registrationPage() {
    respondTemplate("register.peb", model = emptyMap())
}

private suspend fun ApplicationCall.registerUser() {
    val credentials = getCredentials()
    val result = runCatching {
        UserDatabase.addUser(credentials)
        findOrCreateMemberUserId(credentials.name)
    }
    if (result.isSuccess) {
        application.log.info("User ${credentials.name} registered")
        respondTemplate("register.peb", model = mapOf("success" to true))
    }
    else {
        val error = result.exceptionOrNull()?.message ?: "Unknown error"
        respondTemplate("register.peb", model = mapOf("error" to error))
    }
}

private fun findOrCreateMemberUserId(username: String): Int? {
    if (username.isBlank()) return null

    return transaction {
        val existing = Users.find { UsersTable.name eq username }.firstOrNull()
        if (existing != null) {
            existing.id.value
        } else {
            Users.new {
                name = username
                email = "${username}@library.local"
                passwordHash = "managed_in_auth_csv"
                role = UserRole.member
            }.id.value
        }
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
}private suspend fun ApplicationCall.manualLogin() {
    val credentials = getCredentials()
    if (UserDatabase.check(credentials)) {
        application.log.info("User ${credentials.name} logged in")
        sessions.set(UserSession(credentials.name, 1))
        respondRedirect("/")
    } else {
        application.log.info("Login failed for user ${credentials.name}")
        respondTemplate("login.peb", model = mapOf("error" to true))
    }
}

private suspend fun ApplicationCall.login() {
    val username = principal<UserIdPrincipal>()?.name.toString()
    application.log.info("User $username logged in")
    sessions.set(UserSession(username, 1))
    respondRedirect("/")
}




private suspend fun ApplicationCall.logout() {
    val username = sessions.get<UserSession>()?.username.toString()
    application.log.info("User $username logged out")
    sessions.clear<UserSession>()
    respondRedirect("/")
}


private fun isAvailabilityTransitionAllowed(
    from: CopyAvailabilityStatus,
    to: CopyAvailabilityStatus
): Boolean {
    if (from == to) return true

    return when (from) {
        CopyAvailabilityStatus.available ->
            to == CopyAvailabilityStatus.borrowed ||
            to == CopyAvailabilityStatus.reserved ||
            to == CopyAvailabilityStatus.inaccessible
        CopyAvailabilityStatus.borrowed ->
            to == CopyAvailabilityStatus.not_returned ||
            to == CopyAvailabilityStatus.available ||
            to == CopyAvailabilityStatus.inaccessible
        CopyAvailabilityStatus.reserved ->
            to == CopyAvailabilityStatus.borrowed ||
            to == CopyAvailabilityStatus.available ||
            to == CopyAvailabilityStatus.inaccessible
        CopyAvailabilityStatus.not_returned ->
            to == CopyAvailabilityStatus.available ||
            to == CopyAvailabilityStatus.inaccessible
        CopyAvailabilityStatus.inaccessible ->
            to == CopyAvailabilityStatus.available
    }
}
