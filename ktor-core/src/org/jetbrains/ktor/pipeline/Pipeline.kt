package org.jetbrains.ktor.pipeline

class Pipeline<T : Any>() {
    val interceptors = mutableListOf<PipelineContext<T>.(T) -> Unit>()

    constructor(interceptors: List<PipelineContext<T>.(T) -> Unit>) : this() {
        this.interceptors.addAll(interceptors)
    }

    fun intercept(block: PipelineContext<T>.(T) -> Unit) {
        interceptors.add(block)
    }

    fun intercept(index: Int, block: PipelineContext<T>.(T) -> Unit) {
        interceptors.add(index, block)
    }
}

class PipelineBranchCompleted : Throwable() {
    @Suppress("unused") // implicit override
    fun fillInStackTrace(): Throwable? {
        return null
    }
}