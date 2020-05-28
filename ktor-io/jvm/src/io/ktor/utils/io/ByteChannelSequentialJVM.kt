package io.ktor.utils.io

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import io.ktor.utils.io.core.*
import java.nio.ByteBuffer

@Suppress("EXPERIMENTAL_FEATURE_WARNING")
@ExperimentalIoApi
public class ByteChannelSequentialJVM(
    initial: IoBuffer, autoFlush: Boolean
) : ByteChannelSequentialBase(initial, autoFlush) {

    @Volatile
    private var attachedJob: Job? = null

    @OptIn(InternalCoroutinesApi::class)
    override fun attachJob(job: Job) {
        attachedJob?.cancel()
        attachedJob = job
        job.invokeOnCompletion(onCancelling = true) { cause ->
            attachedJob = null
            if (cause != null) {
                cancel(cause)
            }
        }
    }

    override suspend fun writeAvailable(src: ByteBuffer): Int {
        val rc = tryWriteAvailable(src)
        return when {
            rc > 0 -> rc
            !src.hasRemaining() -> 0
            else -> writeAvailableSuspend(src)
        }
    }

    private suspend fun writeAvailableSuspend(src: ByteBuffer): Int {
        awaitAtLeastNBytesAvailableForWrite(1)
        return writeAvailable(src)
    }

    override suspend fun writeFully(src: ByteBuffer) {
        tryWriteAvailable(src)
        if (!src.hasRemaining()) return

        writeFullySuspend(src)
    }

    private suspend fun writeFullySuspend(src: ByteBuffer) {
        while (src.hasRemaining()) {
            awaitAtLeastNBytesAvailableForWrite(1)
            val count = tryWriteAvailable(src)
            afterWrite(count)
        }
    }

    private fun tryWriteAvailable(src: ByteBuffer): Int {
        val srcRemaining = src.remaining()
        val availableForWrite = availableForWrite

        return when {
            closed -> throw closedCause ?: ClosedSendChannelException("Channel closed for write")
            srcRemaining == 0 -> 0
            srcRemaining <= availableForWrite -> {
                writable.writeFully(src)
                srcRemaining
            }
            availableForWrite == 0 -> 0
            else -> {
                val oldLimit = src.limit()
                src.limit(src.position() + availableForWrite)
                writable.writeFully(src)
                src.limit(oldLimit)
                availableForWrite
            }
        }
    }

    override suspend fun readAvailable(dst: ByteBuffer): Int {
        val rc = tryReadAvailable(dst)
        if (rc != 0) return rc
        if (!dst.hasRemaining()) return 0
        return readAvailableSuspend(dst)
    }

    private suspend fun readAvailableSuspend(dst: ByteBuffer): Int {
        if (!await(1)) return -1
        return readAvailable(dst)
    }

    override suspend fun readFully(dst: ByteBuffer): Int {
        val rc = tryReadAvailable(dst)
        if (rc == -1) throw EOFException("Channel closed")
        if (!dst.hasRemaining()) return rc

        return readFullySuspend(dst, rc)
    }

    private suspend fun readFullySuspend(dst: ByteBuffer, rc0: Int): Int {
        var count = rc0

        while (dst.hasRemaining()) {
            if (!await(1)) throw EOFException("Channel closed")
            val rc = tryReadAvailable(dst)
            if (rc == -1) throw EOFException("Channel closed")
            count += rc
        }

        return count
    }

    private fun tryReadAvailable(dst: ByteBuffer): Int {
        val closed = closed
        val closedCause = closedCause

        return when {
            closedCause != null -> throw closedCause
            closed -> {
                val count = readable.readAvailable(dst)

                if (count != 0) {
                    afterRead(count)
                    count
                } else {
                    -1
                }
            }
            else -> readable.readAvailable(dst).also { afterRead(it) }
        }
    }

    override fun <R> lookAhead(visitor: LookAheadSession.() -> R): R {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun <R> lookAheadSuspend(visitor: suspend LookAheadSuspendSession.() -> R): R {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun read(min: Int, consumer: (ByteBuffer) -> Unit) {
        require(min >= 0)

        if (!await(min)) throw EOFException("Channel closed while $min bytes expected")

        readable.readDirect(min) { buffer ->
            consumer(buffer)
        }
    }

    override suspend fun write(min: Int, block: (ByteBuffer) -> Unit) {
        if (closed) {
            throw closedCause ?: ClosedSendChannelException("Channel closed for write")
        }

        awaitAtLeastNBytesAvailableForWrite(min)
        val count = writable.writeDirect(min) { block(it) }
        afterWrite(count)
    }

    override suspend fun writeWhile(block: (ByteBuffer) -> Boolean) {
        while (true) {
            if (closed) {
                throw closedCause ?: ClosedSendChannelException("Channel closed for write")
            }

            var shouldContinue: Boolean = false
            awaitAtLeastNBytesAvailableForWrite(1)
            val result = writable.writeDirect(1) {
                shouldContinue = block(it)
            }

            afterWrite(result)
            if (!shouldContinue) break
        }
    }
}

