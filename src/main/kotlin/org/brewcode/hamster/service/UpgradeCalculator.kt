package org.brewcode.hamster.service

import io.github.oshai.kotlinlogging.KotlinLogging
import org.brewcode.hamster.Cfg.exclude_upgrades
import org.brewcode.hamster.Cfg.min_cost
import org.brewcode.hamster.Cfg.time_priority
import org.brewcode.hamster.Cfg.upgrade_cost_backpressure_factor
import org.brewcode.hamster.Cfg.upgrade_cost_factor

private val logger = KotlinLogging.logger {}

data class UpgradeCalculator(
    private val upgrades: Map<String, Upgrade>,
    private val desireUpgrades: List<String>,
    private val minCost: Int = min_cost,
    private val maxCostFactor: Double = upgrade_cost_factor
) {

    private var costBackPressureFactor = 1.0
    private val exclude: MutableSet<String> = exclude_upgrades.toMutableSet()
    private val desireUpgradesToBuy: MutableList<String> = desireUpgrades.toMutableList()

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    fun costBackPressureFactorUp() {
        costBackPressureFactor *= upgrade_cost_backpressure_factor
    }

    fun exclude(vararg upgrades: String) = exclude.addAll(upgrades)

    fun calculate(coins: Int): Upgrade {

        val costRange = minCost..(coins.limit()).toInt()
        val hasDesireUpgradesToBuy = desireUpgradesToBuy.isNotEmpty()

        val filteredUpgrades = upgrades
            .filterKeys { it !in exclude }
            .filterKeys { if (hasDesireUpgradesToBuy) it in desireUpgradesToBuy else true }
            .filterValues { it.cost in costRange }
            .filterValues(Upgrade::isUnlocked)
            .filterValues { it.doesntNeedOtherUpgrade() }

        val useRelative = filteredUpgrades.values.all { it.hasRelative() }
        val filteredAndSortedUpgrades = filteredUpgrades
            .toList()
            .sortedBy { it.second.comparingValue(useRelative) }
            .toMap()

        val mostProfitable = filteredAndSortedUpgrades.toList().lastOrNull()?.second ?: Upgrade.none
        val hitSmartRelative = mostProfitable.hasRelative()
        val smartFilteredAndSortedUpgrades = if (hitSmartRelative) filteredAndSortedUpgrades.filter { it.value.hasRelative() } else filteredAndSortedUpgrades

        logger.info { "Calculating... Hit smart relative: ${mostProfitable.hasRelative()}. Limit: $costRange. Suitable upgrades(${filteredUpgrades.size}): ${filteredUpgrades.values.shortString()}" }

        val target = smartFilteredAndSortedUpgrades.maxByOrNull {
            if (it.value.withTimerPriority())
                it.value.comparingValue(hitSmartRelative) * 5
            else
                it.value.comparingValue(hitSmartRelative)
        }?.value ?: Upgrade.none

        logger.info { "Calculated! Target upgrade: $target " }
        desireUpgradesToBuy.remove(target.name)
        return target
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private fun Int.limit() = this * maxCostFactor / costBackPressureFactor
    private fun Upgrade.doesntNeedOtherUpgrade() = needText.isBlank() && needUpgrade == null
    private fun Upgrade.hasRelative() = relativeTotalMargin > 0
    private fun Upgrade.comparingValue(useRelative: Boolean = false) = if (useRelative) relativeTotalMargin else totalMargin
    private fun Upgrade.withTimerPriority() = if (time_priority) timer.isNotBlank() else false
    private fun Upgrade.shortString() = "$name=$cost[$totalProfit:$totalMargin or $relativeProfit:$relativeTotalMargin]"
    private fun Collection<Upgrade>.shortString() = joinToString(", ") { it.shortString() }

}
