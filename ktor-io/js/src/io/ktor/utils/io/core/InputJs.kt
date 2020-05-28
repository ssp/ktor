package io.ktor.utils.io.core

import io.ktor.utils.io.bits.Memory

/**
 * Shouldn't be implemented directly. Inherit [AbstractInput] instead.
 */
@Suppress("NO_ACTUAL_CLASS_MEMBER_FOR_EXPECTED_CLASS")
public actual interface Input : Closeable {
    @Deprecated(
        "Not supported anymore. All operations are big endian by default. " +
            "Read and readXXXLittleEndian or readXXX then X.reverseByteOrder() instead.",
        level = DeprecationLevel.ERROR
    )
    @Suppress("ACTUAL_WITHOUT_EXPECT")
    public actual var byteOrder: ByteOrder
        get() = ByteOrder.BIG_ENDIAN
        set(newValue) {
            if (newValue != ByteOrder.BIG_ENDIAN) {
                throw IllegalArgumentException("Only BIG_ENDIAN is supported")
            }
        }

    /**
     * It is `true` when it is known that no more bytes will be available. When it is `false` then this means that
     * it is not known yet or there are available bytes.
     * Please note that `false` value doesn't guarantee that there are available bytes so `readByte()` may fail.
     */
    public actual val endOfInput: Boolean

    /**
     * Copy at least [min] but up to [max] bytes to the specified [destination] buffer from this input
     * skipping [offset] bytes. If there are not enough bytes available to provide [min] bytes then
     * it fails with an exception.
     * It is safe to specify `max > destination.writeRemaining` but
     * `min` shouldn't be bigger than the [destination] free space.
     * This function could trigger the underlying source reading that may lead to blocking I/O.
     * It is safe to specify too big [offset] but only if `min = 0`, fails otherwise.
     * This function usually copy more bytes than [min] (unless `max = min`).
     *
     * @param destination to write bytes
     * @param offset to skip input
     * @param min bytes to be copied, shouldn't be greater than the buffer free space. Could be `0`.
     * @param max bytes to be copied even if there are more bytes buffered, could be [Int.MAX_VALUE].
     * @return number of bytes copied to the [destination] possibly `0`
     * @throws Throwable when not enough bytes available to provide
     */
    public actual fun peekTo(
        destination: Memory,
        destinationOffset: Long,
        offset: Long,
        min: Long,
        max: Long
    ): Long

    /**
     * Read the next upcoming byte
     * @throws EOFException if no more bytes available.
     */
    public actual fun readByte(): Byte

    /*
     * Returns next byte (unsigned) or `-1` if no more bytes available
     */
    public actual fun tryPeek(): Int

    public actual fun discard(n: Long): Long

    actual override fun close()
}
