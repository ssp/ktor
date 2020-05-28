package io.ktor.utils.io.core

import io.ktor.utils.io.bits.*
import io.ktor.utils.io.core.internal.*

public fun Input.readShort(): Short {
    return readPrimitive(2, { memory, index -> memory.loadShortAt(index) }, { readShortFallback() })
}

private fun Input.readShortFallback(): Short {
    return readPrimitiveFallback(2) { it.readShort() }
}

public fun Input.readInt(): Int {
    return readPrimitive(4, { memory, index -> memory.loadIntAt(index) }, { readIntFallback() })
}

private fun Input.readIntFallback(): Int {
    return readPrimitiveFallback(4) { it.readInt() }
}

public fun Input.readLong(): Long {
    return readPrimitive(8, { memory, index -> memory.loadLongAt(index) }, { readLongFallback() })
}

private fun Input.readLongFallback(): Long {
    return readPrimitiveFallback(8) { it.readLong() }
}

public fun Input.readFloat(): Float {
    return readPrimitive(4, { memory, index -> memory.loadFloatAt(index) }, { readFloatFallback() })
}

private fun Input.readFloatFallback(): Float {
    return readPrimitiveFallback(4) { it.readFloat() }
}

public fun Input.readDouble(): Double {
    return readPrimitive(8, { memory, index -> memory.loadDoubleAt(index) }, { readDoubleFallback() })
}

private fun Input.readDoubleFallback(): Double {
    return readPrimitiveFallback(8) { it.readDouble() }
}

private inline fun <R> Input.readPrimitive(size: Int, main: (Memory, Int) -> R, fallback: () -> R): R {
    if (this is AbstractInput && headRemaining > size) {
        val index = headPosition
        headPosition = index + size
        return main(headMemory, index)
    }

    return fallback()
}

private inline fun <R> Input.readPrimitiveFallback(size: Int, read: (Buffer) -> R): R {
    val head = prepareReadFirstHead(size) ?: prematureEndOfStream(size)
    val value = read(head)
    completeReadHead(head)
    return value
}
