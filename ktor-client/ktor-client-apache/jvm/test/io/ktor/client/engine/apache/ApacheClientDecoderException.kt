import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.*

/**
 * Run this main to get the following exception:
 *
 * Exception in thread "main" java.lang.IllegalStateException:
 * Decoder shouldn't be completed while the coroutine is on suspension
 *
 * The downloaded file is 8175 bytes long (all the letter X in this example,
 * the exact content does not seem to matter). The same exception occurs with
 * the lengths 12263, 16351 (then I stopped trying).
 */
fun main() {

    val uri = "http://localhost/8175.txt"

    runBlocking {
        val response = HttpClient(Apache).request<String>(uri)
        //response.readText()
    }
}
