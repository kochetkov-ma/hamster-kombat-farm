package org.brewcode.hamster.util

import com.codeborne.selenide.Configuration
import com.codeborne.selenide.Selenide
import com.codeborne.selenide.WebDriverRunner
import io.appium.java_client.android.AndroidDriver
import io.appium.java_client.android.options.UiAutomator2Options
import io.github.oshai.kotlinlogging.KotlinLogging
import java.net.URL
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

val logger = KotlinLogging.logger {}

// setAppPackage("org.telegram.messenger")
// setAppActivity("org.telegram.ui.LaunchActivity")
fun configureSession() {

    val options = UiAutomator2Options()
        .setAutomationName("UiAutomator2")
        .setDeviceName("emulator-5554")
        .setPlatformName("Android")
        .setPlatformVersion("14")
        .setNewCommandTimeout(5.minutes.toJavaDuration())

    val driver = AndroidDriver(URL("http://127.0.0.1:4723"), options)
    WebDriverRunner.setWebDriver(driver)

    Configuration.timeout = 10.seconds.inWholeMilliseconds
    Configuration.pollingInterval = 500

    Runtime.getRuntime().addShutdownHook(Thread {
        Selenide.closeWebDriver()
    })

    logger.info { "Session started\n${options.toJson().toJson()}" }
}
