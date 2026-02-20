// Data store for usernames and password hashes

import com.password4j.Password
import io.ktor.server.auth.UserPasswordCredential
import java.io.File

const val MIN_USERNAME_LENGTH = 4
const val MIN_PASSWORD_LENGTH = 8
const val AUTH_FILENAME = "auth.csv"

fun UserPasswordCredential.nameIsValid() = when {
    name.length < MIN_USERNAME_LENGTH -> false
    name.all { it.isLetterOrDigit() || it == '_' } -> true
    else -> false
}

fun UserPasswordCredential.passwordIsValid() = when {
    password.length < MIN_PASSWORD_LENGTH -> false
    password.any { it.isWhitespace() } -> false
    else -> true
}

object UserDatabase {
    private val authFile = File(AUTH_FILENAME)
    private val passwordMap = mutableMapOf<String,String>()

    init {
        if (! authFile.createNewFile()) {
            authFile.forEachLine {
                val (username, hash) = it.split(",")
                passwordMap[username] = hash
            }
        }
    }

    val size get() = passwordMap.size

    operator fun contains(username: String) = passwordMap.containsKey(username)

    fun addUser(cred: UserPasswordCredential) {
        require(cred.nameIsValid()) { "invalid username" }
        require(cred.name !in passwordMap) { "username already exists" }
        require(cred.passwordIsValid()) { "invalid password" }

        val hash = Password.hash(cred.password).addRandomSalt(8).withScrypt()
        passwordMap[cred.name] = hash.result
        authFile.appendText("${cred.name},${hash.result}\n")
    }

    fun check(cred: UserPasswordCredential): Boolean = when {
        cred.name !in passwordMap -> {
            println("DEBUG: Username '${cred.name}' not found. Available users: ${passwordMap.keys}")
            false
        }
        else -> {
            val hash = passwordMap[cred.name]!!
            println("DEBUG: Stored hash: $hash")
            println("DEBUG: Checking with password: '${cred.password}'")
            val isValid = Password.check(cred.password, hash).withScrypt()
            println("DEBUG: Result: $isValid")
            isValid
        }
    }
}