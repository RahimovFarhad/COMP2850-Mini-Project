package com.example

import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.Principal
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.UserPasswordCredential
import io.ktor.server.auth.form
import io.ktor.server.auth.session
import io.ktor.server.response.respondRedirect
import io.ktor.server.sessions.Sessions
import io.ktor.server.sessions.cookie

data class UserSession(val username: String, val count: Int = 1) : Principal

fun Application.configureAuthentication() {

    install(Sessions) {
        // Use the NON-reified overload (works with the candidates your compiler shows)
        cookie<UserSession>("user_session") {
            cookie.path = "/"
            cookie.httpOnly = true
        }
    }

    install(Authentication) {

        form("auth-form") {
            userParamName = "username"
            passwordParamName = "password"

            validate { cred: UserPasswordCredential ->
                if (UserDatabase.check(cred)) UserIdPrincipal(cred.name) else null
            }

            challenge {
                call.respondRedirect("/login")
            }
        }

        session<UserSession>("auth-session") {
            validate { session: UserSession ->
                if (session.username.isNotBlank()) session else null
            }

            challenge {
                call.respondRedirect("/login")
            }
        }
    }
}
