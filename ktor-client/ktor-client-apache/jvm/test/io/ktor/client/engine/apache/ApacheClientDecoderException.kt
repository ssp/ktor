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
 * The downloaded file is 8176 bytes long (8175 + a final newline).
 * The same exception occurs with the lengths 12263+1, 16351+1
 * (then I stopped trying).
 */
fun main() {

    // URI referencing the special file 8175.txt on github.
    val uri = "https://raw.githubusercontent.com/ssp/ktor/4f1986df68e3594714ea12949c8af8274be99d01/ktor-client/ktor-client-apache/jvm/test/io/ktor/client/engine/apache/8175.txt"

    runBlocking {
        val response = HttpClient(Apache).request<String>(uri)
    }
}
