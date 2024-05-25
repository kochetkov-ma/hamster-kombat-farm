package org.brewcode.hamster.view.mine.block

import org.brewcode.hamster.util.element
import org.brewcode.hamster.util.xBy
import org.brewcode.hamster.util.xParent
import org.brewcode.hamster.util.xText

object TopMenuBlock {

    object X {
        val marketsX = xText("Markets")
        val menuSectionXpath = marketsX.xParent
    }

    val markets = element(X.marketsX.xBy())
    val prTeam = element(xText("PR&Team").xBy())
    val legal = element(xText("Legal").xBy())
    val specials = element(xText("Specials").xBy())
    val self = element(X.menuSectionXpath.xBy())
}
