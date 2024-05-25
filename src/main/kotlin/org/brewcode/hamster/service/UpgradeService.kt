package org.brewcode.hamster.service

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.oshai.kotlinlogging.KotlinLogging
import org.brewcode.hamster.action.GameCommonAction
import org.brewcode.hamster.action.GameMineAction.buyUpgradeCard
import org.brewcode.hamster.action.GameMineAction.goToSection
import org.brewcode.hamster.action.GameMineAction.loadCards
import org.brewcode.hamster.service.UpgradeSection.*
import org.brewcode.hamster.service.UpgradeService.updateUpgrades
import org.brewcode.hamster.util.configureSession
import org.brewcode.hamster.util.fromJson
import org.brewcode.hamster.util.toJson
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

val logger = KotlinLogging.logger {}

object UpgradeService {

    private val info = Path("build/upgrade.json").also { if (!it.exists()) it.writeText("") }

    private var currentUpgrades = mutableMapOf<String, Upgrade>()

    val isEmptyUpgradesCache get() = currentUpgrades.isEmpty()

    fun remove(upgrade: Upgrade) = currentUpgrades.remove(upgrade.name)

    fun loadUpgrades() {
        currentUpgrades = info.readText().let { if (it.isEmpty()) mutableMapOf() else it.fromJson<MutableMap<String, Upgrade>>() }
    }

    fun updateUpgrades() {

        GameCommonAction.goToMine()

        logger.info { "Read upgrade section: $Markets" }
        goToSection(Markets)
        currentUpgrades.putAll(loadCards(Markets))

        logger.info { "Read upgrade section: $PrTeam" }
        goToSection(PrTeam)
        currentUpgrades.putAll(loadCards(PrTeam))

        logger.info { "Read upgrade section: $Legal" }
        goToSection(Legal)
        currentUpgrades.putAll(loadCards(Legal))

        logger.info { "All section read successfully" }
        GameCommonAction.goToExchange()

        info.writeText(currentUpgrades.toJson())
    }

    fun calculateTarget(amount: Int, buySomething: Boolean = false): Upgrade {

        val res = currentUpgrades
            .filterValues { if (buySomething) it.cost <= amount else true }
            .filterValues(Upgrade::isUnlocked)
            .filterValues { it.needText.isBlank() && it.needUpgrade == null }
            .maxBy { it.value.totalMargin }.value
        logger.info { "Current total coins: $amount .Target upgrade: $res " }
        return res
    }

    fun buyUpgrade(upgrade: Upgrade) {
        GameCommonAction.goToMine()
        goToSection(upgrade.section)
        val newUpgrade = buyUpgradeCard(upgrade)
        currentUpgrades[upgrade.name] = newUpgrade

        info.writeText(currentUpgrades.toJson())
    }
}

data class Upgrade(
    val section: UpgradeSection,
    val name: String,
    val index: Int,
    val level: Int,
    val totalProfit: Int,
    val relativeProfit: Int,
    val cost: Int,
    val needText: String,
    @JsonProperty("unlocked")
    val isUnlocked: Boolean = true,
    val needUpgrade: Upgrade? = null
) {
    /**
     * Greater is better
     */
    val totalMargin = if (cost != 0) totalProfit / cost else 0

    companion object {
        val empty = Upgrade(Markets, "", 0, 0, 0, 0, 0, "")
    }

}

enum class UpgradeSection(vararg path: String) {
    Markets("Markets"),
    PrTeam("Pr&Team"),
    Legal("Legal"),
    SpecialsMy("Specials", "My cards"),
    SpecialsNew("Specials", "New cards")
}

fun main() {
    configureSession()
    updateUpgrades()
}
