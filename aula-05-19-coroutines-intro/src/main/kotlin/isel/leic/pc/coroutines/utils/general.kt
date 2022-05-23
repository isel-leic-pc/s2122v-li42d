package isel.leic.pc.coroutines.utils

import kotlin.coroutines.CoroutineContext

fun CoroutineContext.list() : String{
    val builder = StringBuilder("")

    fold(builder) {
            builder, ctx->
        builder.append(ctx.toString())
        builder.append("\n")
        builder

    }
    return builder.toString()
}