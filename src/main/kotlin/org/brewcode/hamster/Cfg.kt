package org.brewcode.hamster

import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

object Cfg {

    /** Farming execution duration */
    val timeout = 8.hours

    /** Period of stamina check. Click iterations number */
    const val staminaCheckPeriod = 5

    /** Level of stamina/energy when stop clicking and do another tasks */
    const val staminaMinimumLevel = 250

    /** Wait duration for stamina/energy recover */
    val staminaWaitInterval = 10.minutes

    /**
     * - true - If you don't have money for most profitable upgrade try find something much cheaper
     * - false - If you don't have money for most profitable upgrade buy nothing and wait for more money
     */
    val buy_something = true

    /**
     * Minimum cost of upgrade to buy
     */
    val min_cost = 10_000

    /**
     * If your laptop is not connected to power source you can move mouse to prevent sleep mode.
     */
    val auto_move_mouse = true

    /**
     * Name of upgrade to buy and ignore others till buy. After reaching target_upgrade will buy others.
     */
    val target_upgrade = ""

    override fun toString(): String {
        return "[timeout=$timeout, staminaCheckPeriod=$staminaCheckPeriod, staminaMinimumLevel=$staminaMinimumLevel, staminaWaitInterval=$staminaWaitInterval, buy_something=$buy_something, min_cost=$min_cost, auto_move_mouse=$auto_move_mouse, target_upgrade='$target_upgrade']"
    }
}
