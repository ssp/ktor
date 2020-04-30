/*
 * Copyright 2014-2019 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.client.request

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.util.pipeline.*

/**
 * [HttpClient] Pipeline used for executing [HttpRequest].
 */
public class HttpRequestPipeline : Pipeline<Any, HttpRequestBuilder>(Before, State, Transform, Render, Send) {
    /**
     * All interceptors accept payload as [subject] and try to convert it to [OutgoingContent]
     * Last phase should proceed with [HttpClientCall]
     */
    public companion object Phases {
        /**
         * The earliest phase that happens before any other
         */
        public val Before = PipelinePhase("Before")

        /**
         * Use this phase to modify request with shared state
         */
        public val State = PipelinePhase("State")

        /**
         * Transform request body to supported render format
         */
        public val Transform = PipelinePhase("Transform")

        /**
         * Encode request body to [OutgoingContent]
         */
        public val Render = PipelinePhase("Render")

        /**
         * Phase for [HttpSend] feature
         */
        public val Send = PipelinePhase("Send")
    }
}

/**
 * [HttpClient] Pipeline used for sending [HttpRequest] to remote server.
 */
public class HttpSendPipeline : Pipeline<Any, HttpRequestBuilder>(Before, State, Monitoring, Engine, Receive) {

    public companion object Phases {
        /**
         * The earliest phase that happens before any other.
         */
        public val Before = PipelinePhase("Before")

        /**
         * Use this phase to modify request with shared state.
         */
        public val State = PipelinePhase("State")

        /**
         * Use this phase for logging and other actions that don't modify request or shared data.
         */
        public val Monitoring = PipelinePhase("Monitoring")

        /**
         * Send request to remote server.
         */
        public val Engine = PipelinePhase("Engine")

        /**
         * Receive pipeline execution phase.
         */
        public val Receive = PipelinePhase("Receive")
    }
}
