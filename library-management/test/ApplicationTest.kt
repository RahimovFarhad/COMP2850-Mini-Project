package com.example.test

import com.example.database.Book
import com.example.testModule
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.testing.testApplication
import org.jetbrains.exposed.sql.transactions.transaction

@Suppress("unused")
class ApplicationTest : StringSpec({
    "Home page loads correctly" {
        testApplication {
            environment { config = MapApplicationConfig() }
            application { testModule() }
            val response = client.get("/").also { checkForHtml(it) }
            response.bodyAsText().let {
                it shouldContain "<h1>Library Management</h1>"
            }
        }
    }

    "Books page loads and lists books correctly" {
        testApplication {
            environment { config = MapApplicationConfig() }
            application { testModule() }
            val response = client.get("/books").also { checkForHtml(it) }
            response.bodyAsText().let {
                it shouldContain "<h2>Books</h2>"
                it shouldContain "Test Book"
                it shouldContain "Test Author"
            }
        }
    }

    "Book search works (HTTP)" {
        testApplication {
            environment { config = MapApplicationConfig() }
            application { testModule() }

            client.get("/") // Triggering startup because of lazy load
            transaction {
                Book.new {
                    title = "Ktor in Action"
                    author = "Alex Doe"
                    isbn = "9000000001"
                }
                Book.new {
                    title = "Database Basics"
                    author = "Jane Roe"
                    isbn = "9000000002"
                }
            }

            val response = client.get("/books/search?query=Ktor")
            response.status shouldBe HttpStatusCode.OK
            response.bodyAsText().let {
                it shouldContain "title=Ktor in Action"
                it shouldContain "author=Alex Doe"
                it shouldContain "isbn=9000000001"
            }
        }
    }

    "Book find by id works (HTTP)" {
        testApplication {
            environment { config = MapApplicationConfig() }
            application { testModule() }

            client.get("/") // Triggering startup because of lazy load
            val targetId = transaction {
                Book.new {
                    title = "Refactoring Kotlin"
                    author = "Martin C."
                    isbn = "9000000003"
                }.id.value
            }
            transaction {
                Book.new {
                    title = "Another Book"
                    author = "Someone Else"
                    isbn = "9000000004"
                }
            }


            val response = client.get("/books/$targetId")
            response.status shouldBe HttpStatusCode.OK
            response.bodyAsText().let {
                it shouldContain "id=$targetId"
                it shouldContain "title=Refactoring Kotlin"
                it shouldContain "author=Martin C."
                it shouldContain "isbn=9000000003"
            }
        }
    }

    "Reserving and cancelling endpoints return expected HTTP responses" {
        testApplication {
            environment { config = MapApplicationConfig() }
            application { testModule() }

            val reserveBad = client.post("/reservations/reserve") {
                setBody(
                    FormDataContent(
                        Parameters.build {
                            append("copyId", "1")
                            append("userId", "1")
                        }
                    )
                )
            }
            reserveBad.status shouldBe HttpStatusCode.Conflict

            val cancelBad = client.post("/reservations/1/cancel")
            cancelBad.status shouldBe HttpStatusCode.Conflict
        }
    }
})

fun checkForHtml(response: HttpResponse) {
    response.status shouldBe HttpStatusCode.OK
    response.headers["Content-Type"]?.shouldContain("text/html")
}
