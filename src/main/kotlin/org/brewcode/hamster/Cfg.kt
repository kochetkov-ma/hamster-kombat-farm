package org.brewcode.hamster

import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

@Suppress("ConstPropertyName")
object Cfg {

    /** Farming execution duration */
    val timeout = 8.hours

    /** Period of stamina check. Click iterations number */
    const val stamina_check_period = 5

    /** Level of stamina/energy when stop clicking and do another tasks */
    const val stamina_minimum_level = 250

    /** Wait duration for stamina/energy recover */
    val staminaWaitInterval = 10.minutes

    /**
     * Minimum cost of upgrade
     */
    const val min_cost = 0

    /**
     * If your laptop is not connected to power source you can move mouse to prevent sleep mode.
     */
    const val auto_move_mouse = false

    /**
     * Name of upgrade to buy and ignore others till buy. After reaching target_upgrade will buy others.
     */
    val desire_upgrades = emptyList<String>()

    const val time_priority = false

    const val upgrade_cost_factor = 4.0

    override fun toString(): String {
        return """
        |timeout=$timeout
        |staminaCheckPeriod=$stamina_check_period
        |staminaMinimumLevel=$stamina_minimum_level
        |staminaWaitInterval=$staminaWaitInterval
        |upgrade_cost_factor=$upgrade_cost_factor
        |min_cost=$min_cost
        |auto_move_mouse=$auto_move_mouse
        |target_upgrade='$desire_upgrades'
    """.trimMargin()
    }
}
