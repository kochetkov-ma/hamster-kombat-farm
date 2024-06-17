package org.brewcode.hamster.service

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.oshai.kotlinlogging.KotlinLogging
import org.brewcode.hamster.Cfg
import org.brewcode.hamster.action.GameCommonAction
import org.brewcode.hamster.action.GameMineAction.buyUpgradeCard
import org.brewcode.hamster.action.GameMineAction.goToSection
import org.brewcode.hamster.action.GameMineAction.loadCards
import org.brewcode.hamster.service.UpgradeSection.*
import org.brewcode.hamster.service.UpgradeService.loadAndSaveNewSection
import org.brewcode.hamster.service.UpgradeService.loadUpgrades
import org.brewcode.hamster.service.UpgradeService.updateUpgrades
import org.brewcode.hamster.util.configureSession
import org.brewcode.hamster.util.fromJson
import org.brewcode.hamster.util.toJson
import org.brewcode.hamster.view.mine.MineView.dailyComboApplyButton
import java.time.LocalDateTime
import kotlin.io.path.*

private val logger = KotlinLogging.logger {}

object UpgradeService {

    private val info = Path("build/upgrade.json").also { if (!it.exists()) it.writeText("") }
    private val history = Path("build/history.json").also { if (!it.exists()) it.writeText("") }
    private var currentUpgrades = mutableMapOf<String, Upgrade>()
    var upgradeToBuy: Upgrade? = null
    val desireUpgrades = Cfg.desire_upgrades.toMutableList()
    val isEmptyUpgradesCache get() = currentUpgrades.isEmpty()

    init {
        loadUpgrades()
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    fun toBuy(upgrade: Upgrade) {
        upgradeToBuy = upgrade
    }

    fun toBuy() = upgradeToBuy!!
    fun hasToBuy() = upgradeToBuy != null
    fun clearToBuy() {
        desireUpgrades.remove(upgradeToBuy?.name)
        upgradeToBuy = null
    }

    fun loadUpgrades() {
        currentUpgrades = info.readText().let { if (it.isEmpty()) mutableMapOf() else it.fromJson<MutableMap<String, Upgrade>>() }
    }

    fun loadAndSaveNewSection(section: UpgradeSection) {
        val upgradesFromApp = loadCards(section).map { (newName, newUpgrade) ->
            val existing = currentUpgrades[newName]
            newName to if (existing != null) {
                val newHasNotRProfit = newUpgrade.relativeProfit == 0
                val oldHasRProfit = existing.relativeProfit > 0
                if (oldHasRProfit && newHasNotRProfit) newUpgrade.copy(relativeProfit = existing.relativeProfit) else newUpgrade
            } else
                newUpgrade
        }.toMap()

        val upgradesFromAppNames = upgradesFromApp.keys
        val incorrect = currentUpgrades.filter { it.value.section == section }.filter { it.key !in upgradesFromAppNames }
        if (incorrect.isNotEmpty()) {
            logger.warn { "Found incorrect or deprecated upgrades. Will be removed: $incorrect" }
            incorrect.keys.forEach { currentUpgrades.remove(it) }
        }

        logger.trace { "Save upgrades from APP: $upgradesFromApp" }
        currentUpgrades.putAll(upgradesFromApp)
    }

    private fun saveToFile() {
        val sorted = currentUpgrades.toList().sortedBy { it.second.totalMargin }.reversed().toMap()
        info.writeText(sorted.toJson())
    }

    fun updateUpgrades() {

        GameCommonAction.goToMine()

        logger.debug { "Read upgrade section: $Markets" }
        goToSection(Markets)
        loadAndSaveNewSection(Markets)

        logger.debug { "Read upgrade section: $PrTeam" }
        goToSection(PrTeam)
        loadAndSaveNewSection(PrTeam)

        logger.debug { "Read upgrade section: $Legal" }
        goToSection(Legal)
        loadAndSaveNewSection(Legal)

        logger.info { "Read upgrade section: $SpecialsMy" }
        goToSection(SpecialsMy)
        loadAndSaveNewSection(SpecialsMy)

        logger.debug { "Read upgrade section: $SpecialsNew" }
        goToSection(SpecialsNew)
        loadAndSaveNewSection(SpecialsNew)

        logger.info { "All section read successfully!" }
        GameCommonAction.goToExchange()

        saveToFile()
    }

    fun buyUpgrade(upgrade: Upgrade): Boolean {
        GameCommonAction.goToMine()
        goToSection(upgrade.section)
        val newUpgrade = runCatching { buyUpgradeCard(upgrade) }
            .onFailure { logger.error { "Error during buy upgrade: $upgrade" } }
            .getOrThrow()

        currentUpgrades[upgrade.name] = newUpgrade
        saveToFile()

        logger.debug { "Upgrade next level will be: $newUpgrade" }
        return newUpgrade != upgrade
    }

    fun saveToHistory(upgrade: Upgrade) {
        history.appendText("\n \"${LocalDateTime.now()}\": ${upgrade.toJson()} ,")
    }

    fun upgradeCalculator() = UpgradeCalculator(currentUpgrades, desireUpgrades)
}

fun applyCombo() {
    dailyComboApplyButton
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

data class Upgrade(
    val section: UpgradeSection,
    val name: String,
    val level: Int,
    val totalProfit: Int,
    val relativeProfit: Int,
    val cost: Int,
    val needText: String,
    @JsonProperty("unlocked")
    val isUnlocked: Boolean = true,
    val needUpgrade: Upgrade? = null,
    val timer: String = ""
) {

    @get:JsonProperty
    val totalMargin: Double get() = if (cost != 0) totalProfit.toDouble() / cost else 0.0

    @get:JsonProperty
    val relativeTotalMargin: Double get() = if (cost != 0) relativeProfit.toDouble() / cost else 0.0

    companion object {
        val none = Upgrade(None, "none", 0, 0, 0, 0, "")
    }

    override fun toString(): String {
        return "Upgrade($section > $name[lvl $level] -$$cost +$$totalProfit, relativeTotalMargin=$relativeTotalMargin, totalMargin=$totalMargin, relativeProfit=$relativeProfit, timer='$timer', isUnlocked=$isUnlocked, needText='$needText')"
    }
}

enum class UpgradeSection(vararg path: String) {
    None("None"),
    Markets("Markets"),
    PrTeam("Pr&Team"),
    Legal("Legal"),
    SpecialsMy("Specials", "My cards"),
    SpecialsNew("Specials", "New cards");

    companion object {
        val UpgradeSection.isSpecial get() = this in arrayOf(SpecialsMy, SpecialsNew)
    }
}

fun main() {
    configureSession()
    updateUpgrades()

//    loadUpgrades()
//    logger.info { "Read upgrade section: $SpecialsMy" }
//    goToSection(Markets)
//    loadAndSaveNewSection(Markets)
}
