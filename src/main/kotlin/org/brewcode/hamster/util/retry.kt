package org.brewcode.hamster.util

import com.codeborne.selenide.Selenide.sleep
import com.fasterxml.jackson.databind.util.ExceptionUtil
import io.opentelemetry.sdk.internal.ThrowableUtil

data class Retryer(
    val name: String = "",
    val maxAttempts: Int = 1,
    val delay: Long = 0,
    val throwOnError: Boolean = true,
    val onFailAction: (err: Throwable) -> Unit = {},
    val mainAction: () -> Unit = {}
) {

    fun name(name: String) = copy(name = name)
    fun maxAttempts(maxAttempts: Int) = copy(maxAttempts = maxAttempts)
    fun noRetry() = copy(maxAttempts = 0)
    fun delay(delay: Long) = copy(delay = delay)
    fun ignoreErrors() = copy(throwOnError = false)
    fun onFail(onFailAction: (err: Throwable) -> Unit) = copy(onFailAction = onFailAction)
    fun action(mainAction: () -> Unit) = copy(mainAction = mainAction)

    fun evaluate() {
        var attempts = 0

        while (attempts <= maxAttempts) {
            try {
                mainAction()
                return
            } catch (err: Throwable) {
                attempts++

                logger.error(err.rootCause()) { "Evaluation '$name' error: ${err.localizedMessage}" }
                sleep(delay)

                runCatching { onFailAction(err) }
                    .onFailure { logger.error(it.rootCause()) { "Evaluate '$name' on-fail action: ${it.localizedMessage}" } }
            }
        }

        if (throwOnError)
            throw Exception("Evaluation '$name' failed after $attempts attempts")
    }

    companion object {
        fun retry(name: String, action: () -> Unit = {}) = Retryer(name, mainAction = action)
    }
}
