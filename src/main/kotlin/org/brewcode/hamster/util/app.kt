package org.brewcode.hamster.util

import com.codeborne.selenide.Configuration
import com.codeborne.selenide.Selenide
import com.codeborne.selenide.WebDriverRunner
import io.appium.java_client.android.AndroidDriver
import io.appium.java_client.android.options.UiAutomator2Options
import io.appium.java_client.service.local.AppiumDriverLocalService
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.io.path.Path
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

private val logger = KotlinLogging.logger {}

fun configureSession() {

    val cfg = Path("brew-hamster.yaml").fromYaml<BrewConfiguration>()

    val options = UiAutomator2Options()
        .setAutomationName(cfg.appium.automationName ?: "UiAutomator2")
        .setDeviceName(cfg.appium.deviceName)
        .setPlatformName(cfg.appium.platformName ?: "Android")
        .setPlatformVersion(cfg.appium.platformVersion ?: "14")
        .setNewCommandTimeout(5.minutes.toJavaDuration())

    logger.debug { "Session starting... $options" }

    val appium = AppiumDriverLocalService.buildDefaultService().apply {
        clearOutPutStreams()
        enableDefaultSlf4jLoggingOfOutputData()
    }

    val driver = AndroidDriver(appium, options)
    WebDriverRunner.setWebDriver(driver)

    Configuration.timeout = 10.seconds.inWholeMilliseconds
    Configuration.pollingInterval = 500

    Runtime.getRuntime().addShutdownHook(Thread {
        appium.stop()
        appium.close()
        Selenide.closeWebDriver()
    })

    val optionForInspector = UiAutomator2Options()
        .setAutomationName("UiAutomator2")
        .setPlatformName("Android")
        .toJson()
        .toJson()

    logger.info { "Session started! Options for inspector:\n$optionForInspector" }
}

// appium:
//  deviceName: emulator-5554
//  platformVersion: 14

data class BrewConfiguration(
    val appium: Appium,
    val hamster: Hamster,
    val advanced: Advanced
)

data class Appium(
    val automationName: String?,
    val deviceName: String?,
    val platformName: String?,
    val platformVersion: String?,
    val newCommandTimeout: Long?
)

data class Hamster(
    val timeoutHours: Int,
    val staminaWaitIntervalMin: Int,
    val minCost: Int,
    val autoMoveMouse: Boolean,
    val buyUpgrades: Boolean,
    val desireUpgrades: List<String> = listOf(),
    val excludeUpgrades: List<String> = listOf(),
    val upgradeCostFactor: Double = 1.5
)

data class Advanced(
    val staminaCheckPeriodSec: Int,
    val staminaMinimumLevel: Int,
    val upgradeCostBackpressureFactor: Double
)

fun main() {
    logger.error { "Error!" }
    logger.warn { "Warning!" }
    logger.info { "Info!" }
    logger.debug { "Debug!" }
    logger.trace { "Trace!" }
}
