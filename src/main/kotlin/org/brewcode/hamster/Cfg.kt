package org.brewcode.hamster

import org.brewcode.hamster.util.BrewConfiguration
import org.brewcode.hamster.util.fromYaml
import kotlin.io.path.Path
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

@Suppress("ConstPropertyName")
object Cfg {

    private val yaml = Path("brew-hamster.yaml").fromYaml<BrewConfiguration>()

    /** Farming execution duration */
    val timeout = yaml.hamster.timeoutHours.hours

    /** Period of stamina check. Click iterations number */
    val stamina_check_period = yaml.advanced.staminaCheckPeriodSec

    /** Level of stamina/energy when stop clicking and do another tasks */
    val stamina_minimum_level = yaml.advanced.staminaMinimumLevel

    /** Wait duration for stamina/energy recover */
    val stamina_wait_interval = yaml.hamster.staminaWaitIntervalMin.minutes

    /** Minimum cost of upgrade */
    val min_cost = yaml.hamster.minCost

    /** If your laptop is not connected to power source you can move mouse to prevent sleep mode. */
    val auto_move_mouse = yaml.hamster.autoMoveMouse

    /** Automatically buy upgrade. Or save money */
    val buy_upgrades = yaml.hamster.buyUpgrades

    /** Combo. Name of upgrade to buy and ignore others till buy. After reaching target_upgrade will buy others */
    val desire_upgrades: List<String> = yaml.hamster.desireUpgrades

    /** Exclude upgrades from buying */
    val exclude_upgrades: List<String> = yaml.hamster.excludeUpgrades

    /** Maximum cost of upgrade to save extra money. Coins amount * upgrade_cost_factor */
    val upgrade_cost_factor = yaml.hamster.upgradeCostFactor

    /** Maximum cost of upgrade to save extra money. Coins amount * upgrade_cost_factor */
    val upgrade_cost_backpressure_factor = yaml.advanced.upgradeCostBackpressureFactor

    override fun toString(): String {
        return """
        |timeout=$timeout
        |stamina_check_period=$stamina_check_period
        |stamina_minimum_level=$stamina_minimum_level
        |stamina_wait_interval=$stamina_wait_interval
        |min_cost=$min_cost
        |auto_move_mouse=$auto_move_mouse
        |buy_upgrades=$buy_upgrades
        |target_upgrade='$desire_upgrades'
        |exclude_upgrades='$exclude_upgrades'
        |upgrade_cost_factor=$upgrade_cost_factor
        |upgrade_cost_backpressure_factor=$upgrade_cost_backpressure_factor
    """.trimMargin()
    }
}
