import com.password4j.Password
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.system.exitProcess
import kotlin.time.measureTime

const val HASH_FILENAME = "hash.txt"

fun register(password: String) {
    val hash = Password.hash(password).addRandomSalt(8).withScrypt()
    val output = hash.result + "\n"
    Files.writeString(Paths.get(HASH_FILENAME), output)

}

fun login(password: String) {
    val expectedHash = Files.readString(Paths.get(HASH_FILENAME))
    var loggedIn = false
    val timeToCheck = measureTime {
        loggedIn = Password.check(password, expectedHash).withScrypt()
    }
    println("Hash check took $timeToCheck")
    when (loggedIn) {
        true -> println("\u2705 Login succeeded!")
        false -> println("\u274c Login failed!")
    }

}

fun main(args: Array<String>) {
    when (args.size) {
        2 if args[0] == "--register" -> register(args[1])
        1 -> login(args[0])
        else -> {
            println("Error: invalid command line!")
            exitProcess(1)
        }
    }
}
