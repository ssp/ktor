package io.ktor.utils.io.core

import io.ktor.utils.io.core.internal.*
import io.ktor.utils.io.pool.*
import kotlin.contracts.*

public expect val PACKET_MAX_COPY_SIZE: Int

/**
 * Build a byte packet in [block] lambda. Creates a temporary builder and releases it in case of failure
 */
public inline fun buildPacket(headerSizeHint: Int = 0, block: BytePacketBuilder.() -> Unit): ByteReadPacket {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    val builder = BytePacketBuilder(headerSizeHint)
    try {
        block(builder)
        return builder.build()
    } catch (cause: Throwable) {
        builder.release()
        throw cause
    }
}

public expect fun BytePacketBuilder(headerSizeHint: Int = 0): BytePacketBuilder

/**
 * Discard all written bytes and prepare to build another packet.
 */

public fun BytePacketBuilder.reset() {
    release()
}

@DangerousInternalIoApi
@Deprecated("Will be removed in future releases.", level = DeprecationLevel.ERROR)
@Suppress("DEPRECATION_ERROR")
public abstract class BytePacketBuilderPlatformBase
internal constructor(pool: ObjectPool<ChunkBuffer>) : BytePacketBuilderBase(pool)

@DangerousInternalIoApi
@Deprecated("Will be removed in future releases", level = DeprecationLevel.ERROR)
@Suppress("DEPRECATION_ERROR")
public abstract class BytePacketBuilderBase
internal constructor(pool: ObjectPool<ChunkBuffer>) : AbstractOutput(pool)

