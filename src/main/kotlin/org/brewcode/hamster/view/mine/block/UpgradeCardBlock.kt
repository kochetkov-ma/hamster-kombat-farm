package org.brewcode.hamster.view.mine.block

import org.brewcode.hamster.service.Upgrade
import org.brewcode.hamster.util.*

data class UpgradeCardBlock(val name: String, private val upgrade: Upgrade) {

    val selfXpath = xText(name).xParent
    val relativeProfit = element(selfXpath.xChild("Profit per hour").xSibling(1).xChild("android.widget.TextView").xBy())

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    fun toFullUpgrade() = upgrade.copy(
        relativeProfit = relativeProfit.text.money()
    )
}
