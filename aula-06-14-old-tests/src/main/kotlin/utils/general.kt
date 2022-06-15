package isel.leic.pc.kotlin_concurrency.utils

import kotlin.coroutines.CoroutineContext

fun CoroutineContext.elements() : String{
    val builder = StringBuilder("")

    fold(builder) {
            builder, ctx->
        builder.append(ctx.toString())
        builder.append("\n")
        builder

    }
    return builder.toString()
}