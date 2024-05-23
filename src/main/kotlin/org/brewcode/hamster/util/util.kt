package org.brewcode.hamster.util

fun Int.rnd(left: Number = -20, right: Number = 20) = this + (left.toInt()..right.toInt()).random()
fun Long.rnd(left: Number = -20, right: Number = 20) = this + (left.toInt()..right.toInt()).random()

fun retry(times: Int = 1, onFail: (err: Throwable) -> Unit = {}, block: () -> Unit) {
    var count = 0
    while (count <= times) {
        try {
            block()
            return
        } catch (err: Throwable) {
            logger.info { "Error on retry: ${err.localizedMessage}" }
            runCatching { onFail(err) }
                .onFailure { logger.info { "Error on repair action: ${it.localizedMessage}" } }
            count++
        }
    }

    throw Exception("Failed after $times retries")
}
