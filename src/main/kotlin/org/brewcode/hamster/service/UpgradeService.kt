package org.brewcode.hamster.service

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.oshai.kotlinlogging.KotlinLogging
import org.brewcode.hamster.action.GameCommonAction
import org.brewcode.hamster.action.GameMineAction.buyUpgradeCard
import org.brewcode.hamster.action.GameMineAction.goToSection
import org.brewcode.hamster.action.GameMineAction.loadCards
import org.brewcode.hamster.service.UpgradeSection.*
import org.brewcode.hamster.service.UpgradeService.updateUpgrades
import org.brewcode.hamster.Cfg
import org.brewcode.hamster.util.configureSession
import org.brewcode.hamster.util.fromJson
import org.brewcode.hamster.util.toJson
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

private val logger = KotlinLogging.logger {}

object UpgradeService {

    private val info = Path("build/upgrade.json").also { if (!it.exists()) it.writeText("") }

    private var sessionTargetAlreadyBought = false
    private var currentUpgrades = mutableMapOf<String, Upgrade>()

    val isEmptyUpgradesCache get() = currentUpgrades.isEmpty()

    fun remove(upgrade: Upgrade) = currentUpgrades.remove(upgrade.name)

    fun loadUpgrades() {
        currentUpgrades = info.readText().let { if (it.isEmpty()) mutableMapOf() else it.fromJson<MutableMap<String, Upgrade>>() }
    }

    private fun loadAndSaveNewSection(section: UpgradeSection) {
        loadCards(section).map { (newName, newUpgrade) ->
            currentUpgrades.computeIfPresent(newName) { _, oldUpgrade ->

                val oldHasRProfit = oldUpgrade.relativeProfit > 0
                val newHasNotRProfit = newUpgrade.relativeProfit == 0

                if (oldHasRProfit && newHasNotRProfit) newUpgrade.copy(relativeProfit = oldUpgrade.relativeProfit) else newUpgrade

            } ?: currentUpgrades.put(newName, newUpgrade)
        }
    }

    fun updateUpgrades() {

        GameCommonAction.goToMine()

        logger.info { "Read upgrade section: $Markets" }
        goToSection(Markets)
        loadAndSaveNewSection(Markets)

        logger.info { "Read upgrade section: $PrTeam" }
        goToSection(PrTeam)
        loadAndSaveNewSection(PrTeam)

        logger.info { "Read upgrade section: $Legal" }
        goToSection(Legal)
        loadAndSaveNewSection(Legal)

        logger.info { "Read upgrade section: $SpecialsMy" }
        goToSection(SpecialsMy)
        loadAndSaveNewSection(SpecialsMy)

        logger.info { "Read upgrade section: $SpecialsNew" }
        goToSection(SpecialsNew)
        loadAndSaveNewSection(SpecialsNew)

        logger.info { "All section read successfully" }
        GameCommonAction.goToExchange()

        info.writeText(currentUpgrades.toJson())
    }

    fun calculateTarget(amount: Int, buySomething: Boolean = false, minCost: Int = 0, targetUpgrade: String = "", exclude: Set<String> = emptySet()): Upgrade {

        val allHaveRelativelyForNextLLevel = currentUpgrades.values.all { it.relativeProfit > 0 }

        val res = currentUpgrades
            .filterKeys { if (targetUpgrade.isNotBlank() && !sessionTargetAlreadyBought) it == targetUpgrade else true }
            .filterKeys { it !in exclude }
            .filterValues { if (buySomething) it.cost in minCost..amount else true }
            .filterValues(Upgrade::isUnlocked)
            .filterValues { it.needText.isBlank() && it.needUpgrade == null }
            .maxByOrNull {
                val hasTimer = it.value.timer.isNotBlank()
                val number = if (allHaveRelativelyForNextLLevel) it.value.relativeTotalMargin else it.value.totalMargin
                if (hasTimer) number * 5 else number
            }?.value ?: Upgrade.none
        logger.info { "Current total coins: $amount. Target upgrade: $res " }
        return res
    }

    fun buyUpgrade(upgrade: Upgrade): Boolean {
        GameCommonAction.goToMine()
        goToSection(upgrade.section)
        val newUpgrade = buyUpgradeCard(upgrade)
        currentUpgrades[upgrade.name] = newUpgrade

        info.writeText(currentUpgrades.toJson())
        logger.info { "Upgrade next level will be: $newUpgrade" }
        if (newUpgrade.name == Cfg.target_upgrade)
            sessionTargetAlreadyBought = true
        return newUpgrade != upgrade
    }
}

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
}
