/*
 * Copyright 2014-2019 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.client.features.logging

/**
 * [Logging]  log level.
 */
@Suppress("NO_EXPLICIT_VISIBILITY_IN_API_MODE_WARNING", "KDocMissingDocumentation")
public enum class LogLevel(
    public val info: Boolean,
    public val headers: Boolean,
    public val body: Boolean
) {
    ALL(true, true, true),
    HEADERS(true, true, false),
    BODY(true, false, true),
    INFO(true, false, false),
    NONE(false, false, false)
}
