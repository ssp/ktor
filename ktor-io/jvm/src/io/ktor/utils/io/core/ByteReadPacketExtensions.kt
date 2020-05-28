@file:Suppress("FunctionName")

package io.ktor.utils.io.core

import io.ktor.utils.io.core.internal.*
import io.ktor.utils.io.pool.*
import java.nio.*

public actual inline fun ByteReadPacket(array: ByteArray, offset: Int, length: Int, crossinline block: (ByteArray) -> Unit): ByteReadPacket {
    return ByteReadPacket(ByteBuffer.wrap(array, offset, length)) { block(array) }
}

public fun ByteReadPacket(buffer: ByteBuffer, release: (ByteBuffer) -> Unit = {}): ByteReadPacket {
    val pool = poolFor(buffer, release)
    val view = pool.borrow().apply { resetForRead() }
    return ByteReadPacket(view, pool)
}

private fun poolFor(bb: ByteBuffer, release: (ByteBuffer) -> Unit): ObjectPool<ChunkBuffer> {
    return SingleByteBufferPool(bb, release)
}

private class SingleByteBufferPool(val instance: ByteBuffer, val release: (ByteBuffer) -> Unit) :
    SingleInstancePool<ChunkBuffer>() {
    override fun produceInstance(): ChunkBuffer {
        @Suppress("DEPRECATION")
        return IoBuffer(instance)
    }

    override fun disposeInstance(instance: ChunkBuffer) {
        @Suppress("DEPRECATION")
        check(instance is IoBuffer) { "Only IoBuffer could be recycled" }
        release(this.instance)
    }
}
