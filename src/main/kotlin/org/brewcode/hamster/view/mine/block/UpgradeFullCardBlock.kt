package org.brewcode.hamster.view.mine.block

import org.brewcode.hamster.util.*

class UpgradeFullCardBlock(name: String) {

    private val xProfit = xText("Profit per hour", "android.widget.TextView").xLast()
    private val xName = xTextContains(name, "android.widget.TextView").xLast()
    val xSelf = xProfit.xAncestor("android.widget.ListView", 1).xParent

    // (//android.widget.TextView[@text="Profit per hour"])[last()]/ancestor::android.view.View[1]
    val self = element(xSelf.xBy())
    val closeButton = element(xSelf.xSiblingPrev(1, "android.view.View").xBy())
    val title = element(xName.xBy())
    val actionButton = element(xSelf.xChild("android.widget.Button").xLast().xBy())
}

fun main() {
    println(UpgradeFullCardBlock("").xSelf)
}
